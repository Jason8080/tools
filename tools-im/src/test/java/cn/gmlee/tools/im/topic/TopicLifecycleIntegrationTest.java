package cn.gmlee.tools.im.topic;

import cn.gmlee.tools.im.conf.EndpointProperties;
import cn.gmlee.tools.im.conf.SseProperties;
import cn.gmlee.tools.im.endpoint.EndpointRegistry;
import cn.gmlee.tools.im.model.EndpointMode;
import cn.gmlee.tools.im.sse.SseConnectionRegistry;
import cn.gmlee.tools.im.sse.backpressure.BackpressureStrategyResolver;
import cn.gmlee.tools.im.sse.backpressure.DropOldestBackpressureStrategy;
import cn.gmlee.tools.im.sse.cleanup.ConnectionReaper;
import cn.gmlee.tools.im.sse.metrics.NoOpSseMetrics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * TopicLifecycleManager 集成测试.
 * <p>
 * 验证 TopicLifecycleManager 与 EndpointRegistry、ConnectionReaper 的集成。
 * </p>
 */
class TopicLifecycleIntegrationTest {

    private TopicRegistry topicRegistry;
    private TopicFactory topicFactory;
    private SseProperties sseProperties;
    private EndpointRegistry endpointRegistry;
    private ConnectionReaper connectionReaper;
    private TopicLifecycleManager lifecycleManager;

    @BeforeEach
    void setUp() {
        // 创建 Mock 对象
        topicRegistry = mock(TopicRegistry.class);
        topicFactory = mock(TopicFactory.class);
        SseConnectionRegistry connectionRegistry = mock(SseConnectionRegistry.class);
        BackpressureStrategyResolver strategyResolver = topic -> new DropOldestBackpressureStrategy();

        // 配置
        sseProperties = new SseProperties();
        sseProperties.getCleanup().setEmptyTopicTtl(Duration.ofMillis(100));

        // 创建真实对象
        when(connectionRegistry.getCounter()).thenReturn(new cn.gmlee.tools.im.sse.internal.ConnectionCounter());
        connectionReaper = new ConnectionReaper(connectionRegistry, NoOpSseMetrics.INSTANCE, sseProperties);
        endpointRegistry = new EndpointRegistry();

        // 创建生命周期管理器
        lifecycleManager = new DefaultTopicLifecycleManager(topicRegistry, topicFactory, sseProperties);

        // 注入依赖
        endpointRegistry.setTopicLifecycleManager(lifecycleManager);
        connectionReaper.setTopicLifecycleManager(lifecycleManager);
    }

    @Test
    @DisplayName("端点注册 - Topic 引用计数递增")
    void testEndpointRegister_topicRefCountIncrement() {
        EndpointProperties props = new EndpointProperties();
        props.setPath("/api/test");
        props.setTopic("test.topic");
        props.setMode(EndpointMode.PUSH);

        endpointRegistry.register(props);

        assertEquals(TopicState.CREATED, lifecycleManager.getState("test.topic"));
        assertEquals(1, lifecycleManager.getRefCount("test.topic"));
    }

    @Test
    @DisplayName("多个端点共享同一 Topic - 引用计数累加")
    void testMultipleEndpoints_sameTopic() {
        EndpointProperties props1 = new EndpointProperties();
        props1.setPath("/api/test1");
        props1.setTopic("test.topic");
        props1.setMode(EndpointMode.PUSH);

        EndpointProperties props2 = new EndpointProperties();
        props2.setPath("/api/test2");
        props2.setTopic("test.topic");
        props2.setMode(EndpointMode.PULL);

        endpointRegistry.register(props1);
        endpointRegistry.register(props2);

        assertEquals(2, lifecycleManager.getRefCount("test.topic"));
    }

    @Test
    @DisplayName("端点注销 - Topic 引用计数递减")
    void testEndpointUnregister_topicRefCountDecrement() {
        EndpointProperties props = new EndpointProperties();
        props.setPath("/api/test");
        props.setTopic("test.topic");
        props.setMode(EndpointMode.PUSH);

        endpointRegistry.register(props);
        assertEquals(1, lifecycleManager.getRefCount("test.topic"));

        endpointRegistry.unregister("/api/test");
        assertEquals(0, lifecycleManager.getRefCount("test.topic"));
    }

