package cn.gmlee.tools.im.spi.factory;

import cn.gmlee.tools.im.core.Publisher;
import cn.gmlee.tools.im.core.Repeater;
import cn.gmlee.tools.im.model.Msg;

import java.io.Serializable;
import java.util.function.Supplier;

/**
 * Publisher 工厂.
 * <p>
 * 框架扩展点，用于创建自定义 {@link Publisher} 实例。
 * 实现类注册为 Spring Bean 后，{@code TopicRegistry} 优先使用自定义工厂，
 * 否则使用框架默认工厂。
 * </p>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * @Component
 * public class ChatPublisherFactory implements PublisherFactory<Long, ChatMsg> {
 *     @Override
 *     public Publisher<Long, ChatMsg> create(String topic, Supplier<Repeater<Long, ChatMsg>> repeaterSupplier) {
 *         if ("im.chat".equals(topic)) {
 *             return new ChatPublisher(topic, repeaterSupplier);
 *         }
 *         return null; // 使用默认工厂
 *     }
 * }
 * }</pre>
 *
 * @param <ID>  消息 ID 类型
 * @param <MSG> 消息载荷类型
 * @since 5.6.0
 */
public interface PublisherFactory<ID extends Serializable, MSG extends Msg>
        extends ComponentFactory<Publisher<ID, MSG>, Supplier<Repeater<ID, MSG>>> {

    /**
     * 创建 Publisher 实例.
     *
     * @param topic            Topic 名称
     * @param repeaterSupplier Repeater 延迟解析器（首次使用时调用）
     * @return Publisher 实例，返回 {@code null} 表示使用默认工厂
     */
    Publisher<ID, MSG> create(String topic, Supplier<Repeater<ID, MSG>> repeaterSupplier);
}
