package cn.gmlee.tools.im.topic;

import cn.gmlee.tools.im.core.ImRepeater;
import cn.gmlee.tools.im.spi.RepeaterInterceptor;
import cn.gmlee.tools.im.model.Msg;
import cn.gmlee.tools.im.model.TopicMessage;
import cn.gmlee.tools.im.sse.SseConnectionManager;
import cn.gmlee.tools.im.util.BindingNames;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.util.MultiValueMap;
import reactor.core.publisher.Flux;

import java.io.Serializable;
import java.util.List;

/**
 * 默认消息转发器.
 * <p>
 * 继承 {@link ImRepeater} 框架骨架，拦截器自动织入。
 * 三向桥接：
 * </p>
 * <ul>
 *   <li>{@link #doSend(TopicMessage)} — 通过 {@link StreamBridge} 将消息发送到 MQ</li>
 *   <li>{@link #doReceive(TopicMessage)} — 通过 {@link SseConnectionManager} 将 MQ 消息转发到 SSE 连接</li>
 *   <li>{@link #doSubscribe(MultiValueMap)} — 通过 {@link SseConnectionManager} 提供 SSE 实时消息流</li>
 * </ul>
 *
 * @since 5.6.0
 */
@Slf4j
public class DefaultRepeater extends ImRepeater {

    private final String topic;
    private final StreamBridge streamBridge;
    private final SseConnectionManager sseConnectionManager;

    public DefaultRepeater(String topic,
                           StreamBridge streamBridge,
                           SseConnectionManager sseConnectionManager,
                           List<RepeaterInterceptor> interceptors) {
        super(interceptors);
        this.topic = topic;
        this.streamBridge = streamBridge;
        this.sseConnectionManager = sseConnectionManager;
    }

    @Override
    public String topic() {
        return topic;
    }

    @Override
    protected Serializable doSend(TopicMessage<Msg> message) {
        String bindingName = BindingNames.outputBinding(message.getTopic());
        streamBridge.send(bindingName, message);
        log.debug("[DefaultRepeater] 发送到 Stream: topic={}, id={}", message.getTopic(), message.getId());
        return message.getId();
    }

    @Override
    protected void doReceive(TopicMessage<Msg> message) {
        sseConnectionManager.publish(message);
        log.debug("[DefaultRepeater] 转发到 SSE: topic={}, id={}", topic, message.getId());
    }

    @Override
    protected Flux<Msg> doSubscribe(MultiValueMap<String, String> urlParams) {
        log.debug("[DefaultRepeater] 订阅消息流: topic={}", topic);
        return sseConnectionManager.subscribe(topic)
                .map(TopicMessage::getMsg);
    }
}
