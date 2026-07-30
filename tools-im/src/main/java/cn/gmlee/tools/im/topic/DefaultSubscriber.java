package cn.gmlee.tools.im.topic;

import cn.gmlee.tools.im.core.Msg;
import cn.gmlee.tools.im.core.Subscriber;
import cn.gmlee.tools.im.core.TopicMessage;
import cn.gmlee.tools.im.sse.SseConnectionManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.MultiValueMap;
import reactor.core.publisher.Flux;

/**
 * 默认消息订阅器.
 * <p>
 * 委托 {@link SseConnectionManager#subscribe(String)} 返回 SSE 实时消息流。
 * </p>
 *
 * @since 5.6.0
 */
@Slf4j
public class DefaultSubscriber implements Subscriber {

    private final String topic;
    private final SseConnectionManager sseConnectionManager;

    public DefaultSubscriber(String topic, SseConnectionManager sseConnectionManager) {
        this.topic = topic;
        this.sseConnectionManager = sseConnectionManager;
    }

    @Override
    public String topic() {
        return topic;
    }

    @Override
    public Flux<TopicMessage<Msg>> pull(MultiValueMap<String, String> urlParams) {
        log.debug("[DefaultSubscriber] 订阅消息流: topic={}", topic);
        return sseConnectionManager.subscribe(topic);
    }
}
