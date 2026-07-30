package cn.gmlee.tools.im.topic;

import cn.gmlee.tools.im.core.Msg;
import cn.gmlee.tools.im.core.Repeater;
import cn.gmlee.tools.im.core.Subscriber;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.MultiValueMap;
import reactor.core.publisher.Flux;

/**
 * 默认消息订阅器.
 * <p>
 * 完全委托给 {@link Repeater#subscribe()}，自身仅做日志记录。
 * </p>
 * <p>
 * Repeater 由 {@link TopicRegistry} 在创建时注入；若构造时未注入（如通过
 * {@link TopicRegistry#createDefaultSubscriber(String)} 工厂方法创建），
 * 则在首次 {@link #pull} 时延迟解析。
 * </p>
 *
 * @since 5.6.0
 */
@Slf4j
public class DefaultSubscriber implements Subscriber {

    private final String topic;
    private volatile Repeater repeater;
    private final TopicRegistry topicRegistry;

    public DefaultSubscriber(String topic, Repeater repeater) {
        this.topic = topic;
        this.repeater = repeater;
        this.topicRegistry = null;
    }

    /**
     * 供 {@link TopicRegistry} 工厂方法使用，延迟解析 Repeater.
     */
    DefaultSubscriber(String topic, TopicRegistry topicRegistry) {
        this.topic = topic;
        this.repeater = null;
        this.topicRegistry = topicRegistry;
    }

    @Override
    public String topic() {
        return topic;
    }

    @Override
    public Flux<Msg> pull(MultiValueMap<String, String> urlParams) {
        log.debug("[DefaultSubscriber] 订阅消息流: topic={}", topic);
        return resolveRepeater().subscribe();
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
