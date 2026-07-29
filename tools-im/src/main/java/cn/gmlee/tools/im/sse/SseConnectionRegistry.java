package cn.gmlee.tools.im.sse;

import cn.gmlee.tools.im.core.Msg;
import cn.gmlee.tools.im.core.TopicMessage;
import cn.gmlee.tools.im.sse.backpressure.BackpressureStrategy;
import cn.gmlee.tools.im.sse.backpressure.BackpressureStrategyResolver;
import cn.gmlee.tools.im.sse.internal.ConnectionCounter;
import cn.gmlee.tools.im.conf.SseProperties;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Sinks;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SSE 连接注册表.
 * <p>
 * 内部组件，负责线程安全地存储所有连接、Sink 和计数器。
 * 计数器操作委托给 {@link ConnectionCounter}，连接存储使用 ConcurrentHashMap。
 * </p>
 *
 * <h3>设计要点</h3>
 * <ul>
 *   <li><b>计数器管理</b>：委托给 {@link ConnectionCounter}，保证 CAS 操作的原子性</li>
 *   <li><b>连接存储</b>：connections 按 connectionId 索引，支持 O(1) 查找</li>
 *   <li><b>Topic 索引</b>：topicConnections 按 Topic 索引连接 ID 集合，支持快速按 Topic 清理</li>
 *   <li><b>Sink 管理</b>：topicSinks 使用 compute() 原子操作</li>
 * </ul>
 */
@Slf4j
public class SseConnectionRegistry {

    private final SseProperties properties;
    private final BackpressureStrategyResolver strategyResolver;

    /**
     * 连接计数器管理器
     */
    @Getter
    private final ConnectionCounter counter;

    /**
     * Topic -> Sink 映射
     */
    private final ConcurrentHashMap<String, Sinks.Many<TopicMessage<Msg>>> topicSinks = new ConcurrentHashMap<>();

    /**
     * connectionId -> 连接记录
     */
    private final ConcurrentHashMap<String, SseConnection> connections = new ConcurrentHashMap<>();

    /**
     * Topic -> connectionId 集合
     */
    private final ConcurrentHashMap<String, Set<String>> topicConnections = new ConcurrentHashMap<>();

    /**
     * Topic -> 首次变空时间（用于 TTL 清理）
     */
    private final ConcurrentHashMap<String, Long> emptySince = new ConcurrentHashMap<>();

    /**
     * 是否正在关闭.
     * <p>
     * 由 {@link #closeAll()} 在清理开始前设置，阻止 subscribe() 创建新 Sink。
     * volatile 保证跨线程可见性。
     * </p>
     */
    private volatile boolean closing = false;

    /**
     * 创建连接注册表.
     *
     * @param properties       配置
     * @param strategyResolver 背压策略解析器
     */
    public SseConnectionRegistry(SseProperties properties, BackpressureStrategyResolver strategyResolver) {
        this.properties = properties;
        this.strategyResolver = strategyResolver;
        this.counter = new ConnectionCounter();
    }

    /**
     * 注册连接.
     *
     * @param conn 连接记录
     */
    public void register(SseConnection conn) {
        connections.put(conn.getConnectionId(), conn);
        topicConnections.computeIfAbsent(conn.getTopic(), k -> ConcurrentHashMap.newKeySet())
                .add(conn.getConnectionId());
        // 清除 emptySince 标记（Topic 有新连接了）
        emptySince.remove(conn.getTopic());
    }

    /**
     * 注销连接（仅从 maps 移除，不操作计数器）.
     *
     * @param conn 连接记录
     */
    public void unregister(SseConnection conn) {
        connections.remove(conn.getConnectionId());
        Set<String> connIds = topicConnections.get(conn.getTopic());
        if (connIds != null) {
            connIds.remove(conn.getConnectionId());
        }
    }

    /**
     * 统一清理连接.
     * <p>
     * 执行完整的连接清理流程：
     * <ol>
     *   <li>尝试递减计数器（通过 ConnectionCounter.tryDecrement）</li>
     *   <li>从 maps 中移除连接</li>
     *   <li>如果 Topic 为空，清理 Sink 和连接集合</li>
     *   <li>清理该 Topic 的指标</li>
     * </ol>
     * </p>
     *
     * @param conn    连接记录
     * @param metrics 指标收集器
     * @return 如果执行了计数器递减返回 true
     */
    public boolean cleanupConnection(SseConnection conn, SseMetrics metrics) {
        // 1. 尝试递减计数器（CAS 保证仅一次）
        boolean decremented = counter.tryDecrement(conn);
        if (decremented) {
            // 2. 从 maps 中移除
            unregister(conn);
            // 3. 清理空 Topic
            if (cleanupIfEmpty(conn.getTopic())) {
                metrics.cleanupTopic(conn.getTopic());
            }
        }
        return decremented;
    }

