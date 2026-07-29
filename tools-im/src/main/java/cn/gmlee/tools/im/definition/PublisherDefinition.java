package cn.gmlee.tools.im.definition;

import cn.gmlee.tools.im.core.Msg;
import cn.gmlee.tools.im.core.Publisher;
import cn.gmlee.tools.im.core.Topic;
import cn.gmlee.tools.im.stream.AbstractStreamPublisher;
import org.springframework.cloud.stream.function.StreamBridge;

import java.io.Serializable;

/**
 * 发布者定义接口（进 - 发送端）.
 * <p>
 * 用于定义 Topic 的 Publisher 组件，适用于微服务场景下的发送端服务。
 * 发送端服务只需实现此接口，无需创建 Consumer 和 Subscriber 组件。
 * </p>
 *
 * <h3>泛型参数</h3>
 * <ul>
 *   <li>{@code ID} — 消息 ID 类型，必须实现 {@link Serializable}</li>
 *   <li>{@code MSG} — 消息类型，必须实现 {@link Msg}</li>
 * </ul>
 *
 * <h3>微服务场景</h3>
 * <p>
 * 在微服务架构中，发送消息和拉取消息通常是两个独立的微服务进程：
 * </p>
 * <ul>
 *   <li><b>发送端服务</b>：只需实现 {@link PublisherDefinition}，负责接收 HTTP push 并发送到 MQ</li>
 *   <li><b>拉取端服务</b>：只需实现 {@link SubscriberDefinition}，负责从 MQ 接收并推送到 SSE</li>
 * </ul>
 *
 * <h3>典型用法（使用默认类型）</h3>
 * <pre>{@code
 * @Component
 * public class OrderPublisherDefinition implements PublisherDefinition<Serializable, Msg> {
 *     @Override
 *     public String topic() {
 *         return "order.update";
 *     }
 *     // 使用默认实现，无需重写 createPublisher
 *     // MQ destination 自动使用 topic() 的值
 * }
 * }</pre>
 *
 * <h3>自定义消息类型</h3>
 * <pre>{@code
 * @Component
 * public class OrderPublisherDefinition implements PublisherDefinition<Long, OrderMsg> {
 *     @Override
 *     public String topic() {
 *         return "order.update";
 *     }
 *
 *     @Override
 *     public Publisher<Long, OrderMsg> createPublisher(StreamBridge streamBridge) {
 *         return new AbstractStreamPublisher<>(streamBridge) {
 *             @Override
 *             public String topic() { return OrderPublisherDefinition.this.topic(); }
 *
 *             @Override
 *             public Long push(MultiValueMap<String, String> urlParams, OrderMsg msg) {
 *                 // 自定义发布逻辑，返回自定义 ID 类型
 *                 TopicMessage<OrderMsg> event = msg.build(urlParams);
 *                 streamBridge.send(topic() + "-out-0", event);
 *                 return event.getId() instanceof Long ? (Long) event.getId() : 0L;
 *             }
 *         };
 *     }
 * }
 * }</pre>
 *
 * @param <ID>  消息 ID 类型
 * @param <MSG> 消息类型
 * @see SubscriberDefinition 拉取端定义接口
 * @see TopicDefinition       单进程场景的完整定义接口
 * @since 5.6.0
 */
public interface PublisherDefinition<ID extends Serializable, MSG extends Msg> extends Topic {

    /**
     * 创建发布者（进）.
     * <p>
     * 默认实现使用 {@link AbstractStreamPublisher}，子类可重写以自定义发布逻辑。
     * </p>
     *
     * @param streamBridge Spring Cloud Stream 桥接器
     * @return 发布者实例
     */
    default Publisher<ID, MSG> createPublisher(StreamBridge streamBridge) {
        return new AbstractStreamPublisher<>(streamBridge) {
            @Override
            public String topic() {
                return PublisherDefinition.this.topic();
            }
        };
    }
}
