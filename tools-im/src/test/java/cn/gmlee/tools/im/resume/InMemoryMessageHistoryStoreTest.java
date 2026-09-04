package cn.gmlee.tools.im.resume;

import cn.gmlee.tools.im.model.Msg;
import cn.gmlee.tools.im.model.TopicMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link InMemoryMessageHistoryStore} 内存历史存储测试.
 * <p>
 * 覆盖：基础存取、容量逐出与精确间隙判定、乱序到达、资源回收。
 * </p>
 *
 * @since 5.7.0
 */
@DisplayName("InMemoryMessageHistoryStore 内存历史存储")
class InMemoryMessageHistoryStoreTest {

    private static final String TOPIC = "im.chat";

    /** 测试消息载荷 */
    private static final class TestMsg implements Msg {
    }

    @Test
    @DisplayName("容量必须为正数")
    void capacityValidation() {
        assertThrows(IllegalArgumentException.class, () -> new InMemoryMessageHistoryStore(0));
        assertThrows(IllegalArgumentException.class, () -> new InMemoryMessageHistoryStore(-1));
    }

    @Test
    @DisplayName("supports 全部 Topic，Order 为兜底优先级")
    void supportsAndOrder() {
        InMemoryMessageHistoryStore store = new InMemoryMessageHistoryStore(10);
        assertTrue(store.supports("any.topic"));
        assertEquals(Integer.MAX_VALUE, store.getOrder());
    }

    @Test
    @DisplayName("基础存取：升序回放位点之后的消息")
    void storeAndLoadAscending() {
        InMemoryMessageHistoryStore store = new InMemoryMessageHistoryStore(100);
        for (long id = 1; id <= 5; id++) {
            store.store(env(id)).block();
        }

        assertEquals(List.of(3L, 4L, 5L), ids(store.loadSince(TOPIC, 2L).messages()));
        assertEquals(List.of(1L, 2L, 3L, 4L, 5L), ids(store.loadSince(TOPIC, 0L).messages()));
        assertTrue(store.loadSince(TOPIC, 5L).messages().collectList().block().isEmpty(),
                "位点即最新时回放为空（非间隙）");
        assertFalse(store.loadSince(TOPIC, 2L).gapKnown());
    }

    @Test
    @DisplayName("无历史（未知 Topic）：报告间隙")
    void unknownTopicReportsGap() {
        InMemoryMessageHistoryStore store = new InMemoryMessageHistoryStore(10);
        assertTrue(store.loadSince("no.such.topic", 1L).gapKnown());
    }

    @Test
    @DisplayName("逐出后精确间隙判定：位点覆盖完整则无间隙")
    void evictionGapDetectionIsExact() {
        InMemoryMessageHistoryStore store = new InMemoryMessageHistoryStore(3);
        // 写入 1..5，环形缓冲保留 [3,4,5]，已逐出最大 ID = 2
        for (long id = 1; id <= 5; id++) {
            store.store(env(id)).block();
        }

        // 位点 2 = 已逐出最大 ID：其后消息全部在缓冲中 → 完整覆盖，非间隙
        HistoryLoadResult covered = store.loadSince(TOPIC, 2L);
        assertFalse(covered.gapKnown(), "位点 >= 已逐出最大 ID 时应可完整覆盖");
        assertEquals(List.of(3L, 4L, 5L), ids(covered.messages()));

        // 位点 1 < 已逐出最大 ID 2：ID 2 已丢失 → 间隙
        assertTrue(store.loadSince(TOPIC, 1L).gapKnown());
        assertTrue(store.loadSince(TOPIC, 0L).gapKnown());
    }

    @Test
    @DisplayName("乱序到达：快照仍按 ID 升序")
    void outOfOrderArrivalStillSorted() {
        InMemoryMessageHistoryStore store = new InMemoryMessageHistoryStore(100);
        // 到达顺序 3,1,2,5,4（框架异步写入不保证顺序）
        for (long id : new long[]{3, 1, 2, 5, 4}) {
            store.store(env(id)).block();
        }
        assertEquals(List.of(1L, 2L, 3L, 4L, 5L), ids(store.loadSince(TOPIC, 0L).messages()));
        assertEquals(List.of(3L, 4L, 5L), ids(store.loadSince(TOPIC, 2L).messages()));
    }