    /**
     * 获取或创建 Sink.
     * <p>
     * 使用 computeIfAbsent 保证原子性，同一 Topic 的所有并发请求只会创建一个 Sink。
     * 关闭中（{@link #closing} = true）时返回 null，阻止创建新 Sink。
     * </p>
     *
     * @param topic Topic 名称
     * @return 对应的 Sink，关闭中返回 null
     */
    public Sinks.Many<TopicMessage<Msg>> getOrCreateSink(String topic) {
        if (closing) {
            return null;
        }
        return topicSinks.computeIfAbsent(topic, t -> {
            BackpressureStrategy strategy = strategyResolver.resolve(t);
            int bufferSize = properties.getBackpressure().getDefaultBufferSize();
            log.debug("[Sink] 创建: topic={}, strategy={}, bufferSize={}", t, strategy.name(), bufferSize);
            return strategy.createSink(bufferSize);
        });
    }

    /**
     * 原子清理空 Topic.
     * <p>
     * 使用 ConcurrentHashMap.compute() 持有 bin 锁，保证检查-删除的原子性。
     * 仅当 Topic 计数为 0 时才清理 Sink 和连接集合。
     * </p>
     * <p>
     * 注意：保留 topicCounts 计数器条目（值为 0），避免与并发 subscribe() 的
     * computeIfAbsent() 产生竞态导致计数器漂移。代价是每个历史 Topic 保留一个
     * AtomicInteger（~40 字节），对 IM 场景可忽略。
     * </p>
     *
     * @param topic Topic 名称
     * @return 如果执行了清理返回 true
     */
    public boolean cleanupIfEmpty(String topic) {
        final boolean[] cleaned = {false};
        counter.getTopicCounts().compute(topic, (k, v) -> {
            if (v == null || v.get() > 0) {
                return v;
            }
            // 计数为 0，清理 Sink 和连接集合，但保留计数器条目
            topicSinks.remove(k);
            topicConnections.remove(k);
            emptySince.remove(k);
            cleaned[0] = true;
            log.debug("[Topic] 清理空 Topic: {}", k);
            return v; // 保留计数器，避免与并发 subscribe 的竞态
        });
        return cleaned[0];
    }

    /**
     * 基于 TTL 清理空 Topic.
     * <p>
     * 仅当 Topic 为空超过指定 TTL 时才清理，防止抖动。
     * 保留 topicCounts 计数器条目，避免与并发 subscribe() 的竞态。
     * </p>
     *
     * @param emptyTopicTtlMillis 空 Topic TTL（毫秒）
     * @return 被清理的 Topic 名称列表
     */
    public List<String> cleanupEmptyTopicsByTtl(long emptyTopicTtlMillis) {
        long now = System.currentTimeMillis();
        List<String> cleaned = new ArrayList<>();

        for (Map.Entry<String, java.util.concurrent.atomic.AtomicInteger> entry : counter.getTopicCounts().entrySet()) {
            String topic = entry.getKey();
            java.util.concurrent.atomic.AtomicInteger count = entry.getValue();

            if (count.get() == 0) {
                // 首次标记为空
                emptySince.putIfAbsent(topic, now);
                Long since = emptySince.get(topic);
                if (since != null && (now - since) > emptyTopicTtlMillis) {
                    // 超过 TTL，清理 Sink 和连接集合（保留计数器）
                    final boolean[] didClean = {false};
                    counter.getTopicCounts().compute(topic, (k, v) -> {
                        if (v != null && v.get() == 0) {
                            topicSinks.remove(k);
                            topicConnections.remove(k);
                            didClean[0] = true;
                        }
                        return v; // 保留计数器
                    });
                    if (didClean[0]) {
                        emptySince.remove(topic);
                        cleaned.add(topic);
                    }
                }
            } else {
                // Topic 有连接，清除空标记
                emptySince.remove(topic);
            }
        }

        return cleaned;
    }

