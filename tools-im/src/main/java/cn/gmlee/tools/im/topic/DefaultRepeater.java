package cn.gmlee.tools.im.topic;

import cn.gmlee.tools.im.core.BindingNames;
import cn.gmlee.tools.im.core.Msg;
import cn.gmlee.tools.im.core.Repeater;
import cn.gmlee.tools.im.core.TopicMessage;
import cn.gmlee.tools.im.sse.SseConnectionManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;

import java.io.Serializable;

/**
 * 默认消息转发器.
 * <p>
 * 双向桥接：
 * </p>
 * <ul>
 *   <li>{@link #send(TopicMessage)} — 通过 {@link StreamBridge} 将消息发送到 MQ</li>
 *   <li>{@link #receive(TopicMessage)} — 通过 {@link SseConnectionManager} 将消息转发到 SSE 连接</li>
 * </ul>
 *
 * @since 5.6.0
 */
@Slf4j
public class DefaultRepeater implements Repeater {

    private final String topic;
    private final StreamBridge streamBridge;
    private final SseConnectionManager sseConnectionManager;

    public DefaultRepeater(String topic,
                           StreamBridge streamBridge,
                           SseConnectionManager sseConnectionManager) {
        this.topic = topic;
        this.streamBridge = streamBridge;
        this.sseConnectionManager = sseConnectionManager;
    }

    @Override
    public String topic() {
        return topic;
    }

    @Override
    public Serializable send(TopicMessage<Msg> message) {
        String bindingName = BindingNames.outputBinding(message.getTopic());
        streamBridge.send(bindingName, message);
        log.debug("[DefaultRepeater] 发送到 Stream: topic={}, id={}", message.getTopic(), message.getId());
        return message.getId();
    }

    @Override
    public void receive(TopicMessage<Msg> message) {
        sseConnectionManager.publish(message);
        log.debug("[DefaultRepeater] 转发到 SSE: topic={}, id={}", topic, message.getId());
    }
}
