package cn.gmlee.tools.im.resume;

import java.io.Serializable;

/**
 * SSE 事件 ID 编解码器.
 * <p>
 * 负责 {@code TopicMessage.id}（{@link Serializable}）与 SSE {@code id:} 字段（字符串）
 * 之间的往返转换：
 * </p>
 * <ul>
 *   <li><b>编码</b>：下发 {@code id:} 字段时调用（{@link #encode}）</li>
 *   <li><b>解码</b>：摄入客户端重连携带的 {@code Last-Event-ID} 请求头时调用（{@link #decode}）</li>
 * </ul>
 *
 * <h3>一致性要求</h3>
 * <p>
 * 编解码必须满足往返一致性：{@code decode(encode(id))} 得到的 ID 与 {@code id}
 * 在 {@link EventIdComparator} 语义下等价（可正确比较先后）。
 * 自定义消息 ID 类型（如雪花算法对象、复合键）必须提供配套实现。
 * </p>
 *
 * @since 5.7.0
 * @see EventIdComparator
 */
public interface EventIdCodec {

    /**
     * 默认实现（数字优先编解码）.
     */
    EventIdCodec DEFAULT = new DefaultEventIdCodec();

    /**
     * 将消息 ID 编码为 SSE {@code id:} 字段字符串.
     *
     * @param id 消息 ID，null 时返回 null（调用方省略 {@code id:} 字段）
     * @return 编码后的字符串
     */
    String encode(Serializable id);

    /**
     * 解码客户端 {@code Last-Event-ID} 请求头.
     *
     * @param raw 原始请求头值（可能为 null / 空白 / 非法）
     * @return 解码后的消息 ID；无法解码时返回 null（按新连接处理）
     */
    Serializable decode(String raw);
}
