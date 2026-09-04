package cn.gmlee.tools.im.resume;

import java.io.Serializable;

/**
 * 事件 ID 顺序比较器.
 * <p>
 * 断点续传的正确性依赖「同 Topic 内消息 ID 全局唯一且全序」。
 * 本比较器为水位线过滤提供唯一判定原语：
 * </p>
 * <pre>
 * 水位线 = 已下发给客户端的最大 ID
 * 候选消息 ID &gt; 水位线 → 下发；否则丢弃（去重）
 * </pre>
 *
 * <h3>宁重勿漏原则</h3>
 * <p>
 * ID 不可比较（类型未知、未实现 Comparable）时，{@link #isAfter} 返回 {@code true}：
 * 宁可让客户端收到重复消息（可由业务去重），也不允许静默丢失。
 * </p>
 *
 * @since 5.7.0
 */
public interface EventIdComparator {

    /**
     * 默认实现（整数优先，同类 {@link Comparable} 兜底，不可比较视为"之后"）.
     */
    EventIdComparator DEFAULT = new DefaultEventIdComparator();

    /**
     * 判断候选消息是否严格晚于水位线.
     *
     * @param candidate 候选消息 ID（null 视为晚于，保证无 ID 消息不被误丢）
     * @param watermark 水位线 ID（null 表示无水位，任何候选都通过）
     * @return true 表示应下发；false 表示应丢弃（重复）
     */
    boolean isAfter(Serializable candidate, Serializable watermark);
}
