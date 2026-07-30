package cn.gmlee.tools.im.topic;

import cn.gmlee.tools.im.core.Msg;
import cn.gmlee.tools.im.core.Publisher;
import cn.gmlee.tools.im.core.Repeater;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.MultiValueMap;

import java.io.Serializable;

/**
 * 默认消息发布器.
 * <p>
 * 发送完全委托给 {@link Repeater#send(Msg)}，自身仅返回消息 ID。
 * </p>
 * <p>
 * Repeater 由 {@link TopicRegistry} 在创建时注入；若构造时未注入（如通过
 * {@link TopicRegistry#createDefaultPublisher(String)} 工厂方法创建），
 * 则在首次 {@link #push} 时延迟解析。
 * </p>
 *
 * @since 5.6.0
 */
@Slf4j
public class DefaultPublisher implements Publisher {

    private final String topic;
    private volatile Repeater repeater;
    private TopicRegistry topicRegistry;

    public DefaultPublisher(String topic, Repeater repeater) {
        this.topic = topic;
        this.repeater = repeater;
    }

    /**
     * 供 {@link TopicRegistry} 工厂方法使用，延迟解析 Repeater.
     */
    DefaultPublisher(String topic, TopicRegistry topicRegistry) {
        this.topic = topic;
        this.topicRegistry = topicRegistry;
    }

    @Override
    public String topic() {
        return topic;
    }

    @Override
    public Serializable push(MultiValueMap<String, String> urlParams, Msg msg) {
        Serializable id = resolveRepeater().send(msg);
        log.debug("[DefaultPublisher] 发布消息: topic={}, id={}", topic, id);
        return id;
    }

    private Repeater resolveRepeater() {
        Repeater r = this.repeater;
        if (r != null) {
            return r;
        }
        synchronized (this) {
            if (this.repeater != null) {
                return this.repeater;
            }
            this.repeater = topicRegistry.getRepeater(topic);
            return this.repeater;
        }
    }
}
