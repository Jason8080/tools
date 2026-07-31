package cn.gmlee.tools.im.sse;

import cn.gmlee.tools.im.conf.SseProperties;
import cn.gmlee.tools.im.model.ConnectionMetadata;
import cn.gmlee.tools.im.model.MessageMap;
import cn.gmlee.tools.im.model.Msg;
import cn.gmlee.tools.im.model.TopicMessage;
import cn.gmlee.tools.im.sse.backpressure.BackpressureStrategyResolver;
import cn.gmlee.tools.im.sse.backpressure.DropOldestBackpressureStrategy;
import cn.gmlee.tools.im.sse.cleanup.ConnectionReaper;
import cn.gmlee.tools.im.sse.metrics.NoOpSseMetrics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SseConnectionManager 定向投递集成测试.
 * <p>
 * 验证双通道架构下广播/定向投递的正确性：
 * <ul>
 *   <li>广播消息 → 所有连接通过 topicSink 接收</li>
 *   <li>定向消息 → 仅目标连接通过 directedSink 接收</li>
 *   <li>无目标的定向消息 → 不投递</li>
 * </ul>
 * </p>
 * <p>
 * <b>注意</b>：directedSink 使用 multicast 模式，必须先有订阅者再 emit，
 * 否则 tryEmitNext 返回 FAIL_NO_SUBSCRIBERS 导致消息丢弃。
 * 因此测试中先订阅再 publish。
 * </p>
 */
class SseConnectionManagerDirectedPublishTest {

    private SseConnectionManager manager;
    private SseConnectionRegistry registry;
    private SseProperties properties;

    @BeforeEach
    void setUp() {
        properties = new SseProperties();
        properties.setMaxTotalConnections(10000);
        properties.setMaxConnectionsPerTopic(1000);
        properties.setMaxConnectionLifetime(Duration.ZERO); // 禁用连接超时，简化测试

        BackpressureStrategyResolver resolver = topic -> new DropOldestBackpressureStrategy();
        registry = new SseConnectionRegistry(properties, resolver);
        registry.setMetrics(NoOpSseMetrics.INSTANCE);

        ConnectionReaper reaper = new ConnectionReaper(registry, NoOpSseMetrics.INSTANCE, properties);
        manager = new SseConnectionManager(properties, registry, NoOpSseMetrics.INSTANCE, reaper, Collections.emptyList());
    }

    // ==================== 辅助方法 ====================

    /**
     * 创建带 routingKey 的连接并注册.
     */
    private SseConnection registerAndSubscribe(String topic, String routingKey) {
        // 确保 topicSink 已创建
        registry.getOrCreateSink(topic);

        SseConnection conn = new SseConnection(topic);
        ConnectionMetadata metadata = ConnectionMetadata.builder()
                .topic(topic)
                .routingKey(routingKey)
                .connectionId(conn.getConnectionId())
                .build();
        conn.setMetadata(metadata);
        registry.register(conn);
        return conn;
    }

    private TopicMessage<Msg> createBroadcastMessage(String topic, String content) {
        MessageMap msg = new MessageMap();
        msg.put("content", content);
        return TopicMessage.<Msg>builder()
                .topic(topic)
                .msg(msg)
                .build();
    }

    private TopicMessage<Msg> createDirectedMessage(String topic, String content, Set<String> routingKeys) {
        MessageMap msg = new MessageMap();
        msg.put("content", content);
        return TopicMessage.<Msg>builder()
                .topic(topic)
                .msg(msg)
                .routingKeys(routingKeys)
                .build();
    }

    /**
     * 订阅 Sink 并异步等待接收消息（必须先订阅再 emit，multicast 模式要求）.
     *
     * @return AtomicReference，publish 后可通过 get() 获取收到的消息（超时为 null）
     */
    private AtomicReference<TopicMessage<Msg>> subscribeSink(Sinks.Many<TopicMessage<Msg>> sink) {
        AtomicReference<TopicMessage<Msg>> received = new AtomicReference<>();
        sink.asFlux().subscribe(
                received::set,
                error -> { /* ignore */ },
                () -> { /* ignore */ }
        );
        return received;
    }

    // ==================== 广播投递测试 ====================

    @Test
    @DisplayName("广播消息 → topicSink 可收到")
    void testBroadcastDelivery() {
        String topic = "test.topic";

        // 注册 3 个有 routingKey 的连接
        registerAndSubscribe(topic, "alice");
        registerAndSubscribe(topic, "bob");
        registerAndSubscribe(topic, "charlie");

        // 先订阅 topicSink（广播通道）
        Sinks.Many<TopicMessage<Msg>> topicSink = registry.getSink(topic);
        AtomicReference<TopicMessage<Msg>> received = subscribeSink(topicSink);

        // 发布广播消息
        TopicMessage<Msg> msg = createBroadcastMessage(topic, "hello all");
        manager.publish(msg);

        // 验证收到
        assertNotNull(received.get(), "topicSink 应收到广播消息");
        assertEquals("hello all", ((MessageMap) received.get().getMsg()).get("content"));
    }

    // ==================== 定向投递测试 ====================

