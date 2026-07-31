package cn.gmlee.tools.im.spi.factory;

import cn.gmlee.tools.im.model.ConnectionMetadata;
import cn.gmlee.tools.im.model.Msg;
import cn.gmlee.tools.im.model.TopicMessage;
import lombok.Getter;
import reactor.core.publisher.Flux;

import java.util.function.BiFunction;
import java.util.function.Consumer;

/**
 * Repeater 创建上下文.
 * <p>
 * 封装创建 Repeater 所需的依赖项，避免工厂方法参数过多。
 * 提供 SSE 发布和订阅的函数式接口，Repeater 可通过这些接口与 SSE 连接交互。
 * </p>
 *
 * <h3>设计原则</h3>
 * <ul>
 *   <li><b>封装性</b>：隐藏框架内部组件（如 StreamBridge），仅暴露必要的函数式接口</li>
 *   <li><b>不可变性</b>：所有字段在构造时设置，不可修改</li>
 *   <li><b>线程安全</b>：函数式接口本身是线程安全的</li>
 * </ul>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * @Component
 * public class CustomRepeaterFactory implements RepeaterFactory {
 *     @Override
 *     public Repeater create(String topic, RepeaterContext context) {
 *         if ("im.chat".equals(topic)) {
 *             return new ChatRepeater(topic, context);
 *         }
 *         return null; // 使用默认实现
 *     }
 * }
 *
 * public class ChatRepeater extends ImRepeater {
 *     public ChatRepeater(String topic, RepeaterContext context) {
 *         super(topic, context);
 *     }
 *
 *     @Override
 *     protected void doReceive(TopicMessage message) {
 *         // 自定义接收逻辑
 *         super.doReceive(message);
 *     }
 * }
 * }</pre>
 *
 * @since 5.6.0
 * @see RepeaterFactory
 * @see cn.gmlee.tools.im.core.ImRepeater
 */
@Getter
public class RepeaterContext {

    /**
     * SSE 发布函数：将消息推送到指定 Topic 的所有 SSE 连接.
     * <p>
     * 对应 {@code SseConnectionManager::publish} 方法。
     * </p>
     */
    private final Consumer<TopicMessage> publishFunction;

    /**
     * SSE 订阅函数：获取指定 Topic 的实时消息流.
     * <p>
     * 对应 {@code SseConnectionManager::subscribe} 方法。
     * </p>
     */
    private final BiFunction<String, ConnectionMetadata, Flux<TopicMessage>> subscribeFunction;

    /**
     * 创建 Repeater 上下文.
     *
     * @param publishFunction   SSE 发布函数（不可为 null）
     * @param subscribeFunction SSE 订阅函数（不可为 null）
     */
    public RepeaterContext(Consumer<TopicMessage> publishFunction,
                           BiFunction<String, ConnectionMetadata, Flux<TopicMessage>> subscribeFunction) {
        if (publishFunction == null) {
            throw new IllegalArgumentException("publishFunction must not be null");
        }
        if (subscribeFunction == null) {
            throw new IllegalArgumentException("subscribeFunction must not be null");
        }
        this.publishFunction = publishFunction;
        this.subscribeFunction = subscribeFunction;
    }
}
