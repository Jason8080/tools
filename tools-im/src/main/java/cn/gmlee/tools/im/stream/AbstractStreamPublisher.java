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

    @Override
    public ID push(MultiValueMap<String, String> urlParams, MSG msg) {
        TopicMessage<MSG> event = msg.build(urlParams);
        streamBridge.send(topic(), event);
        return (ID) event.getId();
    }
}