    /**
     * 压缩长期为空的 Topic 计数器条目.
     * <p>
     * 移除同时满足以下条件的条目：
     * <ol>
     *   <li>计数器值为 0</li>
     *   <li>无对应 Sink（已在 {@link #cleanupIfEmpty} 中清理）</li>
     *   <li>空状态持续时间超过 compactTtlMillis</li>
     * </ol>
     * </p>
     *
     * @param compactTtlMillis 压缩 TTL（毫秒）
     * @return 被移除的 Topic 数量
     */
    public int compactTopicCounts(long compactTtlMillis) {
        if (compactTtlMillis <= 0) {
            return 0;
        }
        long now = System.currentTimeMillis();
        int compacted = 0;

        for (Map.Entry<String, java.util.concurrent.atomic.AtomicInteger> entry : counter.getTopicCounts().entrySet()) {
            String topic = entry.getKey();
            java.util.concurrent.atomic.AtomicInteger count = entry.getValue();

            if (count.get() != 0) {
                continue;
            }
            Long since = emptySince.get(topic);
            if (since == null || (now - since) < compactTtlMillis) {
                continue;
            }
            // 原子检查并移除
            final boolean[] removed = {false};
            counter.getTopicCounts().compute(topic, (k, v) -> {
                if (v != null && v.get() == 0 && !topicSinks.containsKey(k)) {
                    removed[0] = true;
                    return null; // 移除条目
                }
                return v;
            });
            if (removed[0]) {
                emptySince.remove(topic);
                compacted++;
                log.debug("[Topic] 压缩计数器: {}", topic);
            }
        }

        return compacted;
    }

    /**
     * 获取所有连接快照.
     *
     * @return 连接列表快照
     */
    public List<SseConnection> snapshotConnections() {
        return new ArrayList<>(connections.values());
    }

    /**
     * 获取指定 Topic 的所有连接.
     *
     * @param topic Topic 名称
     * @return 连接列表
     */
    public List<SseConnection> getConnectionsByTopic(String topic) {
        Set<String> connIds = topicConnections.get(topic);
        if (connIds == null || connIds.isEmpty()) {
            return Collections.emptyList();
        }
        return connIds.stream()
                .map(connections::get)
                .filter(Objects::nonNull)
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * 根据 ID 获取连接.
     *
     * @param connectionId 连接 ID
     * @return 连接记录，不存在返回 null
     */
    public SseConnection getConnection(String connectionId) {
        return connections.get(connectionId);
    }

    /**
     * 获取 Topic 对应的 Sink.
     *
     * @param topic Topic 名称
     * @return Sink，不存在返回 null
     */
    public Sinks.Many<TopicMessage<Msg>> getSink(String topic) {
        return topicSinks.get(topic);
    }

    /**
     * 获取所有 Topic 名称.
     *
     * @return Topic 集合
     */
    public Set<String> getAllTopics() {
        return new HashSet<>(topicSinks.keySet());
    }

    /**
     * 获取所有 Sink.
     *
     * @return Sink 集合
     */
    public Collection<Sinks.Many<TopicMessage<Msg>>> getAllSinks() {
        return Collections.unmodifiableCollection(topicSinks.values());
    }

    /**
     * 更新 Topic 下所有连接的最后活跃时间.
     *
     * @param topic       Topic 名称
     * @param epochMillis 时间戳（毫秒）
     */
    public void updateTopicActivity(String topic, long epochMillis) {
        Set<String> connIds = topicConnections.get(topic);
        if (connIds != null) {
            for (String connId : connIds) {
                SseConnection conn = connections.get(connId);
                if (conn != null) {
                    conn.updateLastActivity(epochMillis);
                }
            }
        }
    }

    /**
     * 获取活跃 Sink 数量.
     *
     * @return Sink 数量
     */
    public int getActiveSinkCount() {
        return topicSinks.size();
    }

    /**
     * 检查连接是否仍在注册表中.
     *
     * @param conn 连接记录
     * @return 存在返回 true
     */
    public boolean isRegistered(SseConnection conn) {
        return connections.containsKey(conn.getConnectionId());
    }

    /**
     * 关闭所有连接.
     * <p>
     * 向所有 Sink 发送完成信号，清空所有映射。
     * 首先设置 {@link #closing} 标志，阻止 subscribe() 创建新 Sink。
     * </p>
     */
    public void closeAll() {
        closing = true;
        topicSinks.values().forEach(sink -> sink.tryEmitComplete());
        topicSinks.clear();
        connections.clear();
        topicConnections.clear();
        emptySince.clear();
        counter.reset();
    }
}
