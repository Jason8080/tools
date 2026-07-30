package cn.gmlee.tools.im.sse;

import cn.gmlee.tools.im.conf.SseProperties;
import cn.gmlee.tools.im.sse.backpressure.BackpressureStrategyResolver;
import cn.gmlee.tools.im.sse.backpressure.DropOldestBackpressureStrategy;
import cn.gmlee.tools.im.sse.metrics.NoOpSseMetrics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SSE 连接管理器性能基准测试.
 * <p>
 * 验证框架在高并发场景下的性能表现。
 * 注意：这些测试可能需要较长时间运行，默认禁用。
 * 运行方式：mvn test -Dtest=SsePerformanceBenchmarkTest
 * </p>
 */
@Disabled("性能测试，需要时手动启用")
class SsePerformanceBenchmarkTest {

    private SseConnectionRegistry registry;
    private SseProperties properties;

    @BeforeEach
    void setUp() {
        properties = new SseProperties();
        properties.setMaxTotalConnections(100000);
        properties.setMaxConnectionsPerTopic(50000);

        BackpressureStrategyResolver resolver = topic -> new DropOldestBackpressureStrategy();
        registry = new SseConnectionRegistry(properties, resolver);
        registry.setMetrics(NoOpSseMetrics.INSTANCE);
    }

    @Test
    @DisplayName("10000 并发连接 - 验证内存占用和延迟")
    void test10KConcurrentConnections() throws InterruptedException {
        int connectionCount = 10000;
        int threadCount = 100;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(connectionCount);

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < connectionCount; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    String topic = "topic." + (index % 100); // 100 个不同 Topic
                    SseConnection conn = new SseConnection(topic);
                    registry.register(conn);
                } finally {
                    latch.countDown();
                }
            });
        }

        assertTrue(latch.await(30, TimeUnit.SECONDS), "应在 30 秒内完成");
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        // 验证结果
        assertEquals(connectionCount, registry.getCounter().getTotalConnections().get());

        // 输出性能指标
        System.out.println("=== 10K 并发连接性能测试 ===");
        System.out.println("总连接数: " + connectionCount);
        System.out.println("总耗时: " + duration + " ms");
        System.out.println("平均延迟: " + (duration / (double) connectionCount) + " ms/连接");
        System.out.println("吞吐量: " + (connectionCount * 1000.0 / duration) + " 连接/秒");
        System.out.println("活跃 Topic 数: " + 100);

        // 内存估算（粗略）
        long memoryUsed = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        System.out.println("内存使用: " + (memoryUsed / 1024 / 1024) + " MB");
        System.out.println("每连接内存: " + (memoryUsed / connectionCount) + " bytes");
    }

    @Test
    @DisplayName("高频发布/订阅 - 验证消息吞吐")
    void testHighFrequencyPublishSubscribe() throws InterruptedException {
        String topic = "benchmark.topic";
        int subscriberCount = 1000;
        int messageCount = 10000;

        // 创建订阅者
        for (int i = 0; i < subscriberCount; i++) {
            SseConnection conn = new SseConnection(topic);
            registry.register(conn);
        }

        ExecutorService executor = Executors.newFixedThreadPool(50);
        CountDownLatch latch = new CountDownLatch(messageCount);

        long startTime = System.currentTimeMillis();

        // 高频发布消息
        for (int i = 0; i < messageCount; i++) {
            executor.submit(() -> {
                try {
                    // 模拟消息发布
                    registry.getSink(topic).tryEmitNext(new cn.gmlee.tools.im.model.TopicMessage<>());
                } finally {
                    latch.countDown();
                }
            });
        }

        assertTrue(latch.await(30, TimeUnit.SECONDS));
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        // 输出性能指标
        System.out.println("=== 高频发布性能测试 ===");
        System.out.println("订阅者数量: " + subscriberCount);
        System.out.println("消息数量: " + messageCount);
        System.out.println("总耗时: " + duration + " ms");
        System.out.println("平均延迟: " + (duration / (double) messageCount) + " ms/消息");
        System.out.println("吞吐量: " + (messageCount * 1000.0 / duration) + " 消息/秒");
    }

    @Test
    @DisplayName("连接生命周期 - 验证创建和销毁的性能")
    void testConnectionLifecyclePerformance() throws InterruptedException {
        int iterations = 10000;
        String topic = "lifecycle.topic";

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < iterations; i++) {
            SseConnection conn = new SseConnection(topic);
            registry.register(conn);
            registry.unregister(conn);
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        // 输出性能指标
        System.out.println("=== 连接生命周期性能测试 ===");
        System.out.println("迭代次数: " + iterations);
        System.out.println("总耗时: " + duration + " ms");
        System.out.println("平均延迟: " + (duration / (double) iterations) + " ms/次");
        System.out.println("吞吐量: " + (iterations * 1000.0 / duration) + " 次/秒");

        // 验证计数器归零
        assertEquals(0, registry.getCounter().getTotalConnections().get());
        assertEquals(0, registry.getCounter().getTopicCount(topic));
    }
}
