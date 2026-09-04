package cn.gmlee.tools.im.resume;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 雪花算法事件 ID 生成器（无锁实现）.
 * <p>
 * 64 位布局：{@code 0 | 41bit 时间戳 | 10bit workerId | 12bit 序列号}。
 * 单实例每毫秒最多 4096 个 ID，时间戳可用约 69 年（自 {@link #EPOCH_MILLIS} 起）。
 * </p>
 *
 * <h3>并发设计</h3>
 * <ul>
 *   <li>单个 {@link AtomicLong} 保存状态 {@code (相对时间戳 << 12) | 序列号}，
 *       通过 CAS 循环推进，无锁、无阻塞</li>
 *   <li>同一毫秒序列号耗尽时，<b>逻辑时钟前进 1ms</b>（预支未来毫秒），
 *       避免忙等待；单调性不受影响</li>
 *   <li>时钟回拨时沿用状态中的较大时间戳，保证生成的 ID 不回退</li>
 * </ul>
 *
 * <h3>集群使用</h3>
 * <p>
 * 每个实例必须配置不同的 {@code workerId}（0~1023）：
 * {@code im.sse.resume.snowflake.worker-id}；未配置时框架按主机名哈希自动分配
 * （存在小概率冲突，生产环境建议显式配置）。
 * </p>
 *
 * @since 5.7.0
 */
public final class SnowflakeEventIdGenerator implements EventIdGenerator {

    /**
     * 自定义纪元：2020-01-01T00:00:00Z.
     */
    public static final long EPOCH_MILLIS = 1577836800000L;

    private static final int WORKER_BITS = 10;
    private static final int SEQ_BITS = 12;
    private static final long SEQ_MASK = (1L << SEQ_BITS) - 1;          // 0xFFF
    private static final long MAX_WORKER_ID = (1L << WORKER_BITS) - 1;  // 1023

    private final long workerShifted;

    /**
     * 打包状态：{@code (相对时间戳 << 12) | 序列号}.
     * <p>相对时间戳 = {@code System.currentTimeMillis() - EPOCH_MILLIS}。</p>
     */
    private final AtomicLong state = new AtomicLong(0L);

    /**
     * 创建雪花算法生成器.
     *
     * @param workerId 实例编号（0~1023），集群内必须唯一
     * @throws IllegalArgumentException workerId 超出范围
     */
    public SnowflakeEventIdGenerator(long workerId) {
        if (workerId < 0 || workerId > MAX_WORKER_ID) {
            throw new IllegalArgumentException(
                    "snowflake workerId 必须在 [0, " + MAX_WORKER_ID + "] 范围内: " + workerId);
        }
        this.workerShifted = workerId << SEQ_BITS;
    }

    @Override
    public Serializable nextId(String topic) {
        return nextLong();
    }

    /**
     * 生成下一个 64 位雪花 ID（单调递增）.
     *
     * @return 雪花 ID
     */
    public long nextLong() {
        for (;;) {
            long now = System.currentTimeMillis() - EPOCH_MILLIS;
            long cur = state.get();
            long curTs = cur >>> SEQ_BITS;
            long seq = cur & SEQ_MASK;

            long nextTs;
            long nextSeq;
            if (now > curTs) {
                // 新的毫秒（含时钟回拨后追上）：序列号归零
                nextTs = now;
                nextSeq = 0;
            } else {
                // 同一毫秒（或时钟回拨）：沿用 curTs 保证不回退
                nextTs = curTs;
                nextSeq = seq + 1;
                if (nextSeq > SEQ_MASK) {
                    // 本毫秒序列号耗尽：逻辑时钟前进 1ms（预支），避免忙等待
                    nextTs = curTs + 1;
                    nextSeq = 0;
                }
            }

            long next = (nextTs << SEQ_BITS) | nextSeq;
            if (state.compareAndSet(cur, next)) {
                return (nextTs << (WORKER_BITS + SEQ_BITS)) | workerShifted | nextSeq;
            }
            // CAS 失败：其他线程已推进，重试
        }
    }
}
