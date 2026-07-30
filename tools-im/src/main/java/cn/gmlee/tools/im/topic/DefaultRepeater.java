package cn.gmlee.tools.im.topic;

import cn.gmlee.tools.im.core.BindingNames;
import cn.gmlee.tools.im.core.Msg;
import cn.gmlee.tools.im.core.Repeater;
import cn.gmlee.tools.im.spi.RepeaterInterceptor;
import cn.gmlee.tools.im.core.TopicMessage;
import cn.gmlee.tools.im.sse.SseConnectionManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.util.MultiValueMap;
import reactor.core.publisher.Flux;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * 默认消息转发器.
 * <p>
 * 三向桥接：
 * </p>
 * <ul>
 *   <li>{@link #send(TopicMessage)} — 通过 {@link StreamBridge} 将消息发送到 MQ</li>
 *   <li>{@link #receive(TopicMessage)} — 通过 {@link SseConnectionManager} 将 MQ 消息转发到 SSE 连接</li>
 *   <li>{@link #subscribe(MultiValueMap)} — 通过 {@link SseConnectionManager} 提供 SSE 实时消息流</li>
 * </ul>
 * <p>
 * 在各关键节点织入 {@link RepeaterInterceptor} 拦截器链：
 * </p>
 * <ul>
 *   <li>{@code send} 前调用 {@link RepeaterInterceptor#beforeSend}</li>
 *   <li>{@code receive} 后调用 {@link RepeaterInterceptor#afterReceive}</li>
 *   <li>{@code subscribe} 流经 {@link RepeaterInterceptor#transformSubscribeStream} 逐级转换</li>
 * </ul>
 *
 * @since 5.6.0
 */
@Slf4j
public class DefaultRepeater implements Repeater {

    private final String topic;
    private final StreamBridge streamBridge;
    private final SseConnectionManager sseConnectionManager;
    private final List<RepeaterInterceptor> interceptors;

    public DefaultRepeater(String topic,
                           StreamBridge streamBridge,
                           SseConnectionManager sseConnectionManager,
                           List<RepeaterInterceptor> interceptors) {
        this.topic = topic;
        this.streamBridge = streamBridge;
        this.sseConnectionManager = sseConnectionManager;
        this.interceptors = interceptors != null ? interceptors : Collections.emptyList();
    }

    @Override
    public String topic() {
        return topic;
    }

    @Override
    public Serializable send(TopicMessage<Msg> message) {
        for (RepeaterInterceptor interceptor : interceptors) {
            interceptor.beforeSend(message);
        }
        String bindingName = BindingNames.outputBinding(message.getTopic());
        streamBridge.send(bindingName, message);
        log.debug("[DefaultRepeater] 发送到 Stream: topic={}, id={}", message.getTopic(), message.getId());
        return message.getId();
    }

    @Override
    public void receive(TopicMessage<Msg> message) {
        sseConnectionManager.publish(message);
        for (RepeaterInterceptor interceptor : interceptors) {
            interceptor.afterReceive(message);
        }
        log.debug("[DefaultRepeater] 转发到 SSE: topic={}, id={}", topic, message.getId());
    }

    @Override
    public Flux<Msg> subscribe(MultiValueMap<String, String> urlParams) {
        log.debug("[DefaultRepeater] 订阅消息流: topic={}", topic);
        Flux<Msg> stream = sseConnectionManager.subscribe(topic)
                .map(TopicMessage::getMsg);
        for (RepeaterInterceptor interceptor : interceptors) {
            stream = interceptor.transformSubscribeStream(topic, stream, urlParams);
        }
        return stream;
    }
}
