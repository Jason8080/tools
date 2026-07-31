package cn.gmlee.tools.im.sse;

import cn.gmlee.tools.im.conf.SseProperties;
import cn.gmlee.tools.im.model.ConnectionMetadata;
import cn.gmlee.tools.im.model.MessageMap;
import cn.gmlee.tools.im.model.Msg;
import cn.gmlee.tools.im.model.TopicMessage;
import cn.gmlee.tools.im.sse.backpressure.BackpressureStrategyResolver;
import cn.gmlee.tools.im.sse.backpressure.DropOldestBackpressureStrategy;
import cn.gmlee.tools.im.sse.metrics.NoOpSseMetrics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SseConnectionRegistry 双通道架构测试.
 * <p>
 * 验证反向索引（routingKeyIndex）和定向 Sink（directedSink）的注册/注销/清理逻辑。
 * </p>
 */
class SseConnectionRegistryDirectedTest {

    private SseConnectionRegistry registry;

    @BeforeEach
    void setUp() {
        SseProperties properties = new SseProperties();
        properties.setMaxTotalConnections(10000);
        properties.setMaxConnectionsPerTopic(1000);

        BackpressureStrategyResolver resolver = topic -> new DropOldestBackpressureStrategy();
        registry = new SseConnectionRegistry(properties, resolver);
        registry.setMetrics(NoOpSseMetrics.INSTANCE);
    }

    // ==================== 辅助方法 ====================

    /**
     * 创建带 routingKey 的连接.
     */
    private SseConnection createConnection(String topic, String routingKey) {
        SseConnection conn = new SseConnection(topic);
        ConnectionMetadata metadata = ConnectionMetadata.builder()
                .topic(topic)
                .routingKey(routingKey)
                .connectionId(conn.getConnectionId())
                .build();
        conn.setMetadata(metadata);
        return conn;
    }

    /**
     * 创建无 routingKey 的连接（纯广播）.
     */
    private SseConnection createBroadcastConnection(String topic) {
        return new SseConnection(topic);
    }

    /**
     * 检查 Sink 是否已完成（tryEmitNext 返回 FAIL_TERMINATED）.
     */
    private boolean isSinkCompleted(Sinks.Many<TopicMessage> sink) {
        MessageMap msg = new MessageMap();
        msg.put("test", "probe");
        TopicMessage probe = TopicMessage.builder()
                .topic("probe")
                .msg(msg)
                .build();
        Sinks.EmitResult result = sink.tryEmitNext(probe);
        return result == Sinks.EmitResult.FAIL_TERMINATED;
    }

    // ==================== 注册测试 ====================

    @Test
    @DisplayName("有 routingKey 的连接注册 - directedSink 和反向索引正确创建")
    void testRegisterWithRoutingKey() {
        String topic = "test.topic";
        SseConnection conn = createConnection(topic, "alice");

        registry.register(conn);

        // directedSink 已创建
        assertTrue(conn.hasDirectedSink(), "连接应有 directedSink");
        assertNotNull(conn.getDirectedSink(), "directedSink 不应为 null");

        // directedSinks map 包含此连接
        Sinks.Many<TopicMessage> ds = registry.getDirectedSink(conn.getConnectionId());
        assertNotNull(ds, "registry 应能获取 directedSink");
        assertSame(conn.getDirectedSink(), ds, "返回的 Sink 应与连接自身的一致");

        // 反向索引包含此连接
        Set<String> connIds = registry.getConnectionIdsByRoutingKey(topic, "alice");
        assertEquals(1, connIds.size(), "反向索引应有 1 个连接");
        assertTrue(connIds.contains(conn.getConnectionId()), "反向索引应包含此连接 ID");

        // 无 routingKey 的查询返回空集
        Set<String> empty = registry.getConnectionIdsByRoutingKey(topic, "nonexistent");
        assertTrue(empty.isEmpty(), "不存在的 routingKey 应返回空集");

        // directedSink 计数
        assertEquals(1, registry.getDirectedSinkCount(), "定向 Sink 数应为 1");
    }

