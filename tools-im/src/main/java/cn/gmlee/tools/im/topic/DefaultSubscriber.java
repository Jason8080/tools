package cn.gmlee.tools.im.topic;

import cn.gmlee.tools.im.core.ImSubscriber;
import cn.gmlee.tools.im.core.Repeater;

import java.util.function.Supplier;

/**
 * 默认消息订阅器.
 * <p>
 * 继承 {@link ImSubscriber} 框架骨架，所有逻辑（Repeater 委托、日志）
 * 由骨架提供。自定义订阅器可继承此类并重写 {@code pull()} 方法。
 * </p>
 *
 * @since 5.6.0
 */
public class DefaultSubscriber extends ImSubscriber {

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
