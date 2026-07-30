package cn.gmlee.tools.im.topic;

import cn.gmlee.tools.im.core.ImSubscriber;
import cn.gmlee.tools.im.core.Repeater;

import java.util.function.Supplier;

/**
 * 默认消息订阅器（包级私有）.
 * <p>
 * 继承 {@link ImSubscriber} 框架骨架，所有逻辑（Repeater 委托、日志）
 * 由骨架提供。仅供 {@link TopicRegistry} 内部使用。
 * </p>
 * <p>
 * 自定义订阅器应继承 {@link ImSubscriber}，并通过 {@link SubscriberFactory} 创建。
 * </p>
 *
 * @since 5.6.0
 */
class DefaultSubscriber extends ImSubscriber {

    public DefaultSubscriber(String topic, Repeater repeater) {
        super(topic, repeater);
    }

    /**
     * 供 {@link TopicRegistry} 工厂方法使用，延迟解析 Repeater.
     */
    DefaultSubscriber(String topic, Supplier<Repeater> repeaterSupplier) {
        super(topic, repeaterSupplier);
    }
}
