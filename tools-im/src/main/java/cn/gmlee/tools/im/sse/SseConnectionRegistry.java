package cn.gmlee.tools.im.sse;

import cn.gmlee.tools.im.conf.SseProperties;
import cn.gmlee.tools.im.model.ConnectionMetadata;
import cn.gmlee.tools.im.model.Msg;
import cn.gmlee.tools.im.model.TopicMessage;
import cn.gmlee.tools.im.sse.backpressure.BackpressureStrategy;
import cn.gmlee.tools.im.sse.backpressure.BackpressureStrategyResolver;
import cn.gmlee.tools.im.sse.internal.ConnectionCounter;
import cn.gmlee.tools.im.sse.metrics.SseMetrics;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Sinks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
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
 *   <li><b>空 Topic 追踪</b>：emptyTopics 独立记录空 Topic 及其首次变空时间，
 *       避免 TTL 扫描时遍历全量 topicCounts（含大量历史零值条目）。
 *       采用防抖动设计：Topic 变空后保留 {@code cleanup.emptyTopicTtl}（默认 60s）缓冲期，
 *       避免间歇性流量导致的资源频繁创建/销毁</li>
 *   <li><b>双通道架构</b>：
 *       <ul>
 *         <li>{@code directedSinks}：connectionId → 定向 Sink（仅有 routingKey 的连接）</li>
 *         <li>{@code routingKeyIndex}：topic → routingKey → Set&lt;connectionId&gt; 反向索引，
 *             支持 O(K) 定向投递目标查找（K 为目标连接数，远小于 N）</li>
 *       </ul>
 *       广播消息走 topicSink（共享），定向消息走 directedSink（per-connection），
 *       两条通道互斥，无重复投递。
 *   </li>
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
     * 指标收集器（延迟设置，避免循环依赖）.
     * <p>
     * SseMetrics 需要 SseConnectionRegistry 来注册 Gauge，
     * 而 SseConnectionRegistry 需要 SseMetrics 来记录压缩指标。
     * 通过 setter 注入打破循环依赖。
     * </p>
     */
    private SseMetrics metrics;

    /**
     * Topic -> Sink 映射
     */
    private final ConcurrentHashMap<String, Sinks.Many<TopicMessage>> topicSinks = new ConcurrentHashMap<>();

    /**
     * connectionId -> 连接记录
     */
    private final ConcurrentHashMap<String, SseConnection> connections = new ConcurrentHashMap<>();

    /**
     * Topic -> connectionId 集合
     */
    private final ConcurrentHashMap<String, Set<String>> topicConnections = new ConcurrentHashMap<>();

    /**
     * 空 Topic 追踪：Topic -> 首次变空时间（毫秒）.
     * <p>
     * 仅包含当前连接数为 0 的 Topic，由 {@link #cleanupIfEmpty} 写入，
     * 由 {@link #cleanupEmptyTopicsByTtl} 和 {@link #compactTopicCounts} 消费。
     * 独立于 {@code counter.compute()} 维护，避免 TTL 扫描时
     * 遍历全量历史 Topic 条目（可能数万条），将扫描复杂度从 O(全部历史) 降为 O(当前空 Topic)。
     * </p>
     *
     * <h3>防抖动设计</h3>
     * <p>
     * Topic 变空后不立即销毁，而是保留 {@code cleanup.emptyTopicTtl}（默认 60s）的缓冲期。
     * 这避免了间歇性流量场景下的资源抖动（empty → cleanup → create 循环）：
     * </p>
     * <ul>
     *   <li>聊天室短暂无人 → 60s 内有人加入 → 复用已有资源，无需重建</li>
     *   <li>动态 Topic 短时波动 → 避免频繁创建/销毁 Publisher/Repeater/Subscriber</li>
     *   <li>减少 Spring Cloud Stream binding 的注册/注销开销</li>
     * </ul>
     * <p>
     * 超时后由 {@link #cleanupEmptyTopicsByTtl} 清理 Sink 和连接集合，
     * 再由 {@link #compactTopicCounts} 在 {@code cleanup.compactTtl}（默认 1h）后移除计数器条目。
     * </p>
     */
    private final ConcurrentHashMap<String, Long> emptyTopics = new ConcurrentHashMap<>();

    /**
     * connectionId → 定向投递 Sink.
     * <p>
     * 仅包含有 routingKey 的连接。publish 路径通过此 map 直接向目标连接投递定向消息，
     * 避免广播到全部连接后再 filter 的 O(N) 开销。
     * </p>
     */
    private final ConcurrentHashMap<String, Sinks.Many<TopicMessage>> directedSinks = new ConcurrentHashMap<>();

    /**
     * 反向索引：topic → routingKey → Set&lt;connectionId&gt;.
     * <p>
     * 支持定向投递的 O(K) 目标查找：给定消息的 routingKeys，
     * 按 routingKey 查找所有匹配连接的 ID，再逐个从 {@link #directedSinks} 获取 Sink 投递。
     * </p>
     * <p>
     * 内层 Set 使用 {@link ConcurrentHashMap#newKeySet()} 保证线程安全。
     * 外层和内层 Map 均使用 ConcurrentHashMap 支持并发 compute 操作。
     * </p>
     */
    private final ConcurrentHashMap<String, ConcurrentHashMap<String, Set<String>>> routingKeyIndex = new ConcurrentHashMap<>();

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
     * 设置指标收集器（延迟注入，避免循环依赖）.
     * <p>
     * 由 {@link ImAutoConfiguration} 在创建 SseMetrics 后调用。
     * </p>
     *
     * @param metrics 指标收集器
     */
    public void setMetrics(SseMetrics metrics) {
        this.metrics = metrics;
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
        // Topic 有新连接，清除空 Topic 追踪
        emptyTopics.remove(conn.getTopic());

        // 双通道架构：有 routingKey 的连接创建 directedSink 并注册反向索引
        ConnectionMetadata metadata = conn.getMetadata();
        String routingKey = metadata != null ? metadata.getRoutingKey() : null;
        if (routingKey != null && !routingKey.isEmpty()) {
            Sinks.Many<TopicMessage> ds = conn.createDirectedSink();
            directedSinks.put(conn.getConnectionId(), ds);
            routingKeyIndex
                    .computeIfAbsent(conn.getTopic(), k -> new ConcurrentHashMap<>())
                    .computeIfAbsent(routingKey, k -> ConcurrentHashMap.newKeySet())
                    .add(conn.getConnectionId());
            log.debug("[Registry] 定向 Sink 注册: topic={}, connectionId={}, routingKey={}",
                    conn.getTopic(), conn.getConnectionId(), routingKey);
        }
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

        // 双通道架构：清理 directedSink 和反向索引
        Sinks.Many<TopicMessage> removed = directedSinks.remove(conn.getConnectionId());
        if (removed != null) {
            ConnectionMetadata metadata = conn.getMetadata();
            String routingKey = metadata != null ? metadata.getRoutingKey() : null;
            if (routingKey != null) {
                ConcurrentHashMap<String, Set<String>> topicIndex = routingKeyIndex.get(conn.getTopic());
                if (topicIndex != null) {
                    // 使用 compute() 保证「移除 connId → 检查空 → 移除 routingKey」的原子性，
                    // 消除 check-then-remove 的竞态窗口
                    topicIndex.compute(routingKey, (k, keySet) -> {
                        if (keySet == null) {
                            return null;
                        }
                        keySet.remove(conn.getConnectionId());
                        return keySet.isEmpty() ? null : keySet;
                    });
                    // topicIndex 可能在 compute 期间被其他线程填充新 routingKey，需再次检查
                    if (topicIndex.isEmpty()) {
                        routingKeyIndex.remove(conn.getTopic());
                    }
                }
            }
            removed.tryEmitComplete();
        }
    }

    /**
     * 统一清理连接.
     * <p>
     * 执行完整的连接清理流程：
     * <ol>
     *   <li>尝试递减计数器（通过 ConnectionCounter.tryDecrement）</li>
     *   <li>从 maps 中移除连接</li>
     *   <li>如果 Topic 为空，清理 Sink 和连接集合，并追踪空 Topic</li>
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
     * <p>
     * 缓冲区大小优先使用 {@code topicBufferOverrides} 中按 Topic 配置的值，
     * 未命中则使用 {@code defaultBufferSize}。
     * </p>
     *
     * @param topic Topic 名称
     * @return 对应的 Sink，关闭中返回 null
     */
    public Sinks.Many<TopicMessage> getOrCreateSink(String topic) {
        if (closing) {
            return null;
        }
        return topicSinks.computeIfAbsent(topic, t -> {
            BackpressureStrategy strategy = strategyResolver.resolve(t);
            int bufferSize = resolveBufferSize(t);
            log.debug("[Sink] 创建: topic={}, strategy={}, bufferSize={}", t, strategy.name(), bufferSize);
            return strategy.createSink(bufferSize);
        });
    }

    /**
     * 解析 Topic 的缓冲区大小.
     * <p>
     * 优先查找 {@code topicBufferOverrides} 配置，未命中则使用默认值。
     * </p>
     *
     * @param topic Topic 名称
     * @return 缓冲区大小
     */
    private int resolveBufferSize(String topic) {
        Map<String, Integer> overrides = properties.getBackpressure().getTopicBufferOverrides();
        if (overrides != null) {
            Integer override = overrides.get(topic);
            if (override != null) {
                return override;
            }
        }
        return properties.getBackpressure().getDefaultBufferSize();
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
     * <p>
     * 清理成功后通过 {@link #trackEmptyTopic} 记录空 Topic 到 {@link #emptyTopics}，
     * 供 TTL 扫描和计数器压缩使用。
     * </p>
     *
     * @param topic Topic 名称
     * @return 如果执行了清理返回 true
     */
    public boolean cleanupIfEmpty(String topic) {
        // 先记录清理前的 Sink 存在状态，compute 后对比判断是否由本次调用执行了清理
        boolean sinkExistedBefore = topicSinks.containsKey(topic);

        counter.compute(topic, (k, v) -> {
            if (v == null || v.get() > 0) {
                return v;
            }
            // 计数为 0，清理 Sink 和连接集合，但保留计数器条目
            topicSinks.remove(topic);
            topicConnections.remove(topic);
            routingKeyIndex.remove(topic);
            log.debug("[Topic] 清理空 Topic: {}", topic);
            return v;
        });

        // compute 内部已原子移除 Sink，通过前后对比判断本次是否执行了清理
        // （若其他线程先完成清理，sinkExistedBefore 为 false，不会误判）
        boolean cleaned = sinkExistedBefore && !topicSinks.containsKey(topic);
        if (cleaned) {
            trackEmptyTopic(topic);
        }
        return cleaned;
    }

    /**
     * 记录空 Topic 首次变空时间.
     * <p>
     * 使用 putIfAbsent 保证仅首次调用设置时间戳，后续调用不会覆盖。
     * 这是防抖动设计的关键：即使 Topic 短暂恢复连接后再次变空，
     * 也不会重置计时器，确保超时后必然被清理。
     * </p>
     *
     * @param topic Topic 名称
     */
    private void trackEmptyTopic(String topic) {
        emptyTopics.putIfAbsent(topic, System.currentTimeMillis());
    }

    /**
     * 基于 TTL 清理空 Topic.
     * <p>
     * 仅遍历 {@link #emptyTopics}（当前空 Topic 集合），而非全量 topicCounts，
     * 在长期运行且 Topic 基数高的场景下性能显著优于全量扫描。
     * 保留 topicCounts 计数器条目，避免与并发 subscribe() 的竞态。
     * </p>
     *
     * <h3>防抖动机制</h3>
     * <p>
     * Topic 变空后不立即清理，而是等待 {@code cleanup.emptyTopicTtl}（默认 60s）超时。
     * 这段时间内如果有新连接加入，Topic 会从 {@link #emptyTopics} 移除，避免资源销毁。
     * 超时后才执行清理（移除 Sink、连接集合），计数器条目由 {@link #compactTopicCounts} 延迟移除。
     * </p>
     *
     * @return 被清理的 Topic 名称列表
     */
    public List<String> cleanupEmptyTopicsByTtl() {
        if (emptyTopics.isEmpty()) {
            return Collections.emptyList();
        }

        long now = System.currentTimeMillis();
        long emptyTopicTtlMillis = properties.getCleanup().getEmptyTopicTtl().toMillis();
        List<String> cleaned = new ArrayList<>();

        for (Map.Entry<String, Long> entry : emptyTopics.entrySet()) {
            String topic = entry.getKey();
            Long since = entry.getValue();
            if (since == null || (now - since) <= emptyTopicTtlMillis) {
                continue;
            }

            // 超过 TTL，原子检查并清理
            final boolean[] didClean = {false};
            counter.compute(topic, (k, v) -> {
                if (v != null && v.get() == 0) {
                    topicSinks.remove(topic);
                    topicConnections.remove(topic);
                    routingKeyIndex.remove(topic);
                    didClean[0] = true;
                }
                return v;
            });

            emptyTopics.remove(topic);
            if (didClean[0]) {
                cleaned.add(topic);
            }
        }

        return cleaned;
    }

    /**
     * 压缩长期为空的 Topic 计数器条目.
     * <p>
     * 仅遍历 {@link #emptyTopics}（当前空 Topic 集合），而非全量 topicCounts。
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
        if (compactTtlMillis <= 0 || emptyTopics.isEmpty()) {
            return 0;
        }

        long now = System.currentTimeMillis();
        int compacted = 0;

        for (Map.Entry<String, Long> entry : emptyTopics.entrySet()) {
            String topic = entry.getKey();
            Long since = entry.getValue();
            if (since == null || (now - since) < compactTtlMillis) {
                continue;
            }

            // 原子检查并移除
            final boolean[] removed = {false};
            counter.compute(topic, (k, v) -> {
                if (v != null && v.get() == 0 && !topicSinks.containsKey(k)) {
                    removed[0] = true;
                    return null; // 移除条目
                }
                return v;
            });

            emptyTopics.remove(topic);
            if (removed[0]) {
                compacted++;
                log.debug("[Topic] 压缩计数器: {}", topic);
            }
        }

        // 记录压缩指标
        if (compacted > 0 && metrics != null) {
            metrics.recordTopicCompaction(compacted);
        }

        return compacted;
    }

    /**
     * 获取所有连接快照.
     *
     * @return 连接列表快照
     * @deprecated 使用 {@link #forEachConnection(java.util.function.Consumer)} 避免内存分配
     */
    @Deprecated
    public List<SseConnection> snapshotConnections() {
        return new ArrayList<>(connections.values());
    }

    /**
     * 遍历所有连接（无内存分配）.
     * <p>
     * 直接迭代 ConcurrentHashMap，避免创建临时 ArrayList 副本。
     * 适用于不需要一致性快照的场景（如 Reaper 扫描、关闭时批量处理）。
     * </p>
     *
     * <h3>线程安全性</h3>
     * <p>
     * ConcurrentHashMap 的迭代器是弱一致性的：
     * </p>
     * <ul>
     *   <li>迭代过程中新增的连接可能被遍历到，也可能被跳过</li>
     *   <li>迭代过程中删除的连接不会被访问（已访问的不会重复访问）</li>
     *   <li>不会抛出 ConcurrentModificationException</li>
     * </ul>
     *
     * @param action 对每个连接执行的操作
     */
    public void forEachConnection(java.util.function.Consumer<SseConnection> action) {
        connections.values().forEach(action);
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
    public Sinks.Many<TopicMessage> getSink(String topic) {
        return topicSinks.get(topic);
    }

    /**
     * 获取连接的定向投递 Sink.
     *
     * @param connectionId 连接 ID
     * @return 定向 Sink，无 routingKey 或已清理返回 null
     */
    public Sinks.Many<TopicMessage> getDirectedSink(String connectionId) {
        return directedSinks.get(connectionId);
    }

    /**
     * 按路由键查找目标连接 ID 集合.
     * <p>
     * 返回内部 Set 的引用（弱一致迭代），供 publish 路径遍历投递。
     * </p>
     *
     * @param topic      Topic 名称
     * @param routingKey 路由键
     * @return 连接 ID 集合（不可变空集表示无匹配）
     */
    public Set<String> getConnectionIdsByRoutingKey(String topic, String routingKey) {
        ConcurrentHashMap<String, Set<String>> topicIndex = routingKeyIndex.get(topic);
        if (topicIndex == null) {
            return Collections.emptySet();
        }
        Set<String> connIds = topicIndex.get(routingKey);
        return connIds != null ? connIds : Collections.emptySet();
    }

    /**
     * 获取所有定向投递 Sink（供 shutdown 完成信号）.
     *
     * @return 不可变的 Sink 集合
     */
    public Collection<Sinks.Many<TopicMessage>> getAllDirectedSinks() {
        return Collections.unmodifiableCollection(directedSinks.values());
    }

    /**
     * 获取活跃定向 Sink 数量（供 metrics gauge）.
     *
     * @return 数量
     */
    public int getDirectedSinkCount() {
        return directedSinks.size();
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
    public Collection<Sinks.Many<TopicMessage>> getAllSinks() {
        return Collections.unmodifiableCollection(topicSinks.values());
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
        // 双通道架构：先完成定向 Sink 并清理索引
        directedSinks.values().forEach(Sinks.Many::tryEmitComplete);
        directedSinks.clear();
        routingKeyIndex.clear();
        // 广播通道
        topicSinks.values().forEach(Sinks.Many::tryEmitComplete);
        topicSinks.clear();
        connections.clear();
        topicConnections.clear();
        emptyTopics.clear();
        counter.reset();
    }
}
