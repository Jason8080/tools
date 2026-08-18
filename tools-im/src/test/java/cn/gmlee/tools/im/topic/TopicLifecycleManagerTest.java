package cn.gmlee.tools.im.topic;

import cn.gmlee.tools.im.conf.SseProperties;
import cn.gmlee.tools.im.model.TopicState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * TopicLifecycleManager 单元测试.
 * <p>
 * 验证 Topic 生命周期管理的核心功能：
 * <ul>
 *   <li>引用计数管理（acquire/release）</li>
 *   <li>状态转换（CREATED → ACTIVE → DESTROYING → DESTROYED）</li>
 *   <li>Topic 销毁（destroy）</li>
 *   <li>自动清理（cleanup）</li>
 *   <li>生命周期监听器</li>
 * </ul>
 * </p>
 */
class TopicLifecycleManagerTest {

    private TopicRegistry topicRegistry;
    private TopicResourceFactory topicResourceFactory;
    private SseProperties sseProperties;
    private DefaultTopicLifecycleManager manager;

    @BeforeEach
    void setUp() {
        topicRegistry = mock(TopicRegistry.class);
        topicResourceFactory = mock(TopicResourceFactory.class);
        sseProperties = new SseProperties();
        sseProperties.getCleanup().setEmptyTopicTtl(Duration.ofMillis(100));

        manager = new DefaultTopicLifecycleManager(topicRegistry, topicResourceFactory, sseProperties);
    }

    @Test
    @DisplayName("acquire - 新 Topic 初始化为 CREATED 状态，引用计数为 1")
    void testAcquire_newTopic() {
        String topic = "test.topic";

        manager.acquire(topic);

        assertEquals(TopicState.CREATED, manager.getState(topic));
        assertEquals(1, manager.getRefCount(topic));
        assertTrue(manager.getAllTopics().contains(topic));
    }

    @Test
    @DisplayName("acquire - 已存在 Topic 引用计数递增")
    void testAcquire_existingTopic() {
        String topic = "test.topic";

        manager.acquire(topic);
        manager.acquire(topic);
        manager.acquire(topic);

        assertEquals(3, manager.getRefCount(topic));
    }

    @Test
    @DisplayName("acquire - 已销毁 Topic 重新激活")
    void testAcquire_destroyedTopic() {
        String topic = "test.topic";

        manager.acquire(topic);
        manager.destroy(topic);
        assertEquals(TopicState.DESTROYED, manager.getState(topic));

        // 重新激活
        manager.acquire(topic);
        assertEquals(TopicState.CREATED, manager.getState(topic));
        assertEquals(1, manager.getRefCount(topic));
    }

    @Test
    @DisplayName("release - 引用计数递减")
    void testRelease() {
        String topic = "test.topic";

        manager.acquire(topic);   // refCount = 1
        manager.acquire(topic);   // refCount = 2

        manager.release(topic);   // refCount = 1
        assertEquals(1, manager.getRefCount(topic));

        manager.release(topic);   // refCount = 0
        assertEquals(0, manager.getRefCount(topic));
    }

    @Test
    @DisplayName("release - 引用计数不为负数")
    void testRelease_notNegative() {
        String topic = "test.topic";

        manager.acquire(topic);
        manager.release(topic);
        manager.release(topic); // 多余的 release

        assertEquals(0, manager.getRefCount(topic));
    }

    @Test
    @DisplayName("destroy - 正常销毁流程")
    void testDestroy_success() {
        String topic = "test.topic";
        manager.acquire(topic);

        boolean destroyed = manager.destroy(topic);

        assertTrue(destroyed);
        assertEquals(TopicState.DESTROYED, manager.getState(topic));
        verify(topicResourceFactory).cleanupResources(topic);
        verify(topicRegistry).destroyTopic(topic);
    }

    @Test
    @DisplayName("destroy - Topic 不存在返回 false")
    void testDestroy_notExist() {
        boolean destroyed = manager.destroy("non.exist");

        assertFalse(destroyed);
        verify(topicResourceFactory, never()).cleanupResources(anyString());
        verify(topicRegistry, never()).destroyTopic(anyString());
    }

    @Test
    @DisplayName("destroy - 防止并发销毁")
    void testDestroy_concurrentProtection() {
        String topic = "test.topic";
        manager.acquire(topic);

        // 第一次销毁成功
        boolean firstDestroy = manager.destroy(topic);
        assertTrue(firstDestroy);

        // 第二次销毁失败（已销毁）
        boolean secondDestroy = manager.destroy(topic);
        assertFalse(secondDestroy);

        // 只调用一次清理
        verify(topicResourceFactory, times(1)).cleanupResources(topic);
    }

