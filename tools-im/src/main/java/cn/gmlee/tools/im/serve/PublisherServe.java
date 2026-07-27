package cn.gmlee.tools.im.serve;

import cn.gmlee.tools.im.conf.StreamProperties;
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
public class PublisherServe implements Publisher<Serializable, Msg> {

    private final StreamBridge streamBridge;

    private final StreamProperties streamProperties;

    @Override
    public String topic() {
        return "sse-im-broadcast";
    }

    @Override
    public Serializable push(MultiValueMap<String, String> urlParams, Msg msg) {
        TopicMessage<Msg> event = msg.build(urlParams);
        streamBridge.send(topic(), event);
        return event.getId();
    }
}