    @Test
    @DisplayName("乱序到达 + 逐出：间隙判定基于已逐出最大 ID")
    void outOfOrderEvictionGapDetection() {
        InMemoryMessageHistoryStore store = new InMemoryMessageHistoryStore(3);
        // 到达顺序 1,5,2,4,3：逐出 1、5（按到达顺序），保留 [2,4,3]，已逐出最大 ID = 5
        for (long id : new long[]{1, 5, 2, 4, 3}) {
            store.store(env(id)).block();
        }

        assertTrue(store.loadSince(TOPIC, 4L).gapKnown(), "ID 5 已被逐出且晚于位点 4 → 间隙");
        assertTrue(store.loadSince(TOPIC, 0L).gapKnown());

        // 位点 5 = 已逐出最大 ID：其后无消息丢失 → 完整覆盖（空回放）
        HistoryLoadResult covered = store.loadSince(TOPIC, 5L);
        assertFalse(covered.gapKnown());
        assertTrue(covered.messages().collectList().block().isEmpty());
    }

    @Test
    @DisplayName("无 ID / 无 Topic 的消息被跳过")
    void skipsMessagesWithoutIdOrTopic() {
        InMemoryMessageHistoryStore store = new InMemoryMessageHistoryStore(10);
        store.store(env(null)).block();

        TopicMessage<Serializable, Msg> noTopic = new TopicMessage<>();
        noTopic.setId(1L);
        noTopic.setTopic(null);
        store.store(noTopic).block();

        store.store(null).block();

        assertTrue(store.loadSince(TOPIC, 0L).gapKnown(), "缓冲应为空（全部跳过）");
    }

    @Test
    @DisplayName("Topic 销毁时清理环形缓冲（防内存泄漏）")
    void topicDestroyedReleasesRing() {
        InMemoryMessageHistoryStore store = new InMemoryMessageHistoryStore(10);
        store.store(env(1L)).block();
        assertEquals(List.of(1L), ids(store.loadSince(TOPIC, 0L).messages()));

        store.onTopicDestroyed(TOPIC);

        assertTrue(store.loadSince(TOPIC, 0L).gapKnown(), "Topic 销毁后历史不可用（间隙）");

        // 销毁后可重新积累（新 Topic 生命周期）
        store.store(env(9L)).block();
        assertEquals(List.of(9L), ids(store.loadSince(TOPIC, 0L).messages()));
    }

    @Test
    @DisplayName("多 Topic 相互隔离")
    void topicsAreIsolated() {
        InMemoryMessageHistoryStore store = new InMemoryMessageHistoryStore(10);
        TopicMessage<Serializable, Msg> a = env(1L);
        a.setTopic("topic.a");
        TopicMessage<Serializable, Msg> b = env(2L);
        b.setTopic("topic.b");
        store.store(a).block();
        store.store(b).block();

        assertEquals(List.of(1L), ids(store.loadSince("topic.a", 0L).messages()));
        assertEquals(List.of(2L), ids(store.loadSince("topic.b", 0L).messages()));

        store.onTopicDestroyed("topic.a");
        assertTrue(store.loadSince("topic.a", 0L).gapKnown());
        assertEquals(List.of(2L), ids(store.loadSince("topic.b", 0L).messages()),
                "销毁 topic.a 不应影响 topic.b");
    }

    // ==================== 测试基础设施 ====================

    private static TopicMessage<Serializable, Msg> env(Serializable id) {
        TopicMessage<Serializable, Msg> m = new TopicMessage<>();
        m.setId(id);
        m.setTopic(TOPIC);
        m.setMsg(new TestMsg());
        return m;
    }

    private static List<Object> ids(reactor.core.publisher.Flux<TopicMessage<?, ?>> messages) {
        return messages.map(m -> (Object) m.getId()).collectList().block();
    }
}