    @Test
    @DisplayName("getState - 不存在返回 null")
    void testGetState_notExist() {
        assertNull(manager.getState("non.exist"));
    }

    @Test
    @DisplayName("getRefCount - 不存在返回 0")
    void testGetRefCount_notExist() {
        assertEquals(0, manager.getRefCount("non.exist"));
    }

    @Test
    @DisplayName("getAllTopics - 返回所有 Topic")
    void testGetAllTopics() {
        manager.acquire("topic1");
        manager.acquire("topic2");
        manager.acquire("topic3");

        Set<String> topics = manager.getAllTopics();

        assertEquals(3, topics.size());
        assertTrue(topics.contains("topic1"));
        assertTrue(topics.contains("topic2"));
        assertTrue(topics.contains("topic3"));
    }

    @Test
    @DisplayName("getTopicsByState - 按状态过滤")
    void testGetTopicsByState() {
        manager.acquire("topic1");
        manager.acquire("topic2");
        manager.destroy("topic2");

        Set<String> createdTopics = manager.getTopicsByState(TopicState.CREATED);
        Set<String> destroyedTopics = manager.getTopicsByState(TopicState.DESTROYED);

        assertEquals(1, createdTopics.size());
        assertTrue(createdTopics.contains("topic1"));

        assertEquals(1, destroyedTopics.size());
        assertTrue(destroyedTopics.contains("topic2"));
    }

    @Test
    @DisplayName("cleanup - 清理空闲超时的 Topic")
    void testCleanup_idleTimeout() throws InterruptedException {
        String topic = "test.topic";
        manager.acquire(topic);
        manager.release(topic); // refCount = 0

        // 等待超过 TTL
        Thread.sleep(150);

        // 第一次 cleanup：标记为 DESTROYED（可观测性：getState 可区分"从未存在"与"最近被销毁"）
        int cleaned = manager.cleanup();
        assertEquals(1, cleaned);
        assertEquals(TopicState.DESTROYED, manager.getState(topic));

        // 第二次 cleanup：移除 DESTROYED 条目
        manager.cleanup();
        assertNull(manager.getState(topic));
        verify(topicResourceFactory).cleanupResources(topic);
    }

    @Test
    @DisplayName("cleanup - 不清理引用计数大于 0 的 Topic")
    void testCleanup_refCountGreaterThanZero() throws InterruptedException {
        String topic = "test.topic";
        manager.acquire(topic);
        manager.acquire(topic);
        manager.release(topic); // refCount = 1

        Thread.sleep(150);

        int cleaned = manager.cleanup();

        assertEquals(0, cleaned);
        assertNotNull(manager.getState(topic));
        verify(topicResourceFactory, never()).cleanupResources(topic);
    }

    @Test
    @DisplayName("cleanup - 清理已销毁的 Topic 记录")
    void testCleanup_destroyedTopic() {
        String topic = "test.topic";
        manager.acquire(topic);
        manager.destroy(topic);

        // cleanup 会移除 DESTROYED 状态的 Topic
        int cleaned = manager.cleanup();

        assertNull(manager.getState(topic));
    }

    @Test
    @DisplayName("监听器 - onTopicCreated 回调")
    void testListener_onTopicCreated() {
        TopicLifecycleListener listener = mock(TopicLifecycleListener.class);
        manager.addListener(listener);

        manager.acquire("test.topic");

        verify(listener).onTopicCreated("test.topic");
    }

    @Test
    @DisplayName("监听器 - onTopicDestroying 和 onTopicDestroyed 回调")
    void testListener_onTopicDestroyed() {
        TopicLifecycleListener listener = mock(TopicLifecycleListener.class);
        manager.addListener(listener);

        manager.acquire("test.topic");
        manager.destroy("test.topic");

        verify(listener).onTopicDestroying("test.topic");
        verify(listener).onTopicDestroyed("test.topic");
    }

    @Test
    @DisplayName("监听器 - removeListener")
    void testListener_remove() {
        TopicLifecycleListener listener = mock(TopicLifecycleListener.class);
        manager.addListener(listener);
        manager.removeListener(listener);

        manager.acquire("test.topic");

        verify(listener, never()).onTopicCreated(anyString());
    }

    @Test
    @DisplayName("监听器 - 异常不影响其他监听器")
    void testListener_exception() {
        TopicLifecycleListener listener1 = mock(TopicLifecycleListener.class);
        doThrow(new RuntimeException("test exception")).when(listener1).onTopicCreated(anyString());

        TopicLifecycleListener listener2 = mock(TopicLifecycleListener.class);

        manager.addListener(listener1);
        manager.addListener(listener2);

        manager.acquire("test.topic");

        // listener2 应该正常执行
        verify(listener2).onTopicCreated("test.topic");
    }
}
