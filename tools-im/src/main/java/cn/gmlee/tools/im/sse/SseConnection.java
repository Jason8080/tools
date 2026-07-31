package cn.gmlee.tools.im.sse;

import cn.gmlee.tools.im.model.ConnectionMetadata;
import cn.gmlee.tools.im.model.ConnectionState;
import cn.gmlee.tools.im.model.Msg;
import cn.gmlee.tools.im.model.TopicMessage;
import lombok.Getter;
import lombok.Setter;
import org.reactivestreams.Subscription;
import reactor.core.publisher.Sinks;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
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
 *   <li>位域标志（AtomicInteger，合并清理守卫和计数器递减守卫）</li>
 *   <li>定向投递 Sink（directedSink，仅当连接有 routingKey 时创建）</li>
 * </ul>
 * </p>
 *
 * <h3>双通道架构</h3>
 * <p>
 * 连接通过 {@link #createDirectedSink()} 创建专属的定向 Sink，
 * 由 {@link SseConnectionRegistry} 在注册时按需调用。
 * 广播消息走 topicSink（共享），定向消息走 directedSink（per-connection），
 * 两条通道在 {@link SseConnectionFluxBuilder} 中通过 {@code Flux.merge} 合并为统一的消息流。
 * </p>
 *
 * <h3>位域标志设计</h3>
 * <p>
 * 使用单个 {@link AtomicInteger} 的两个位替代两个独立 {@link java.util.concurrent.atomic.AtomicBoolean}：
 * <ul>
 *   <li>bit 0 ({@link #FLAG_CLEANUP})：清理守卫，三层清理机制通过 CAS 竞争执行权</li>
 *   <li>bit 1 ({@link #FLAG_COUNTERS_DECREMENT})：计数器递减守卫，保证计数器仅递减一次</li>
 * </ul>
 * 合并后每连接减少一个原子变量（~16 字节对象头 + 引用），且位操作的原子语义更清晰。
 * 两个标志相互独立：赢得清理权的线程通过 bit 0 判定，计数器递减通过 bit 1 二次保护。
 * </p>
 */
@Getter
public class SseConnection {

    /**
     * 清理守卫标志位（bit 0）.
     * <p>
     * 三层清理机制（doFinally / Reaper / Shutdown）都通过
     * {@link #markClosed()} 竞争此位，仅第一个成功的线程执行清理。
     * </p>
     */
    private static final int FLAG_CLEANUP = 1;

    /**
     * 计数器递减守卫标志位（bit 1）.
     * <p>
     * 当 Reaper/forceClose 与 doFinally 竞争清理时，
     * 通过 {@link #tryAcquireCountersDecrement()} 保证
     * totalConnections 和 topicCounts 仅递减一次，
     * 避免双重递减导致的计数器下溢。
     * </p>
     */
    private static final int FLAG_COUNTERS_DECREMENT = 1 << 1;

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
     * 位域标志：合并清理守卫（bit 0）和计数器递减守卫（bit 1）.
     * <p>
     * 使用 {@link AtomicInteger#getAndUpdate(java.util.function.IntUnaryOperator)} 原子设置位，
     * 通过检查返回值中对应位是否已设置来判断是否为首次操作。
     * </p>
     */
    private final AtomicInteger flags;

    /**
     * Reactive Streams 订阅引用（volatile，支持延迟设置和线程安全取消）.
     * <p>
     * 在 doOnSubscribe 回调中设置，用于 Reaper/forceClose 主动取消 Flux 订阅，
     * 确保强制关闭时 Flux 立即终止（而非等待客户端自行断开）。
     * </p>
     * -- SETTER --
     *  设置 Reactive Streams 订阅引用.
     *  <p>
     *  在 Flux 的 doOnSubscribe 回调中调用，保存订阅引用以支持后续主动取消。
     *  </p>
     *

     */
    @Setter
    private volatile Subscription subscription;

    /**
     * 连接元数据（routingKey、自定义属性等）.
     * <p>
     * 订阅时由 {@link SseConnectionManager} 设置，生命周期内不可变。
     * volatile 保证跨线程可见性。
     * </p>
     * -- GETTER --
     *  获取连接元数据.
     * <p>
     *
     * -- SETTER --
     *  设置连接元数据.
     *  <p>
     *  在
     *  中调用，绑定用户身份到连接。
     *  </p>
     *
     */
    @Setter
    private volatile ConnectionMetadata metadata;

    /**
     * Per-connection 定向投递 Sink（nullable）.
     * <p>
     * 仅当连接具有 routingKey 时非 null。接收定向消息（routingKeys 匹配的消息），
     * 与共享的 topicSink（广播通道）互斥，构成双通道投递架构。
     * </p>
     * <p>
     * volatile 保证安全发布：写入在 {@code register()} 期间（连接对 publish 线程可见前），
     * 读取在 publish 路径和 Flux 构建时。清理时置 null。
     * </p>
     */
    private volatile Sinks.Many<TopicMessage<Msg>> directedSink;

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
        this.flags = new AtomicInteger(0);
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
     * 激活连接（CREATED → ACTIVE）.
     * <p>
     * 在 Flux 的 doOnSubscribe 回调中调用，将连接标记为活跃状态。
     * </p>
     *
     * @return 转换成功返回 true，已被关闭或状态不匹配返回 false
     */
    public boolean activate() {
        return state.compareAndSet(ConnectionState.CREATED, ConnectionState.ACTIVE);
    }

    /**
     * 尝试排空连接（ACTIVE/CREATED → DRAINING）.
     * <p>
     * 用于 Reaper 和 Shutdown 路径，将连接标记为排空中。
     * 仅 ACTIVE 或 CREATED 状态的连接可转换为 DRAINING。
     * </p>
     *
     * @return 转换成功返回 true，已是 DRAINING/CLOSED 或状态不匹配返回 false
     */
    public boolean tryDrain() {
        ConnectionState current = state.get();
        if (current == ConnectionState.DRAINING || current == ConnectionState.CLOSED) {
            return false;
        }
        return state.compareAndSet(current, ConnectionState.DRAINING);
    }

    /**
     * 完成关闭（* → CLOSED）.
     * <p>
     * 在清理完成后调用，将连接标记为已关闭。
     * </p>
     */
    public void completeClose() {
        state.set(ConnectionState.CLOSED);
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
     * 尝试标记为已关闭（竞争清理执行权）.
     * <p>
     * 三层清理机制通过此方法竞争清理权：
     * 原子设置 bit 0，仅第一个成功设置的线程返回 true 并执行清理。
     * 使用 {@link AtomicInteger#getAndUpdate(java.util.function.IntUnaryOperator)} 保证原子性。
     * </p>
     *
     * @return 如果是第一个标记成功的返回 true，否则返回 false
     */
    public boolean markClosed() {
        int prev = flags.getAndUpdate(old -> old | FLAG_CLEANUP);
        return (prev & FLAG_CLEANUP) == 0;
    }

    /**
     * 检查是否已被标记为关闭.
     *
     * @return 已标记返回 true
     */
    public boolean isClosed() {
        return (flags.get() & FLAG_CLEANUP) != 0;
    }

    /**
     * 尝试获取计数器递减权.
     * <p>
     * 原子设置 bit 1，仅第一个成功设置的线程返回 true 并执行计数器递减。
     * 这是双守卫机制的第二道防线：即使 {@link #markClosed()} 的 else 分支
     * 意外进入 {@code cleanupConnection}，计数器也不会被双重递减。
     * </p>
     *
     * @return 如果是第一个获取递减权的返回 true，否则返回 false
     */
    public boolean tryAcquireCountersDecrement() {
        int prev = flags.getAndUpdate(old -> old | FLAG_COUNTERS_DECREMENT);
        return (prev & FLAG_COUNTERS_DECREMENT) == 0;
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

    /**
     * 主动取消 Flux 订阅.
     * <p>
     * 用于 Reaper/forceClose 路径：在标记关闭并递减计数器后，
     * 调用此方法立即终止 Flux，触发 doFinally 清理，
     * 而非等待客户端自行断开。
     * </p>
     * <p>
     * 线程安全：volatile 保证可见性，Subscription.cancel() 本身是线程安全的。
     * 如果 subscription 尚未设置（doOnSubscribe 尚未执行），则跳过取消。
     * </p>
     */
    public void cancel() {
        Subscription s = this.subscription;
        if (s != null) {
            s.cancel();
        }
    }

    // ==================== 定向投递 Sink ====================

    /**
     * 创建并赋值定向投递 Sink.
     * <p>
     * 在 {@link SseConnectionRegistry#register(SseConnection)} 期间调用，
     * 仅当连接具有 routingKey 时创建。使用小缓冲区（16），因为定向消息通常是低频率的。
     * </p>
     *
     * @return 新创建的 directedSink
     */
    public Sinks.Many<TopicMessage<Msg>> createDirectedSink() {
        Sinks.Many<TopicMessage<Msg>> ds = Sinks.many()
                .multicast()
                .onBackpressureBuffer(16, false);
        this.directedSink = ds;
        return ds;
    }

    /**
     * 检查是否存在定向投递 Sink.
     *
     * @return directedSink 非 null 返回 true
     */
    public boolean hasDirectedSink() {
        return directedSink != null;
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
