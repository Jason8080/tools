package cn.gmlee.tools.im.core;

import java.io.Serializable;
import java.util.function.Consumer;

/**
 * 消息转发器（转/Transform）.
 * <p>
 * Topic 的消息桥接层，聚合了 MQ 发送与消费两端的职责：
 * </p>
 * <ul>
 *   <li>{@link #send(Msg)} — 将消息发送到 Stream (MQ)，用于编程式发送</li>
 *   <li>{@link #receive(TopicMessage)} — MQ 消费后转发到 SSE 连接</li>
 *   <li>{@link #accept(TopicMessage)} — {@link Consumer} 入口，MQ 消费者回调，默认委托给 {@link #receive}</li>
 * </ul>
 * <p>
 * 实现 {@link Consumer}{@code <TopicMessage<Msg>>}，可直接注册为 Spring Cloud Stream Consumer Bean。
 * </p>
 *
 * <h3>默认实现</h3>
 * <p>
 * {@code DefaultRepeater} 桥接 StreamBridge 和 SseConnectionManager：
 * 发送端通过 StreamBridge 写入 MQ，接收端通过 SseConnectionManager 推送到 SSE 连接。
 * </p>
 *
 * <h3>自定义扩展</h3>
 * <pre>{@code
 * @Component
 * public class AuditRepeater implements Repeater {
 *     @Override public String topic() { return "im.chat"; }
 *     @Override public Serializable send(Msg msg) {
 *         auditLog.record("send", msg);  // 审计日志
 *         Repeater delegate = topicRegistry.createDefaultRepeater(topic());
 *         return delegate.send(msg);     // 委托默认实现
 *     }
 *     @Override public void receive(TopicMessage<Msg> message) {
 *         auditLog.record("receive", message);
 *         Repeater delegate = topicRegistry.createDefaultRepeater(topic());
 *         delegate.receive(message);
 *     }
 * }
 * }</pre>
 *
 * @since 5.6.0
 */
public interface Repeater extends Topic, Consumer<TopicMessage<Msg>> {

    /**
     * 发送消息到 Stream (MQ).
     * <p>
     * 将消息包装为 {@link TopicMessage} 并通过 StreamBridge 发送到 MQ。
     * 用于编程式发送（非 HTTP 入口场景）。
     * </p>
     *
     * @param msg 消息载荷
     * @return 消息 ID
     */
    Serializable send(Msg msg);

    /**
     * 接收 Stream 消息并转发到 SSE.
     * <p>
     * MQ 消费者回调调用此方法。默认实现将消息转发到 SseConnectionManager，
     * 由 SSE 连接管理器推送到所有订阅该 Topic 的客户端。
     * </p>
     *
     * @param message 来自 Stream 的消息
     */
    void receive(TopicMessage<Msg> message);

    /**
     * {@link Consumer} 入口，MQ 消费者回调.
     * <p>
     * 默认委托给 {@link #receive(TopicMessage)}。自定义实现可重写此方法
     * 以自定义消费行为，或直接重写 {@link #receive} 即可。
     * </p>
     *
     * @param message 来自 Stream 的消息
     */
    @Override
    default void accept(TopicMessage<Msg> message) {
        receive(message);
    }
}
