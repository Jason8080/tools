package cn.gmlee.tools.im.topic;

import cn.gmlee.tools.im.core.Msg;
import cn.gmlee.tools.im.core.Publisher;
import cn.gmlee.tools.im.core.TopicMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.util.MultiValueMap;

import java.io.Serializable;

/**
 * 发布服务
 */
@RequiredArgsConstructor
public class ImBroadcastPublisher implements Publisher<Serializable, Msg> {

    private final StreamBridge streamBridge;

    @Override
    public String topic() {
        return "im.broadcast";
    }

    @Override
    public Serializable push(MultiValueMap<String, String> urlParams, Msg msg) {
        TopicMessage<Msg> event = msg.build(urlParams);
        streamBridge.send(topic(), event);
        return event.getId();
    }
}
