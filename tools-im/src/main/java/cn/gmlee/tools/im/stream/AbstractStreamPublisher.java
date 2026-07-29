package cn.gmlee.tools.im.stream;

import cn.gmlee.tools.im.core.Msg;
import cn.gmlee.tools.im.core.Publisher;
import cn.gmlee.tools.im.core.TopicMessage;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.util.MultiValueMap;

import java.io.Serializable;

/**
 * 基于 Spring Cloud Stream 的发布者抽象基类.
 * <p>
 * 封装通用的 StreamBridge 发送逻辑，子类只需实现 {@link #topic()} 指定主题名称。
 * 默认情况下，MQ destination 与 topic 相同，如需发送到不同的 destination，可重写 {@link #destination()} 方法。
 * </p>
 *
 * <h3>泛型参数</h3>
 * <ul>
 *   <li>{@code ID} — 消息 ID 类型，必须实现 {@link Serializable}</li>
 *   <li>{@code MSG} — 消息类型，必须实现 {@link Msg}</li>
 * </ul>
 *
 * <h3>典型用法</h3>
 * <pre>{@code
 * @Component
 * public class OrderPublisher extends AbstractStreamPublisher<Serializable, Msg> {
 *     public OrderPublisher(StreamBridge streamBridge) {
 *         super(streamBridge);
 *     }
 *
 *     @Override
 *     public String topic() {
 *         return "order.update";
 *     }
 * }
 * }</pre>
 *
 * <h3>自定义 MQ destination</h3>
 * <pre>{@code
 * @Component
 * public class OrderPublisher extends AbstractStreamPublisher<Serializable, Msg> {
 *     public OrderPublisher(StreamBridge streamBridge) {
 *         super(streamBridge);
 *     }
 *
 *     @Override
 *     public String topic() {
 *         return "order.update";  // API 路由使用
 *     }
 *
 *     @Override
 *     public String destination() {
 *         return "order-events";  // MQ destination
 *     }
 * }
 * }</pre>
 *
 * @param <ID>  消息 ID 类型
 * @param <MSG> 消息类型
 */
public abstract class AbstractStreamPublisher<ID extends Serializable, MSG extends Msg> implements Publisher<ID, MSG> {

    private final StreamBridge streamBridge;

    /**
     * 创建 Stream 发布者.
     *
     * @param streamBridge Spring Cloud Stream 桥接器
     */
    protected AbstractStreamPublisher(StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
    }

    /**
     * 获取消息目标（MQ destination）.
     * <p>
     * 默认返回 {@link #topic()}，即 topic 名称同时作为 MQ destination。
     * 如需将消息发送到不同的 MQ destination，可重写此方法。
     * </p>
     *
     * @return MQ destination 名称
     */
    public String destination() {
        return topic();
    }

    @Override
    public ID push(MultiValueMap<String, String> urlParams, MSG msg) {
        TopicMessage<MSG> event = msg.build(urlParams);
        // 使用 topic-out-0 作为 binding 名称，符合 Spring Cloud Stream 函数式绑定规范
        String bindingName = topic() + "-out-0";
        streamBridge.send(bindingName, event);
        return (ID) event.getId();
    }
}
