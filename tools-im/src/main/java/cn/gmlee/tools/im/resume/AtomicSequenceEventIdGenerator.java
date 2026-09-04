package cn.gmlee.tools.im.resume;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 单 JVM 自增事件 ID 生成器（默认实现）.
 * <p>
 * 序列起点为构造时刻的 {@code System.currentTimeMillis()}，
 * 此后单调递增，保证单实例内唯一且全序；跨 JVM 重启后起点前进，
 * ID 不回退（大部分情况下）。
 * </p>
 * <p>
 * <b>适用范围</b>：仅单实例（STANDALONE）部署。
 * CLUSTER 多实例 + 断点续传场景必须改用
 * {@link SnowflakeEventIdGenerator}（{@code im.sse.resume.snowflake.enabled=true}），
 * 否则各实例序列交叉破坏全序性。
 * </p>
 *
 * @since 5.7.0
 */
public final class AtomicSequenceEventIdGenerator implements EventIdGenerator {

    private final AtomicLong sequence = new AtomicLong(System.currentTimeMillis());

    @Override
    public Serializable nextId(String topic) {
        return sequence.incrementAndGet();
    }
}
