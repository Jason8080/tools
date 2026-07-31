package cn.gmlee.tools.im.topic;

import cn.gmlee.tools.im.core.Repeater;
import cn.gmlee.tools.im.model.Msg;
import cn.gmlee.tools.im.model.TopicMessage;

import java.util.function.Consumer;

/**
 * Consumer 桥接器（包级私有）.
 * <p>
 * 将泛型 {@link Repeater} 桥接到 SSE 管道所需的 {@code Consumer<TopicMessage>} 接口。
 * </p>
 *
 * <h3>设计说明</h3>
 * <ul>
 *   <li>Spring Cloud Stream 通过 {@code GenericTypeResolver} 解析 Consumer Bean 的类型参数。
 *       直接注册泛型 {@code Repeater<ID, MSG>} 会导致 Spring 无法解析类型参数。</li>
 *   <li>{@code ConsumerBridge} 是非泛型类，明确实现 {@code Consumer<TopicMessage>}，
 *       Spring 可正确解析出载荷类型 {@code TopicMessage}。</li>
 *   <li>内部持有 raw {@link Repeater} 引用，通过运行时类型擦除安全分派消息。</li>
 * </ul>
 *
 * @since 5.6.0
 */
@SuppressWarnings({"rawtypes", "unchecked"})
class ConsumerBridge implements Consumer<TopicMessage> {

    private final Repeater repeater;

    ConsumerBridge(Repeater repeater) {
        this.repeater = repeater;
    }

    @Override
    public void accept(TopicMessage message) {
        repeater.receive(message);
    }
}