    @Test
    @DisplayName("无 routingKey 的连接注册 - 不创建 directedSink")
    void testRegisterWithoutRoutingKey() {
        String topic = "test.topic";
        SseConnection conn = createBroadcastConnection(topic);

        registry.register(conn);

        // 无 directedSink
        assertFalse(conn.hasDirectedSink(), "无 routingKey 连接不应有 directedSink");
        assertNull(conn.getDirectedSink(), "directedSink 应为 null");

        // directedSinks map 不包含此连接
        assertNull(registry.getDirectedSink(conn.getConnectionId()), "registry 不应有 directedSink");

        // directedSink 计数为 0
        assertEquals(0, registry.getDirectedSinkCount(), "定向 Sink 数应为 0");
    }

    @Test
    @DisplayName("多连接共享同一 routingKey - 反向索引集合正确")
    void testMultipleConnectionsSameRoutingKey() {
        String topic = "test.topic";
        SseConnection conn1 = createConnection(topic, "alice");
        SseConnection conn2 = createConnection(topic, "alice");
        SseConnection conn3 = createConnection(topic, "bob");

        registry.register(conn1);
        registry.register(conn2);
        registry.register(conn3);

        // alice 有 2 个连接
        Set<String> aliceIds = registry.getConnectionIdsByRoutingKey(topic, "alice");
        assertEquals(2, aliceIds.size(), "alice 应有 2 个连接");
        assertTrue(aliceIds.contains(conn1.getConnectionId()));
        assertTrue(aliceIds.contains(conn2.getConnectionId()));

        // bob 有 1 个连接
        Set<String> bobIds = registry.getConnectionIdsByRoutingKey(topic, "bob");
        assertEquals(1, bobIds.size(), "bob 应有 1 个连接");
        assertTrue(bobIds.contains(conn3.getConnectionId()));

        assertEquals(3, registry.getDirectedSinkCount(), "定向 Sink 数应为 3");
    }

    // ==================== 注销测试 ====================

    @Test
    @DisplayName("注销有 routingKey 的连接 - directedSink 完成、反向索引清理")
    void testUnregisterWithRoutingKey() {
        String topic = "test.topic";
        SseConnection conn = createConnection(topic, "alice");
        registry.register(conn);

        Sinks.Many<TopicMessage> ds = conn.getDirectedSink();

        // 注销
        registry.unregister(conn);

        // directedSink 应已从 map 移除
        assertNull(registry.getDirectedSink(conn.getConnectionId()), "注销后 registry 不应有 directedSink");

        // 反向索引应已清理
        Set<String> connIds = registry.getConnectionIdsByRoutingKey(topic, "alice");
        assertTrue(connIds.isEmpty(), "注销后反向索引应为空");

        // directedSink 应已发送完成信号（tryEmitNext 返回 FAIL_TERMINATED）
        assertTrue(isSinkCompleted(ds), "注销后 directedSink 应已完成");

        assertEquals(0, registry.getDirectedSinkCount(), "定向 Sink 数应为 0");
    }

    @Test
    @DisplayName("注销一个共享 routingKey 的连接 - 其他连接保留")
    void testUnregisterOneOfSharedRoutingKey() {
        String topic = "test.topic";
        SseConnection conn1 = createConnection(topic, "alice");
        SseConnection conn2 = createConnection(topic, "alice");

        registry.register(conn1);
        registry.register(conn2);

        // 注销 conn1
        registry.unregister(conn1);

        // alice 应仅剩 conn2
        Set<String> aliceIds = registry.getConnectionIdsByRoutingKey(topic, "alice");
        assertEquals(1, aliceIds.size(), "alice 应剩余 1 个连接");
        assertTrue(aliceIds.contains(conn2.getConnectionId()), "剩余连接应为 conn2");

        assertEquals(1, registry.getDirectedSinkCount(), "定向 Sink 数应为 1");
    }

    // ==================== 空 Topic 清理测试 ====================

