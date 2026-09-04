package cn.gmlee.tools.im.resume;

import cn.gmlee.tools.im.model.TopicMessage;
import cn.gmlee.tools.im.topic.TopicLifecycleListener;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 内存消息历史存储（开发/单机场景开箱即用）.
 * <p>
 * 每个 Topic 维护一个有界环形缓冲（{@link ArrayDeque} + 容量上限），
 * 超出容量时逐出最旧消息。适用于：
 * </p>
 * <ul>
 *   <li>STANDALONE 单机部署的断点续传</li>
 *   <li>开发/测试环境快速验证</li>
 *   <li>短时断线重连（秒级~分钟级，取决于消息速率与容量）</li>
 * </ul>
 *
 * <h3>局限性</h3>
 * <ul>
 *   <li>进程重启即丢失全部历史（重启后携带 Last-Event-ID 的重连会收到
 *       {@code event: resync} 信号）</li>
 *   <li>CLUSTER 多实例部署时各实例历史相互独立——客户端重连可能落到
 *       不同实例；生产集群请使用外部存储（Redis 等）实现
 *       {@link MessageHistoryStore}</li>
 * </ul>
 *
 * <h3>资源回收</h3>
 * <p>
 * 实现 {@link TopicLifecycleListener}，Topic 销毁时同步移除对应环形缓冲，
 * 防止动态高基数 Topic 场景下内存泄漏。
 * </p>
 *
 * <h3>优先级</h3>
 * <p>
 * {@link #getOrder()} 返回 {@link Integer#MAX_VALUE}（最低优先级）：
 * 存在自定义存储（如 Redis）且 {@code supports} 命中时，自定义存储优先生效，
 * 本实现作为兜底。
 * </p>
 *
 * @since 5.7.0
 */
public class InMemoryMessageHistoryStore implements MessageHistoryStore, TopicLifecycleListener {

    private final int capacityPerTopic;
    private final EventIdComparator comparator;
    private final ConcurrentHashMap<String, TopicRing> rings = new ConcurrentHashMap<>();

    /**
     * 创建内存历史存储（使用默认 ID 比较器）.
     *
     * @param capacityPerTopic 每 Topic 最大保留消息数（&gt; 0）
     */
    public InMemoryMessageHistoryStore(int capacityPerTopic) {
        this(capacityPerTopic, EventIdComparator.DEFAULT);
    }

    /**
     * 创建内存历史存储.
     *
     * @param capacityPerTopic 每 Topic 最大保留消息数（&gt; 0）
     * @param comparator       ID 顺序比较器
     */
    public InMemoryMessageHistoryStore(int capacityPerTopic, EventIdComparator comparator) {
        if (capacityPerTopic <= 0) {
            throw new IllegalArgumentException("capacityPerTopic 必须 > 0: " + capacityPerTopic);
        }
        this.capacityPerTopic = capacityPerTopic;
        this.comparator = comparator != null ? comparator : EventIdComparator.DEFAULT;
    }

    @Override
    public boolean supports(String topic) {
        return true;
    }

    @Override
    public Mono<Void> store(TopicMessage<?, ?> message) {
        return Mono.fromRunnable(() -> {
            if (message == null || message.getId() == null || message.getTopic() == null) {
                return;
            }
            rings.computeIfAbsent(message.getTopic(), t -> new TopicRing(capacityPerTopic))
                    .add(message, comparator);
        });
    }

    @Override
    public HistoryLoadResult loadSince(String topic, Serializable lastEventId) {
        TopicRing ring = rings.get(topic);
        List<TopicMessage<?, ?>> snapshot = ring != null
                ? ring.snapshotAfter(lastEventId, comparator)
                : null;
        if (snapshot == null) {
            // 无历史（逐出/重启后）：无法覆盖客户端位点 → 间隙
            return HistoryLoadResult.gap();
        }
        return HistoryLoadResult.of(Flux.fromIterable(snapshot));
    }

    /**
     * Topic 销毁时清理历史（防止内存泄漏）.
     *
     * @param topic Topic 名称
     */
    @Override
    public void onTopicDestroyed(String topic) {
        rings.remove(topic);
    }

    /**
     * 兜底存储：自定义存储优先.
     *
     * @return {@link Integer#MAX_VALUE}
     */
    @Override
    public int getOrder() {
        return Integer.MAX_VALUE;
    }

    /**
     * 单 Topic 有界环形缓冲.
     * <p>
     * 所有操作持有自身监视器锁；临界区仅内存拷贝，无 I/O，锁竞争开销可忽略。
     * </p>
     * <p>
     * 容忍乱序到达：逐出按到达顺序发生，但间隙判定基于「已逐出消息的最大 ID」
     * （{@code maxEvictedId}），快照按 ID 升序排序，与到达顺序无关。
     * </p>
     */
    private static final class TopicRing {

        private final ArrayDeque<TopicMessage<?, ?>> deque;
        private final int capacity;
        /** 已逐出消息的最大 ID（精确间隙判定）；无逐出时为 null */
        private Serializable maxEvictedId;

        TopicRing(int capacity) {
            this.capacity = capacity;
            this.deque = new ArrayDeque<>(Math.min(capacity, 64));
        }

        /**
         * 追加消息；超容量逐出最旧（按到达顺序），并记录逐出 ID 用于间隙判定.
         */
        synchronized void add(TopicMessage<?, ?> message, EventIdComparator comparator) {
            deque.addLast(message);
            if (deque.size() > capacity) {
                TopicMessage<?, ?> evicted = deque.pollFirst();
                Serializable id = evicted != null ? evicted.getId() : null;
                if (id != null && comparator.isAfter(id, maxEvictedId)) {
                    maxEvictedId = id;
                }
            }
        }

        /**
         * 快照水位线之后的全部消息（按 ID 升序）.
         *
         * @param watermark  客户端位点
         * @param comparator ID 比较器
         * @return 快照列表；缓冲为空或位点之后存在已逐出消息（历史间隙）时返回 null
         */
        synchronized List<TopicMessage<?, ?>> snapshotAfter(Serializable watermark,
                                                            EventIdComparator comparator) {
            if (deque.isEmpty()) {
                return null;
            }
            if (maxEvictedId != null && comparator.isAfter(maxEvictedId, watermark)) {
                // 客户端位点之后存在已逐出消息 → 无法完整覆盖 → 间隙
                return null;
            }
            List<TopicMessage<?, ?>> snapshot = new ArrayList<>(deque.size());
            for (TopicMessage<?, ?> m : deque) {
                if (comparator.isAfter(m.getId(), watermark)) {
                    snapshot.add(m);
                }
            }
            // 乱序到达时按到达顺序迭代不满足升序契约 → 显式排序（TimSort 对已有序输入 O(n)）
            snapshot.sort(byId(comparator));
            return snapshot;
        }

        /**
         * 由 {@link EventIdComparator} 派生的升序比较器.
         * <p>
         * 不可比较的 ID 对视为相等（保持到达顺序），避免违反
         * {@link Comparator} 契约（宁重勿漏，排序不抛异常）。
         * </p>
         */
        private static Comparator<TopicMessage<?, ?>> byId(EventIdComparator comparator) {
            return (a, b) -> {
                boolean after = comparator.isAfter(a.getId(), b.getId());
                boolean before = comparator.isAfter(b.getId(), a.getId());
                if (after && !before) {
                    return 1;
                }
                if (before && !after) {
                    return -1;
                }
                return 0;
            };
        }
    }
}
