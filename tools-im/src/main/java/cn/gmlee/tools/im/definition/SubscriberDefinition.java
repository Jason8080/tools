package cn.gmlee.tools.im.definition;

import cn.gmlee.tools.im.core.Msg;
import cn.gmlee.tools.im.core.Subscriber;
import cn.gmlee.tools.im.core.Topic;
import cn.gmlee.tools.im.core.TopicMessage;
import cn.gmlee.tools.im.sse.SseConnectionManager;
import cn.gmlee.tools.im.stream.AbstractSseSubscriber;
import cn.gmlee.tools.im.stream.AbstractStreamConsumer;
import reactor.core.publisher.Flux;

import java.util.function.Consumer;

/**
 * 订阅者定义接口（转 + 出 - 拉取端）.
 * <p>
 * 用于定义 Topic 的 Consumer（转）和 Subscriber（出）组件，适用于微服务场景下的拉取端服务。
 * 拉取端服务只需实现此接口，无需创建 Publisher 组件。
 * </p>
 *
 * <h3>泛型参数</h3>
 * <ul>
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
 * <h3>组件说明</h3>
 * <ul>
 *   <li><b>Consumer（转）</b>：从 Spring Cloud Stream 接收消息，转发给本地 {@link SseConnectionManager}</li>
 *   <li><b>Subscriber（出）</b>：从 {@link SseConnectionManager} 获取 Flux，供 SSE pull 端点输出</li>
 * </ul>
 *
 * <h3>典型用法（使用默认类型）</h3>
 * <pre>{@code
 * @Component
 * public class OrderSubscriberDefinition implements SubscriberDefinition<Msg> {
 *     @Override
 *     public String topic() {
 *         return "order.update";
 *     }
 *     // 使用默认实现，无需重写 createConsumer/createSubscriber
 *     // MQ destination 自动使用 topic() 的值
 * }
 * }</pre>
 *
 * <h3>自定义消费者组</h3>
 * <pre>{@code
 * @Component
 * public class OrderSubscriberDefinition implements SubscriberDefinition<Msg> {
 *     @Override
 *     public String topic() {
 *         return "order.update";
 *     }
 *
 *     @Override
 *     public String group() {
 *         return "order-service-group";  // 指定消费者组
 *     }
 * }
 * }</pre>
 *
 * <h3>自定义消息类型</h3>
 * <pre>{@code
 * @Component
 * public class OrderSubscriberDefinition implements SubscriberDefinition<OrderMsg> {
 *     @Override
 *     public String topic() {
 *         return "order.update";
 *     }
 *     // 使用默认实现，无需重写 createConsumer/createSubscriber
 * }
 * }</pre>
 *
 * <h3>自定义场景</h3>
 * <p>
 * 如需自定义消费逻辑或订阅逻辑，可重写对应的 create 方法：
 * </p>
 * <pre>{@code
 * @Override
 * public Consumer<TopicMessage<OrderMsg>> createConsumer(SseConnectionManager sseConnectionManager) {
 *     return new AbstractStreamConsumer<OrderMsg>(sseConnectionManager) {
 *         @Override
 *         public void accept(TopicMessage<OrderMsg> message) {
 *             // 自定义消费逻辑（如添加日志、指标等）
 *             super.accept(message);
 *         }
 *     };
 * }
 *
 * @Override
 * public Subscriber<OrderMsg> createSubscriber(SseConnectionManager sseConnectionManager) {
 *     return new AbstractSseSubscriber<OrderMsg>(sseConnectionManager) {
 *         @Override
 *         public String topic() { return OrderSubscriberDefinition.this.topic(); }
 *
 *         @Override
 *         public Flux<OrderMsg> pull(MultiValueMap<String, String> urlParams) {
 *             // 自定义订阅逻辑（如根据 URL 参数过滤）
 *             return super.pull(urlParams);
 *         }
 *     };
 * }
 * }</pre>
 *
 * @param <MSG> 消息类型
 * @see PublisherDefinition 发送端定义接口
 * @see TopicDefinition 单进程场景的完整定义接口
 * @since 5.6.0
 */
public interface SubscriberDefinition<MSG extends Msg> extends Topic {

    /**
     * 获取消费者组名称.
     * <p>
     * 默认返回 {@code null}，表示使用应用名称作为消费者组。
     * 同一消费者组的实例会负载均衡消费消息，不同组的实例会各自收到全部消息。
     * </p>
     * <p>
     * 如需明确指定消费者组（如多实例部署时确保消息只被消费一次），可重写此方法。
     * </p>
     *
     * @return 消费者组名称，或 null 使用默认值
     */
    default String group() {
        return null;
    }

    /**
     * 创建消费者（转）.
     * <p>
     * 默认实现使用 {@link AbstractStreamConsumer}，子类可重写以自定义消费逻辑。
     * </p>
     *
     * @param sseConnectionManager SSE 连接管理器
     * @return 消费者实例
     */
    default Consumer<TopicMessage<MSG>> createConsumer(SseConnectionManager sseConnectionManager) {
        return new AbstractStreamConsumer<MSG>(sseConnectionManager) {};
    }

    /**
     * 创建订阅者（出）.
     * <p>
     * 默认实现使用 {@link AbstractSseSubscriber}，子类可重写以自定义订阅逻辑。
     * </p>
     *
     * @param sseConnectionManager SSE 连接管理器
     * @return 订阅者实例
     */
    default Subscriber<MSG> createSubscriber(SseConnectionManager sseConnectionManager) {
        return new AbstractSseSubscriber<MSG>(sseConnectionManager) {
            @Override
            public String topic() {
                return SubscriberDefinition.this.topic();
            }
        };
    }
}