    @Test
    @DisplayName("cleanupIfEmpty - routingKeyIndex 条目被移除")
    void testCleanupIfEmptyRemovesRoutingKeyIndex() {
        String topic = "test.topic";

        // 创建 topicSink（cleanupIfEmpty 通过 sinkExistedBefore 判断是否执行清理）
        registry.getOrCreateSink(topic);

        SseConnection conn = createConnection(topic, "alice");
        registry.register(conn);

        // 模拟计数器为 0（cleanupConnection 内 counter.tryDecrement 会先递减）
        registry.getCounter().getOrCreateTopicCount(topic);

        boolean cleaned = registry.cleanupIfEmpty(topic);
        assertTrue(cleaned, "空 Topic 应被清理");

        // routingKeyIndex 条目应已移除
        Set<String> connIds = registry.getConnectionIdsByRoutingKey(topic, "alice");
        assertTrue(connIds.isEmpty(), "清理后反向索引应为空");
    }

    // ==================== closeAll 测试 ====================

    @Test
    @DisplayName("closeAll - 所有 directedSink 完成、索引清空")
    void testCloseAll() {
        String topic = "test.topic";
        SseConnection conn1 = createConnection(topic, "alice");
        SseConnection conn2 = createConnection(topic, "bob");
        SseConnection conn3 = createBroadcastConnection(topic);

        registry.register(conn1);
        registry.register(conn2);
        registry.register(conn3);

        Sinks.Many<TopicMessage> ds1 = conn1.getDirectedSink();
        Sinks.Many<TopicMessage> ds2 = conn2.getDirectedSink();

        registry.closeAll();

        // directedSink 计数归零
        assertEquals(0, registry.getDirectedSinkCount(), "closeAll 后定向 Sink 数应为 0");

        // getAllDirectedSinks 返回空
        Collection<Sinks.Many<TopicMessage>> allSinks = registry.getAllDirectedSinks();
        assertTrue(allSinks.isEmpty(), "closeAll 后 getAllDirectedSinks 应为空");

        // 已移除的 Sink 应发送了完成信号
        assertTrue(isSinkCompleted(ds1), "closeAll 后 ds1 应已完成");
        assertTrue(isSinkCompleted(ds2), "closeAll 后 ds2 应已完成");
    }

    // ==================== 并发测试 ====================

    @Test
    @DisplayName("并发注册/注销 directedSink - 无 NPE、计数正确")
    void testConcurrentDirectedOperations() throws InterruptedException {
        String topic = "test.topic";
        int connectionCount = 200;
        CountDownLatch registerLatch = new CountDownLatch(connectionCount);
        CountDownLatch unregisterLatch = new CountDownLatch(connectionCount);
        ExecutorService executor = Executors.newFixedThreadPool(20);

        SseConnection[] connections = new SseConnection[connectionCount];
        for (int i = 0; i < connectionCount; i++) {
            connections[i] = createConnection(topic, "user" + (i % 10)); // 10 个不同的 routingKey
        }

        // 并发注册
        for (int i = 0; i < connectionCount; i++) {
            final SseConnection conn = connections[i];
            executor.submit(() -> {
                try {
                    registry.register(conn);
                } finally {
                    registerLatch.countDown();
                }
            });
        }

        assertTrue(registerLatch.await(10, TimeUnit.SECONDS), "注册应在 10 秒内完成");

        // 验证注册后状态
        assertEquals(connectionCount, registry.getDirectedSinkCount(), "注册后定向 Sink 数应正确");

        // 并发注销
        for (int i = 0; i < connectionCount; i++) {
            final SseConnection conn = connections[i];
            executor.submit(() -> {
                try {
                    registry.unregister(conn);
                } finally {
                    unregisterLatch.countDown();
                }
            });
        }

        assertTrue(unregisterLatch.await(10, TimeUnit.SECONDS), "注销应在 10 秒内完成");
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        // 验证注销后状态
        assertEquals(0, registry.getDirectedSinkCount(), "注销后定向 Sink 数应为 0");
    }
}
