package cn.gmlee.tools.im.stream;

import cn.gmlee.tools.im.core.Msg;
import cn.gmlee.tools.im.core.Publisher;
import cn.gmlee.tools.im.core.Subscriber;
import cn.gmlee.tools.im.core.TopicMessage;
import cn.gmlee.tools.im.sse.SseConnectionManager;
import org.springframework.cloud.stream.function.StreamBridge;
import reactor.core.publisher.Flux;

import java.io.Serializable;
import java.util.function.Consumer;

/**
 * 主题定义接口（进-转-出 闭环聚合）.
 * <p>
 * 将一个完整 Topic 的三个组件（Publisher/Consumer/Subscriber）聚合为单一接口，
 * 提供零样板代码的 Topic 定义方式。
 * </p>
 *
 * <h3>典型用法</h3>
 * <pre>{@code
 * @Component
 * public class OrderTopicDefinition implements TopicDefinition {
 *     @Override
 *     public String topic() {
 *         return "order.update";
 *     }
 *     // 使用默认实现，无需重写 createPublisher/createConsumer/createSubscriber
 * }
 * }</pre>
 *
 * <h3>自定义场景</h3>
 * <p>
 * 如需自定义消息构建逻辑或消费逻辑，可重写对应的 create 方法：
 * </p>
 * <pre>{@code
 * @Override
 * public Publisher<Serializable, Msg> createPublisher(StreamBridge streamBridge) {
 *     return new AbstractStreamPublisher(streamBridge) {
 *         @Override
 *         public String topic() { return OrderTopicDefinition.this.topic(); }
 *
 *         @Override
 *         public Serializable push(MultiValueMap<String, String> urlParams, Msg msg) {
 *             // 自定义发布逻辑
 *             return super.push(urlParams, msg);
 *         }
 *     };
 * }
 * }</pre>
 */
public interface TopicDefinition {

    /**
     * 主题名称.
     *
     * @return 主题标识字符串
     */
    String topic();

    /**
     * 创建发布者（进）.
     * <p>
     * 默认实现使用 {@link AbstractStreamPublisher}，子类可重写以自定义发布逻辑。
     * </p>
     *
     * @param streamBridge Spring Cloud Stream 桥接器
     * @return 发布者实例
     */
    default Publisher<Serializable, Msg> createPublisher(StreamBridge streamBridge) {
        return new AbstractStreamPublisher(streamBridge) {
            @Override
            public String topic() {
                return TopicDefinition.this.topic();
            }
        };
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
    default Consumer<TopicMessage<Msg>> createConsumer(SseConnectionManager sseConnectionManager) {
        return new AbstractStreamConsumer(sseConnectionManager) {};
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
    default Subscriber<Flux<Msg>> createSubscriber(SseConnectionManager sseConnectionManager) {
        return new AbstractSseSubscriber(sseConnectionManager) {
            @Override
            public String topic() {
                return TopicDefinition.this.topic();
            }
        };
    }
}