    @Test
    @DisplayName("定向消息 → 仅目标连接通过 directedSink 接收")
    void testDirectedDelivery() {
        String topic = "test.topic";

        // 注册 3 个连接
        SseConnection connAlice = registerAndSubscribe(topic, "alice");
        SseConnection connBob = registerAndSubscribe(topic, "bob");
        SseConnection connCharlie = registerAndSubscribe(topic, "charlie");

        // 先订阅所有 directedSink（multicast 模式要求先订阅再 emit）
        Sinks.Many<TopicMessage<Msg>> aliceDS = registry.getDirectedSink(connAlice.getConnectionId());
        Sinks.Many<TopicMessage<Msg>> bobDS = registry.getDirectedSink(connBob.getConnectionId());
        Sinks.Many<TopicMessage<Msg>> charlieDS = registry.getDirectedSink(connCharlie.getConnectionId());

        AtomicReference<TopicMessage<Msg>> aliceReceived = subscribeSink(aliceDS);
        AtomicReference<TopicMessage<Msg>> bobReceived = subscribeSink(bobDS);
        AtomicReference<TopicMessage<Msg>> charlieReceived = subscribeSink(charlieDS);

        // 发布定向消息到 alice
        TopicMessage<Msg> msg = createDirectedMessage(topic, "hello alice", Set.of("alice"));
        manager.publish(msg);

        // alice 应收到
        assertNotNull(aliceReceived.get(), "alice 的 directedSink 应收到消息");
        assertEquals("hello alice", ((MessageMap) aliceReceived.get().getMsg()).get("content"));

        // bob 和 charlie 不应收到
        assertNull(bobReceived.get(), "bob 不应收到定向消息");
        assertNull(charlieReceived.get(), "charlie 不应收到定向消息");
    }

    @Test
    @DisplayName("定向消息到多个目标 → 所有目标连接收到")
    void testDirectedDeliveryMultipleTargets() {
        String topic = "test.topic";

        SseConnection connAlice = registerAndSubscribe(topic, "alice");
        SseConnection connBob = registerAndSubscribe(topic, "bob");
        SseConnection connCharlie = registerAndSubscribe(topic, "charlie");

        // 先订阅所有 directedSink
        Sinks.Many<TopicMessage<Msg>> aliceDS = registry.getDirectedSink(connAlice.getConnectionId());
        Sinks.Many<TopicMessage<Msg>> bobDS = registry.getDirectedSink(connBob.getConnectionId());
        Sinks.Many<TopicMessage<Msg>> charlieDS = registry.getDirectedSink(connCharlie.getConnectionId());

        AtomicReference<TopicMessage<Msg>> aliceReceived = subscribeSink(aliceDS);
        AtomicReference<TopicMessage<Msg>> bobReceived = subscribeSink(bobDS);
        AtomicReference<TopicMessage<Msg>> charlieReceived = subscribeSink(charlieDS);

        // 发布定向消息到 alice 和 bob
        TopicMessage<Msg> msg = createDirectedMessage(topic, "hello alice+bob", Set.of("alice", "bob"));
        manager.publish(msg);

        // alice 和 bob 应收到
        assertNotNull(aliceReceived.get(), "alice 应收到定向消息");
        assertEquals("hello alice+bob", ((MessageMap) aliceReceived.get().getMsg()).get("content"));

        assertNotNull(bobReceived.get(), "bob 应收到定向消息");
        assertEquals("hello alice+bob", ((MessageMap) bobReceived.get().getMsg()).get("content"));

        // charlie 不应收到
        assertNull(charlieReceived.get(), "charlie 不应收到定向消息");
    }

    @Test
    @DisplayName("定向消息无目标 → 不投递，无异常")
    void testDirectedDeliveryNoTargets() {
        String topic = "test.topic";

        // 注册 alice
        SseConnection connAlice = registerAndSubscribe(topic, "alice");

        // 先订阅 alice 的 directedSink
        Sinks.Many<TopicMessage<Msg>> aliceDS = registry.getDirectedSink(connAlice.getConnectionId());
        AtomicReference<TopicMessage<Msg>> aliceReceived = subscribeSink(aliceDS);

        // 发布定向消息到不存在的 routingKey
        TopicMessage<Msg> msg = createDirectedMessage(topic, "hello nobody", Set.of("nonexistent"));
        assertDoesNotThrow(() -> manager.publish(msg), "无目标的定向投递不应抛异常");

        // alice 不应收到
        assertNull(aliceReceived.get(), "alice 不应收到发往 nonexistent 的消息");
    }

    // ==================== 双通道互斥测试 ====================

    @Test
    @DisplayName("双通道互斥 - 广播消息不走 directedSink，定向消息不走 topicSink")
    void testDualChannelMutualExclusion() {
        String topic = "test.topic";

        SseConnection conn = registerAndSubscribe(topic, "alice");

        Sinks.Many<TopicMessage<Msg>> topicSink = registry.getSink(topic);
        Sinks.Many<TopicMessage<Msg>> directedSink = registry.getDirectedSink(conn.getConnectionId());

        // 先订阅两个通道
        AtomicReference<TopicMessage<Msg>> broadcastReceived = subscribeSink(topicSink);
        AtomicReference<TopicMessage<Msg>> directedReceived = subscribeSink(directedSink);

        // 1. 广播消息
        TopicMessage<Msg> broadcastMsg = createBroadcastMessage(topic, "broadcast");
        manager.publish(broadcastMsg);

        // topicSink 应收到
        assertNotNull(broadcastReceived.get(), "topicSink 应收到广播消息");
        assertEquals("broadcast", ((MessageMap) broadcastReceived.get().getMsg()).get("content"));

        // directedSink 不应收到广播消息
        assertNull(directedReceived.get(),
                "directedSink 不应收到广播消息");

        // 2. 定向消息
        TopicMessage<Msg> directedMsg = createDirectedMessage(topic, "directed", Set.of("alice"));
        manager.publish(directedMsg);

        // directedSink 应收到
        assertNotNull(directedReceived.get(), "directedSink 应收到定向消息");
        assertEquals("directed", ((MessageMap) directedReceived.get().getMsg()).get("content"));
    }
}
