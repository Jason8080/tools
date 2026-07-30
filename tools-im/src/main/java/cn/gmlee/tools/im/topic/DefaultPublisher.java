package cn.gmlee.tools.im.topic;

import cn.gmlee.tools.im.core.BindingNames;
import cn.gmlee.tools.im.core.Msg;
import cn.gmlee.tools.im.core.Publisher;
import cn.gmlee.tools.im.core.TopicMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.util.MultiValueMap;

import java.io.Serializable;

/**
 * 默认消息发布器.
 * <p>
 * 将消息包装为 {@link TopicMessage}，通过 {@link StreamBridge} 发送到 MQ。
 * 框架在端点注册时自动创建，开发者通常无需直接使用。
 * </p>
 *
 * @since 5.6.0
 */
@Slf4j
public class DefaultPublisher implements Publisher {

    private final String topic;
    private final StreamBridge streamBridge;

    public DefaultPublisher(String topic, StreamBridge streamBridge) {
        this.topic = topic;
        this.streamBridge = streamBridge;
    }

    @Override
    public String topic() {
        return topic;
    }

    @Override
    public Serializable push(MultiValueMap<String, String> urlParams, Msg msg) {
        TopicMessage<Msg> event = msg.build(urlParams);
        event.setTopic(topic);
        String bindingName = BindingNames.outputBinding(topic);
        streamBridge.send(bindingName, event);
        log.debug("[DefaultPublisher] 发布消息: topic={}, id={}", topic, event.getId());
        return event.getId();
    }
}
