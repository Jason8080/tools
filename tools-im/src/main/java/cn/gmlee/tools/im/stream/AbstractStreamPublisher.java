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
 * <h3>典型用法</h3>
 * <pre>{@code
 * @Component
 * public class OrderPublisher extends AbstractStreamPublisher {
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
 */
public abstract class AbstractStreamPublisher implements Publisher<Serializable, Msg> {

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
    public Serializable push(MultiValueMap<String, String> urlParams, Msg msg) {
        TopicMessage<Msg> event = msg.build(urlParams);
        streamBridge.send(topic(), event);
        return event.getId();
    }
}
