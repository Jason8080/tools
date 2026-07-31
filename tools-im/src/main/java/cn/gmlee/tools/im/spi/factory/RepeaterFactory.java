package cn.gmlee.tools.im.spi.factory;

import cn.gmlee.tools.im.core.Repeater;
import cn.gmlee.tools.im.model.Msg;

import java.io.Serializable;

/**
 * Repeater 工厂.
 * <p>
 * 框架扩展点，用于创建自定义 {@link Repeater} 实例。
 * 实现类注册为 Spring Bean 后，{@code TopicRegistry} 优先使用自定义工厂，
 * 否则使用框架默认工厂。
 * </p>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * @Component
 * public class ChatRepeaterFactory implements RepeaterFactory<Long, ChatMsg> {
 *     @Override
 *     public Repeater<Long, ChatMsg> create(String topic, RepeaterContext context) {
 *         if ("im.chat".equals(topic)) {
 *             return new ChatRepeater(topic, context);
 *         }
 *         return null; // 使用默认工厂
 *     }
 * }
 * }</pre>
 *
 * @param <ID>  消息 ID 类型
 * @param <MSG> 消息载荷类型
 * @since 5.6.0
 * @see RepeaterContext
 */
public interface RepeaterFactory<ID extends Serializable, MSG extends Msg>
        extends ComponentFactory<Repeater<ID, MSG>, RepeaterContext> {

    /**
     * 创建 Repeater 实例.
     *
     * @param topic   Topic 名称
     * @param context Repeater 创建上下文（封装 SSE 发布/订阅函数）
     * @return Repeater 实例，返回 {@code null} 表示使用默认工厂
     */
    Repeater<ID, MSG> create(String topic, RepeaterContext context);
}