    @Test
    @DisplayName("端点更新 - Topic 变更时调整引用计数")
    void testEndpointUpdate_topicChange() {
        EndpointProperties props = new EndpointProperties();
        props.setPath("/api/test");
        props.setTopic("topic.old");
        props.setMode(EndpointMode.PUSH);

        endpointRegistry.register(props);
        assertEquals(1, lifecycleManager.getRefCount("topic.old"));

        // 更新 Topic
        EndpointProperties newProps = new EndpointProperties();
        newProps.setPath("/api/test");
        newProps.setTopic("topic.new");
        newProps.setMode(EndpointMode.PUSH);

        endpointRegistry.register(newProps);

        assertEquals(0, lifecycleManager.getRefCount("topic.old"));
        assertEquals(1, lifecycleManager.getRefCount("topic.new"));
    }

    @Test
    @DisplayName("ConnectionReaper - 自动清理空闲 Topic")
    void testConnectionReaper_autoCleanup() throws InterruptedException {
        EndpointProperties props = new EndpointProperties();
        props.setPath("/api/test");
        props.setTopic("test.topic");
        props.setMode(EndpointMode.PUSH);

        // 注册并注销端点
        endpointRegistry.register(props);
        endpointRegistry.unregister("/api/test");

        assertEquals(0, lifecycleManager.getRefCount("test.topic"));

        // 等待超过 TTL
        Thread.sleep(150);

        // 触发清理
        int cleaned = lifecycleManager.cleanup();

        assertEquals(1, cleaned);
        assertNull(lifecycleManager.getState("test.topic"));
        verify(topicFactory).cleanupTopicResources("test.topic");
    }

    @Test
    @DisplayName("强制销毁 - 忽略引用计数")
    void testForceDestroy_ignoreRefCount() {
        EndpointProperties props = new EndpointProperties();
        props.setPath("/api/test");
        props.setTopic("test.topic");
        props.setMode(EndpointMode.PUSH);

        endpointRegistry.register(props);
        assertEquals(1, lifecycleManager.getRefCount("test.topic"));

        // 强制销毁
        boolean destroyed = lifecycleManager.destroy("test.topic");

        assertTrue(destroyed);
        assertEquals(TopicState.DESTROYED, lifecycleManager.getState("test.topic"));
        verify(topicFactory).cleanupTopicResources("test.topic");
        verify(topicRegistry).destroyTopic("test.topic");
    }

    @Test
    @DisplayName("生命周期监听器 - 端点注册触发回调")
    void testLifecycleListener_endpointRegister() {
        TopicLifecycleListener listener = mock(TopicLifecycleListener.class);
        lifecycleManager.addListener(listener);

        EndpointProperties props = new EndpointProperties();
        props.setPath("/api/test");
        props.setTopic("test.topic");
        props.setMode(EndpointMode.PUSH);

        endpointRegistry.register(props);

        verify(listener).onTopicCreated("test.topic");
    }

    @Test
    @DisplayName("生命周期监听器 - 强制销毁触发回调")
    void testLifecycleListener_forceDestroy() {
        TopicLifecycleListener listener = mock(TopicLifecycleListener.class);
        lifecycleManager.addListener(listener);

        EndpointProperties props = new EndpointProperties();
        props.setPath("/api/test");
        props.setTopic("test.topic");
        props.setMode(EndpointMode.PUSH);

        endpointRegistry.register(props);
        lifecycleManager.destroy("test.topic");

        verify(listener).onTopicDestroying("test.topic");
        verify(listener).onTopicDestroyed("test.topic");
    }

    @Test
    @DisplayName("完整流程 - 注册 → 使用 → 注销 → 清理")
    void testCompleteLifecycle() throws InterruptedException {
        TopicLifecycleListener listener = mock(TopicLifecycleListener.class);
        lifecycleManager.addListener(listener);

        // 1. 注册端点
        EndpointProperties props = new EndpointProperties();
        props.setPath("/api/test");
        props.setTopic("test.topic");
        props.setMode(EndpointMode.PUSH);

        endpointRegistry.register(props);
        assertEquals(TopicState.CREATED, lifecycleManager.getState("test.topic"));
        assertEquals(1, lifecycleManager.getRefCount("test.topic"));
        verify(listener).onTopicCreated("test.topic");

        // 2. 使用 Topic（模拟）
        // ... 实际使用中会创建 Publisher/Repeater/Subscriber

        // 3. 注销端点
        endpointRegistry.unregister("/api/test");
        assertEquals(0, lifecycleManager.getRefCount("test.topic"));

        // 4. 等待自动清理
        Thread.sleep(150);
        int cleaned = lifecycleManager.cleanup();

        assertEquals(1, cleaned);
        assertNull(lifecycleManager.getState("test.topic"));
        verify(listener).onTopicDestroying("test.topic");
        verify(listener).onTopicDestroyed("test.topic");
        verify(topicFactory).cleanupTopicResources("test.topic");
        verify(topicRegistry).destroyTopic("test.topic");
    }
}
