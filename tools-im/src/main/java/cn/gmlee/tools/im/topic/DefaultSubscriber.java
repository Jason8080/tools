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
 * 完全委托给 {@link Repeater#subscribe(MultiValueMap)}，自身仅做日志记录。
 * </p>
 * <p>
 * Repeater 由 {@link TopicRegistry} 在创建时注入；若构造时未注入（如通过
 * {@link TopicRegistry#createDefaultSubscriber(String)} 工厂方法创建），
 * 则在首次 {@link #pull} 时通过 {@link #resolveRepeater()} 延迟解析。
 * </p>
 *
 * @since 5.6.0
 */
@Slf4j
public class DefaultSubscriber extends AbstractTopicComponent implements Subscriber {

    public DefaultSubscriber(String topic, Repeater repeater) {
        super(topic, repeater);
    }

    /**
     * 供 {@link TopicRegistry} 工厂方法使用，延迟解析 Repeater.
     */
    DefaultSubscriber(String topic, TopicRegistry topicRegistry) {
        super(topic, topicRegistry);
    }

    @Override
    public Flux<Msg> pull(MultiValueMap<String, String> urlParams) {
        log.debug("[DefaultSubscriber] 订阅消息流: topic={}", topic);
        return resolveRepeater().subscribe(urlParams);
    }
}
