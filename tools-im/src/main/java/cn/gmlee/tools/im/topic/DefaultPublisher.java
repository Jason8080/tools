package cn.gmlee.tools.im.topic;

import cn.gmlee.tools.im.core.ImPublisher;
import cn.gmlee.tools.im.core.Repeater;

import java.util.List;
import java.util.function.Supplier;

/**
 * 默认消息发布器（包级私有）.
 * <p>
 * 继承 {@link ImPublisher} 框架骨架，所有逻辑（TopicMessage 构建、Repeater 委托、日志）
 * 由骨架提供。仅供 {@link TopicRegistry} 内部使用。
 * </p>
 * <p>
 * 自定义发布器应继承 {@link ImPublisher}，并通过 {@link PublisherFactory} 创建。
 * </p>
 *
 * @since 5.6.0
 */
class DefaultPublisher extends ImPublisher {

    public DefaultPublisher(String topic, Repeater repeater) {
        super(topic, repeater);
    }

    /**
     * 供 {@link TopicRegistry} 工厂方法使用，延迟解析 Repeater.
     */
    DefaultPublisher(String topic, Supplier<Repeater> repeaterSupplier) {
        super(topic, repeaterSupplier);
    }

    /**
     * 供 {@link TopicRegistry} 工厂方法使用，延迟解析 Repeater + 自定义路由键.
     */
    DefaultPublisher(String topic, Supplier<Repeater> repeaterSupplier, List<String> routingKeys) {
        super(topic, repeaterSupplier, routingKeys);
    }
}
