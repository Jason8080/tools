package cn.gmlee.tools.im.sse;

import cn.gmlee.tools.im.core.Msg;
import cn.gmlee.tools.im.core.TopicMessage;
import cn.gmlee.tools.im.sse.backpressure.BackpressureStrategy;
import cn.gmlee.tools.im.sse.backpressure.BackpressureStrategyResolver;
import cn.gmlee.tools.im.conf.SseProperties;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Sinks;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * SSE 连接注册表.
 * <p>
 * 内部组件，负责线程安全地存储所有连接、Sink 和计数器。
 * 所有修改操作使用 ConcurrentHashMap.compute() 保证原子性。
 * </p>
 *
 * <h3>设计要点</h3>
 * <ul>
 *   <li>所有计数器使用 AtomicLong/AtomicInteger，支持 CAS 操作</li>
 *   <li>topicSinks 使用 ConcurrentHashMap，读取无锁，写入通过 compute() 原子化</li>
 *   <li>connections 按 connectionId 索引，支持 O(1) 查找</li>
 *   <li>topicConnections 按 Topic 索引连接 ID 集合，支持快速按 Topic 清理</li>
 * </ul>
 */
@Slf4j
@RequiredArgsConstructor
public class SseConnectionRegistry {

    private final SseProperties properties;
    private final BackpressureStrategyResolver strategyResolver;

    /**
     * Topic -> Sink 映射
     */
    private final ConcurrentHashMap<String, Sinks.Many<TopicMessage<Msg>>> topicSinks = new ConcurrentHashMap<>();

    /**
     * Topic -> 连接数映射
     */
    private final ConcurrentHashMap<String, AtomicInteger> topicCounts = new ConcurrentHashMap<>();

    /**
     * 全局连接数
     */
    @Getter
    private final AtomicLong totalConnections = new AtomicLong(0);

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
     * 注销连接.
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
     * 获取或创建 Sink.
     * <p>
     * 使用 computeIfAbsent 保证原子性，同一 Topic 的所有并发请求只会创建一个 Sink。
     * </p>
     *
     * @param topic Topic 名称
     * @return 对应的 Sink
     */
    public Sinks.Many<TopicMessage<Msg>> getOrCreateSink(String topic) {
        return topicSinks.computeIfAbsent(topic, t -> {
            BackpressureStrategy strategy = strategyResolver.resolve(t);
            int bufferSize = properties.getBackpressure().getDefaultBufferSize();
            log.debug("创建 Sink: topic={}, strategy={}, bufferSize={}", t, strategy.name(), bufferSize);
            return strategy.createSink(bufferSize);
        });
    }

    /**
     * 获取 Topic 的连接数.
     *
     * @param topic Topic 名称
     * @return 连接数，无连接返回 0
     */
    public int getTopicCount(String topic) {
        AtomicInteger count = topicCounts.get(topic);
        return count != null ? count.get() : 0;
    }

    /**
     * 获取或创建 Topic 计数器.
     *
     * @param topic Topic 名称
     * @return Topic 计数器
     */
    public AtomicInteger getOrCreateTopicCount(String topic) {
        return topicCounts.computeIfAbsent(topic, k -> new AtomicInteger(0));
    }

    /**
     * 原子清理空 Topic.
     * <p>
     * 使用 ConcurrentHashMap.compute() 持有 bin 锁，保证检查-删除的原子性。
     * 仅当 Topic 计数为 0 时才清理 Sink 和计数器。
     * </p>
     *
     * @param topic Topic 名称
     * @return 如果执行了清理返回 true
     */
    public boolean cleanupIfEmpty(String topic) {
        final boolean[] cleaned = {false};
        topicCounts.compute(topic, (k, v) -> {
            if (v == null || v.get() > 0) {
                return v;
            }
            // 计数为 0，清理 Sink
            topicSinks.remove(k);
            cleaned[0] = true;
            log.debug("清理空 Topic: {}", k);
            return null; // 移除该计数器条目
        });
        return cleaned[0];
    }

