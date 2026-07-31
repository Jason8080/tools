package cn.gmlee.tools.im.sse;

import cn.gmlee.tools.im.conf.SseProperties;
import cn.gmlee.tools.im.sse.backpressure.BackpressureStrategyResolver;
import cn.gmlee.tools.im.sse.backpressure.DropOldestBackpressureStrategy;
import cn.gmlee.tools.im.sse.metrics.NoOpSseMetrics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SseConnectionRegistry 并发测试.
 * <p>
 * 验证高并发场景下计数器、连接注册/注销的线程安全性。
 * </p>
 */
class SseConnectionRegistryConcurrencyTest {

    private SseConnectionRegistry registry;
    private SseProperties properties;

    @BeforeEach
    void setUp() {
        properties = new SseProperties();
        properties.setMaxTotalConnections(10000);
        properties.setMaxConnectionsPerTopic(1000);

        BackpressureStrategyResolver resolver = topic -> new DropOldestBackpressureStrategy();
        registry = new SseConnectionRegistry(properties, resolver);
        registry.setMetrics(NoOpSseMetrics.INSTANCE);
    }

    @Test
    @DisplayName("并发订阅 - 1000 个连接同时订阅同一 Topic")
    void testConcurrentSubscribeSameTopic() throws InterruptedException {
        String topic = "test.topic";
        int threadCount = 1000;
        ExecutorService executor = Executors.newFixedThreadPool(50);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    // 模拟真实 subscribe() 流程：先 tryAcquire 递增计数器，再 register 填充 map
                    registry.getCounter().tryAcquire(topic,
                            properties.getMaxTotalConnections(), properties.getMaxConnectionsPerTopic());
                    SseConnection conn = new SseConnection(topic);
                    registry.register(conn);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        assertTrue(latch.await(10, TimeUnit.SECONDS), "所有线程应在 10 秒内完成");
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        // 验证：所有连接都应成功注册（未达到限制）
        assertEquals(threadCount, successCount.get(), "所有连接应成功注册");
        assertEquals(0, failureCount.get(), "不应有失败");
        assertEquals(threadCount, registry.getCounter().getTotalConnections().get());
        assertEquals(threadCount, registry.getCounter().getTopicCount(topic));
    }

    @Test
    @DisplayName("并发注销 - 验证计数器不会重复递减")
    void testConcurrentUnregister() throws InterruptedException {
        String topic = "test.topic";
        int connectionCount = 100;

        // 先注册 100 个连接（模拟真实 subscribe() 流程：tryAcquire + register）
        for (int i = 0; i < connectionCount; i++) {
            registry.getCounter().tryAcquire(topic,
                    properties.getMaxTotalConnections(), properties.getMaxConnectionsPerTopic());
            SseConnection conn = new SseConnection(topic);
            registry.register(conn);
        }

        // 并发清理所有连接（cleanupConnection = tryDecrement + unregister + cleanupIfEmpty）
        ExecutorService executor = Executors.newFixedThreadPool(20);
        CountDownLatch latch = new CountDownLatch(connectionCount);
        AtomicInteger unregisterCount = new AtomicInteger(0);

        for (SseConnection conn : registry.snapshotConnections()) {
            executor.submit(() -> {
                try {
                    registry.cleanupConnection(conn, NoOpSseMetrics.INSTANCE);
                    unregisterCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        assertTrue(latch.await(5, TimeUnit.SECONDS));
        executor.shutdown();
        executor.awaitTermination(3, TimeUnit.SECONDS);

        // 验证：计数器应准确归零（不会因并发重复递减而下溢）
        assertEquals(0, registry.getCounter().getTotalConnections().get(),
                "总连接数应为 0");
        assertEquals(0, registry.getCounter().getTopicCount(topic),
                "Topic 连接数应为 0");
        assertEquals(connectionCount, unregisterCount.get());
    }

    @Test
    @DisplayName("计数器一致性 - 总连接数等于各 Topic 连接数之和")
    void testCounterConsistency() throws InterruptedException {
        int topicCount = 10;
        int connectionsPerTopic = 50;
        ExecutorService executor = Executors.newFixedThreadPool(20);
        CountDownLatch latch = new CountDownLatch(topicCount * connectionsPerTopic);

        // 并发创建多个 Topic 的连接
        for (int t = 0; t < topicCount; t++) {
            String topic = "topic." + t;
            for (int c = 0; c < connectionsPerTopic; c++) {
                executor.submit(() -> {
                    try {
                        // 模拟真实 subscribe() 流程：先 tryAcquire 递增计数器
                        registry.getCounter().tryAcquire(topic,
                                properties.getMaxTotalConnections(), properties.getMaxConnectionsPerTopic());
                        SseConnection conn = new SseConnection(topic);
                        registry.register(conn);
                    } finally {
                        latch.countDown();
                    }
                });
            }
        }

        assertTrue(latch.await(5, TimeUnit.SECONDS));
        executor.shutdown();
        executor.awaitTermination(3, TimeUnit.SECONDS);

        // 验证计数器一致性
        long totalConnections = registry.getCounter().getTotalConnections().get();
        long sumOfTopicCounts = 0;
        for (int t = 0; t < topicCount; t++) {
            sumOfTopicCounts += registry.getCounter().getTopicCount("topic." + t);
        }

        assertEquals(totalConnections, sumOfTopicCounts,
                "总连接数应等于各 Topic 连接数之和");
        assertEquals((long) topicCount * connectionsPerTopic, totalConnections);
    }

    @Test
    @DisplayName("topicCounts 压缩 - 验证延迟移除机制")
    void testTopicCountsCompaction() throws InterruptedException {
        properties.getCleanup().setCompactTtl(java.time.Duration.ofMillis(100));

        // 创建并注销连接，使 Topic 变空
        String topic1 = "topic.compact";
        // 先创建 Sink（cleanupIfEmpty 通过 topicSinks.containsKey 判断是否执行清理并追踪空 Topic）
        registry.getOrCreateSink(topic1);
        // 模拟真实 subscribe() 流程：先 tryAcquire 递增计数器，再 register
        registry.getCounter().tryAcquire(topic1,
                properties.getMaxTotalConnections(), properties.getMaxConnectionsPerTopic());
        SseConnection conn1 = new SseConnection(topic1);
        registry.register(conn1);
        // cleanupConnection = tryDecrement + unregister + cleanupIfEmpty（追踪空 Topic）
        registry.cleanupConnection(conn1, NoOpSseMetrics.INSTANCE);

        // 立即检查：计数器应保留（值为 0）
        assertEquals(0, registry.getCounter().getTopicCount(topic1));

        // 等待超过 compactTtl
        Thread.sleep(200);

        // 触发压缩
        int compacted = registry.compactTopicCounts(100);

        // 验证：条目应被移除
        assertEquals(1, compacted);
    }
}
