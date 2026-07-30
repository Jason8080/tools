package cn.gmlee.tools.im.sse.internal;

import cn.gmlee.tools.im.ex.SseConnectionLimitExceededException;
import cn.gmlee.tools.im.ex.SseConnectionLimitExceededException.Scope;
import cn.gmlee.tools.im.sse.SseConnection;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 连接计数器管理器.
 * <p>
 * 封装全局连接数和 Topic 连接数的 CAS 操作，保证计数器递增/递减的原子性和配对性。
 * 这是资源安全的核心组件，确保连接数限制的正确执行。
 * </p>
 *
 * <h3>设计要点</h3>
 * <ul>
 *   <li><b>CAS 循环</b>：tryAcquire() 使用 CAS 循环原子检查并递增计数器</li>
 *   <li><b>双守卫递减</b>：tryDecrement() 使用 countersDecrementGuard 保证仅递减一次</li>
 *   <li><b>topicCounts 条目保留</b>：空 Topic 计数器保留（值为 0），避免与并发 subscribe() 竞态</li>
 * </ul>
 *
 * <h3>资源安全不变量</h3>
 * <ul>
 *   <li>totalConnections 和 topicCounts 始终 >= 0</li>
 *   <li>每个连接的计数器递增/递减恰好一次</li>
 *   <li>异常路径必须回滚已递增的计数器</li>
 * </ul>
 */
public class ConnectionCounter {

    /**
     * 全局连接数
     */
    @Getter
    private final AtomicLong totalConnections = new AtomicLong(0);

    /**
     * Topic -> 连接数映射.
     * <p>
     * <b>设计决策</b>：条目仅在 reset() 时全量移除，不在清理时空 Topic 移除。
     * 原因是移除操作会与并发 tryAcquire() 的 computeIfAbsent 产生竞态。
     * 保留条目（值为 0）的代价是每个历史 Topic 约占 40 字节。
     * </p>
     */
    @Getter
    private final ConcurrentHashMap<String, AtomicInteger> topicCounts = new ConcurrentHashMap<>();

    /**
     * 尝试获取连接许可.
     * <p>
     * 使用 CAS 循环原子检查并递增全局和 Topic 计数器。
     * 如果超过限制，返回拒绝结果（包含拒绝原因和当前值）。
     * </p>
     *
     * @param topic       Topic 名称
     * @param maxTotal    全局最大连接数
     * @param maxPerTopic Topic 最大连接数
     * @return 获取结果
     */
    public AcquireResult tryAcquire(String topic, int maxTotal, int maxPerTopic) {
        // 1. CAS 循环：原子检查并递增全局连接数
        long currentTotal;
        do {
            currentTotal = totalConnections.get();
            if (currentTotal >= maxTotal) {
                return AcquireResult.rejectedGlobal((int) currentTotal, maxTotal);
            }
        } while (!totalConnections.compareAndSet(currentTotal, currentTotal + 1));

        // 2. CAS 循环：原子检查并递增 Topic 连接数
        AtomicInteger topicCount = getOrCreateTopicCount(topic);
        int currentTopic;
        do {
            currentTopic = topicCount.get();
            if (currentTopic >= maxPerTopic) {
                // 回滚全局计数
                totalConnections.decrementAndGet();
                return AcquireResult.rejectedTopic(topic, currentTopic, maxPerTopic);
            }
        } while (!topicCount.compareAndSet(currentTopic, currentTopic + 1));

        return AcquireResult.success();
    }

    /**
     * 回滚计数器（异常路径）.
     * <p>
     * 当连接创建过程中发生异常时调用，回滚已递增的计数器。
     * </p>
     *
     * @param topic Topic 名称
     */
    public void rollback(String topic) {
        totalConnections.decrementAndGet();
        AtomicInteger topicCount = topicCounts.get(topic);
        if (topicCount != null) {
            topicCount.decrementAndGet();
        }
    }

    /**
     * 尝试递减计数器.
     * <p>
     * 使用 {@link SseConnection#tryAcquireCountersDecrement()}（位域 CAS）
     * 保证每个连接仅递减一次，避免 doFinally 与 Reaper/forceClose 的双重递减。
     * </p>
     *
     * @param conn 连接记录
     * @return 如果执行了递减返回 true
     */
    public boolean tryDecrement(SseConnection conn) {
        // 位域 CAS: 仅当计数器尚未递减时执行
        if (!conn.tryAcquireCountersDecrement()) {
            return false;
        }
        totalConnections.decrementAndGet();
        AtomicInteger topicCount = topicCounts.get(conn.getTopic());
        if (topicCount != null) {
            topicCount.decrementAndGet();
        }
        return true;
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
     * 重置所有计数器（关闭时调用）.
     */
    public void reset() {
        totalConnections.set(0);
        topicCounts.clear();
    }

    /**
     * 连接许可获取结果.
     */
    @Getter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class AcquireResult {
        private final boolean success;
        private final SseConnectionLimitExceededException exception;

        static AcquireResult success() {
            return new AcquireResult(true, null);
        }

        static AcquireResult rejectedGlobal(int current, int max) {
            return new AcquireResult(false,
                    new SseConnectionLimitExceededException("unknown", current, max, Scope.GLOBAL));
        }

        static AcquireResult rejectedTopic(String topic, int current, int max) {
            return new AcquireResult(false,
                    new SseConnectionLimitExceededException(topic, current, max, Scope.PER_TOPIC));
        }
    }
}
