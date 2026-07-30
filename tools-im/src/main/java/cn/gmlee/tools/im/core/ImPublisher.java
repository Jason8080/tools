package cn.gmlee.tools.im.core;

import cn.gmlee.tools.im.model.Msg;
import cn.gmlee.tools.im.model.TopicMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.MultiValueMap;

import java.io.Serializable;
import java.util.function.Supplier;

/**
 * Publisher 框架骨架.
 * <p>
 * 提供 Topic 管理、Repeater 延迟解析（双检锁）和日志记录。
 * 子类只需提供构造器即可组成可用的默认发布器。
 * </p>
 *
 * <h3>扩展方式</h3>
 * <p>
 * 继承此类即可获得 Repeater 解析和日志能力。
 * 可重写 {@link #push(MultiValueMap, Msg)} 自定义发布逻辑。
 * </p>
 *
 * @since 5.6.0
 */
@Slf4j
public abstract class ImPublisher implements Publisher {

    protected final String topic;
    private volatile Repeater repeater;
    private final Supplier<Repeater> repeaterSupplier;

    /**
     * 直接注入 Repeater.
     *
     * @param topic    Topic 名称
     * @param repeater 已解析的 Repeater 实例
     */
    protected ImPublisher(String topic, Repeater repeater) {
        this.topic = topic;
        this.repeater = repeater;
        this.repeaterSupplier = null;
    }

    /**
     * 延迟解析 Repeater.
     *
     * @param topic            Topic 名称
     * @param repeaterSupplier Repeater 解析器（首次使用时调用）
     */
    protected ImPublisher(String topic, Supplier<Repeater> repeaterSupplier) {
        this.topic = topic;
        this.repeater = null;
        this.repeaterSupplier = repeaterSupplier;
    }

    @Override
    public final String topic() {
        return topic;
    }

    @Override
    public Serializable push(MultiValueMap<String, String> urlParams, Msg msg) {
        TopicMessage<Msg> event = msg.build(urlParams);
        event.setTopic(topic);
        Serializable id = resolveRepeater().send(event);
        log.debug("[ImPublisher] 发布消息: topic={}, id={}", topic, id);
        return id;
    }

    /**
     * 获取 Repeater 实例（双检锁延迟解析）.
     *
     * @return Repeater 实例
     */
    protected final Repeater resolveRepeater() {
        Repeater r = this.repeater;
        if (r != null) {
            return r;
        }
        synchronized (this) {
            if (this.repeater != null) {
                return this.repeater;
            }
            this.repeater = repeaterSupplier.get();
            return this.repeater;
        }
    }
}
