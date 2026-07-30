package cn.gmlee.tools.im.topic;

import cn.gmlee.tools.im.model.Msg;
import cn.gmlee.tools.im.core.Publisher;
import cn.gmlee.tools.im.core.Repeater;
import cn.gmlee.tools.im.model.TopicMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.MultiValueMap;

import java.io.Serializable;

/**
 * 默认消息发布器.
 * <p>
 * 发送完全委托给 {@link Repeater#send(TopicMessage)}，自身仅返回消息 ID。
 * </p>
 * <p>
 * Repeater 由 {@link TopicRegistry} 在创建时注入；若构造时未注入（如通过
 * {@link TopicRegistry#createDefaultPublisher(String)} 工厂方法创建），
 * 则在首次 {@link #push} 时通过 {@link #resolveRepeater()} 延迟解析。
 * </p>
 *
 * @since 5.6.0
 */
@Slf4j
public class DefaultPublisher extends AbstractTopicComponent implements Publisher {

    public DefaultPublisher(String topic, Repeater repeater) {
        super(topic, repeater);
    }

    /**
     * 供 {@link TopicRegistry} 工厂方法使用，延迟解析 Repeater.
     */
    DefaultPublisher(String topic, TopicRegistry topicRegistry) {
        super(topic, topicRegistry);
    }

    @Override
    public Serializable push(MultiValueMap<String, String> urlParams, Msg msg) {
        TopicMessage<Msg> event = msg.build(urlParams);
        event.setTopic(topic);
        Serializable id = resolveRepeater().send(event);
        log.debug("[DefaultPublisher] 发布消息: topic={}, id={}", topic, id);
        return id;
    }
}
