package cn.gmlee.tools.im.serve;

import cn.gmlee.tools.im.core.Msg;
import cn.gmlee.tools.im.core.MsgEvent;
import cn.gmlee.tools.im.core.Publisher;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.util.MultiValueMap;

import java.io.Serializable;

/**
 * 发布服务
 */
@RequiredArgsConstructor
public class PublisherServe implements Publisher<Msg> {

    private final StreamBridge streamBridge;

    @Override
    public Serializable push(String topic, MultiValueMap<String, String> urlParams, Msg msg) {
        MsgEvent<Msg> event = msg.build(urlParams);
        streamBridge.send(topic, event);
        return event.getId();
    }
}
