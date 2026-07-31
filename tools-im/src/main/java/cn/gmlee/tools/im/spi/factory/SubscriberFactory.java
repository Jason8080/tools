package cn.gmlee.tools.im.spi.factory;

import cn.gmlee.tools.im.core.Repeater;
import cn.gmlee.tools.im.core.Subscriber;
import cn.gmlee.tools.im.model.Msg;

import java.util.function.Supplier;

/**
 * Subscriber 工厂.
 * <p>
 * 框架扩展点，用于创建自定义 {@link Subscriber} 实例。
 * 实现类注册为 Spring Bean 后，{@code TopicRegistry} 优先使用自定义工厂，
 * 否则使用框架默认工厂。
 * </p>
 * <p>
 * <b>注意</b>：Subscriber 不涉及消息 ID 类型，因此仅有 {@code MSG} 泛型参数。
 * 上下文类型为 {@code Supplier<Repeater<?, MSG>>}，因为 Subscriber 不关心 Repeater 的 ID 类型。
 * </p>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * @Component
 * public class ChatSubscriberFactory implements SubscriberFactory<ChatMsg> {
 *     @Override
 *     public Subscriber<ChatMsg> create(String topic, Supplier<Repeater<?, ChatMsg>> repeaterSupplier) {
 *         if ("im.chat".equals(topic)) {
 *             return new ChatSubscriber(topic, repeaterSupplier);
 *         }
 *         return null; // 使用默认工厂
 *     }
 * }
 * }</pre>
 *
 * @param <MSG> 消息载荷类型
 * @since 5.6.0
 */
public interface SubscriberFactory<MSG extends Msg>
        extends ComponentFactory<Subscriber<MSG>, Supplier<Repeater<?, MSG>>> {

    /**
     * 创建 Subscriber 实例.
     *
     * @param topic            Topic 名称
     * @param repeaterSupplier Repeater 延迟解析器（首次使用时调用）
     * @return Subscriber 实例，返回 {@code null} 表示使用默认工厂
     */
    Subscriber<MSG> create(String topic, Supplier<Repeater<?, MSG>> repeaterSupplier);
}
