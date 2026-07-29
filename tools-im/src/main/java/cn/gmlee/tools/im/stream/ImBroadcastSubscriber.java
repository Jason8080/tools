package cn.gmlee.tools.im.stream;

import cn.gmlee.tools.im.core.Msg;
import cn.gmlee.tools.im.core.Subscriber;
import cn.gmlee.tools.im.core.TopicMessage;
import cn.gmlee.tools.im.sse.SseConnectionManager;
import lombok.RequiredArgsConstructor;
import org.springframework.util.MultiValueMap;
import reactor.core.publisher.Flux;

/**
 * 订阅服务
 */
@RequiredArgsConstructor
public class ImBroadcastSubscriber implements Subscriber<Flux<Msg>> {

    private final SseConnectionManager sseConnectionManager;

    @Override
    public String topic() {
        return "im.broadcast";
    }

    @Override
    public Flux<Msg> pull(MultiValueMap<String, String> urlParams) {
        return sseConnectionManager.subscribe(topic()).map(TopicMessage::getMsg);
    }

}
