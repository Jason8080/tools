package cn.gmlee.tools.im.sse;

import lombok.Getter;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * SSE 连接记录.
 * <p>
 * 追踪单个 SSE 连接的完整生命周期：
 * <ul>
 *   <li>唯一连接 ID（UUID）</li>
 *   <li>所属 Topic</li>
 *   <li>当前状态（原子引用，支持 CAS 转换）</li>
 *   <li>创建时间</li>
 *   <li>最后活跃时间（AtomicLong，避免 Instant 对象分配）</li>
 *   <li>清理守卫（AtomicBoolean，确保清理逻辑仅执行一次）</li>
 * </ul>
 * </p>
 */
@Getter
public class SseConnection {

    /**
     * 连接唯一 ID
     */
    private final String connectionId;

    /**
     * 所属 Topic
     */
    private final String topic;

    /**
     * 创建时间
     */
    private final Instant createdAt;

    /**
     * 当前连接状态（原子引用，支持无锁状态转换）
     */
    private final AtomicReference<ConnectionState> state;

    /**
     * 最后活跃时间（epoch 毫秒，性能优化避免 Instant 分配）
     */
    private final AtomicLong lastActivityAt;

    /**
     * 清理守卫：确保清理逻辑仅执行一次
     * <p>
     * 三层清理机制（doFinally / Reaper / Shutdown）都通过
     * compareAndSet(false, true) 竞争执行权，仅第一个成功者执行清理。
     * </p>
     */
    private final AtomicBoolean cleanupGuard;

    /**
     * 计数器递减守卫：确保连接计数器仅递减一次.
     * <p>
     * 当 Reaper/forceClose 与 doFinally 竞争清理时，
     * 通过 CAS 保证 totalConnections 和 topicCounts 仅递减一次，
     * 避免双重递减导致的计数器下溢。
     * </p>
     */
    @Getter
    private final AtomicBoolean countersDecrementGuard;

    /**
     * 创建新连接.
     *
     * @param topic 所属 Topic
     */
    public SseConnection(String topic) {
        this.connectionId = UUID.randomUUID().toString();
        this.topic = topic;
        this.createdAt = Instant.now();
        this.state = new AtomicReference<>(ConnectionState.CREATED);
        this.lastActivityAt = new AtomicLong(System.currentTimeMillis());
        this.cleanupGuard = new AtomicBoolean(false);
        this.countersDecrementGuard = new AtomicBoolean(false);
    }

    /**
     * 尝试转换状态.
     * <p>
     * 使用 CAS 确保状态转换的原子性，仅一个线程能成功转换。
     * </p>
     *
     * @param from 期望的当前状态
     * @param to   目标状态
     * @return 转换成功返回 true，否则返回 false
     */
    public boolean transition(ConnectionState from, ConnectionState to) {
        return state.compareAndSet(from, to);
    }

    /**
     * 更新最后活跃时间.
     *
     * @param epochMillis 当前时间（epoch 毫秒）
     */
    public void updateLastActivity(long epochMillis) {
        lastActivityAt.set(epochMillis);
    }

    /**
     * 更新最后活跃时间为当前时间.
     */
    public void touch() {
        lastActivityAt.set(System.currentTimeMillis());
    }

    /**
     * 尝试标记为已关闭.
     * <p>
     * 三层清理机制通过此方法竞争清理权：
     * 仅第一个调用 compareAndSet(false, true) 成功的线程执行清理。
     * </p>
     *
     * @return 如果是第一个标记成功的返回 true，否则返回 false
     */
    public boolean markClosed() {
        return cleanupGuard.compareAndSet(false, true);
    }

    /**
     * 检查是否已被标记为关闭.
     *
     * @return 已标记返回 true
     */
    public boolean isClosed() {
        return cleanupGuard.get();
    }

    /**
     * 检查连接是否空闲超时.
     *
     * @param idleTimeoutMillis 空闲超时阈值（毫秒）
     * @return 空闲超过阈值返回 true
     */
    public boolean isIdle(long idleTimeoutMillis) {
        return (System.currentTimeMillis() - lastActivityAt.get()) > idleTimeoutMillis;
    }

    @Override
    public String toString() {
        return "SseConnection{" +
                "id='" + connectionId.substring(0, 8) + '\'' +
                ", topic='" + topic + '\'' +
                ", state=" + state.get() +
                '}';
    }
}