    /**
     * 基于 TTL 清理空 Topic.
     * <p>
     * 仅当 Topic 为空超过指定 TTL 时才清理，防止抖动。
     * </p>
     *
     * @param emptyTopicTtlMillis 空 Topic TTL（毫秒）
     * @return 清理的 Topic 数量
     */
    public int cleanupEmptyTopicsByTtl(long emptyTopicTtlMillis) {
        long now = System.currentTimeMillis();
        int cleaned = 0;

        // 检查所有 Topic 计数
        for (Map.Entry<String, AtomicInteger> entry : topicCounts.entrySet()) {
            String topic = entry.getKey();
            AtomicInteger count = entry.getValue();

            if (count.get() == 0) {
                // 首次标记为空
                emptySince.putIfAbsent(topic, now);
                Long since = emptySince.get(topic);
                if (since != null && (now - since) > emptyTopicTtlMillis) {
                    // 超过 TTL，执行清理
                    topicCounts.compute(topic, (k, v) -> {
                        if (v != null && v.get() == 0) {
                            topicSinks.remove(k);
                            return null;
                        }
                        return v;
                    });
                    emptySince.remove(topic);
                    cleaned++;
                }
            } else {
                // Topic 有连接，清除空标记
                emptySince.remove(topic);
            }
        }

        return cleaned;
    }

    /**
     * 获取所有连接快照.
     * <p>
     * 返回当前所有连接的不可变副本，用于 Reaper 扫描。
     * </p>
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
                .collect(Collectors.toList());
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
     * 强制递减连接计数器.
     * <p>
     * 用于 Reaper / forceClose 等非响应式清理路径。
     * 通过 CAS 确保每个连接仅递减一次，避免与 doFinally 的双重递减。
     * </p>
     *
     * @param conn 连接记录
     * @return 如果执行了递减返回 true（表示该连接未被 doFinally 清理过）
     */
    public boolean forceDecrementCounters(SseConnection conn) {
        // CAS: 仅当 doFinally 尚未处理时执行递减
        if (!conn.getCountersDecrementGuard().compareAndSet(false, true)) {
            return false;
        }
        // 递减全局计数
        totalConnections.decrementAndGet();
        // 递减 Topic 计数
        AtomicInteger topicCount = topicCounts.get(conn.getTopic());
        if (topicCount != null) {
            topicCount.decrementAndGet();
        }
        // 从 maps 中移除
        connections.remove(conn.getConnectionId());
        Set<String> connIds = topicConnections.get(conn.getTopic());
        if (connIds != null) {
            connIds.remove(conn.getConnectionId());
        }
        return true;
    }

    /**
     * 获取 Topic 的原子计数器.
     *
     * @param topic Topic 名称
     * @return 计数器，不存在返回 null
     */
    public AtomicInteger getTopicCountAtomic(String topic) {
        return topicCounts.get(topic);
    }

    /**
     * 检查 Topic 是否有连接.
     *
     * @param topic Topic 名称
     * @return 有连接返回 true
     */
    public boolean hasConnections(String topic) {
        return getTopicCount(topic) > 0;
    }

    /**
     * 强制关闭指定连接.
     *
     * @param connectionId 连接 ID
     * @return 找到并标记关闭返回 true
     */
    public boolean forceClose(String connectionId) {
        SseConnection conn = connections.get(connectionId);
        if (conn != null) {
            return conn.markClosed();
        }
        return false;
    }

    /**
     * 关闭所有连接.
     * <p>
     * 向所有 Sink 发送完成信号，清空所有映射。
     * </p>
     */
    public void closeAll() {
        topicSinks.values().forEach(sink -> sink.tryEmitComplete());
        topicSinks.clear();
        topicCounts.clear();
        connections.clear();
        topicConnections.clear();
        emptySince.clear();
        totalConnections.set(0);
    }
}
