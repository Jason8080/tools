package cn.gmlee.tools.im.serve;

import cn.gmlee.tools.im.core.MsgEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.stream.function.StreamBridge;

/**
 * 广播服务
 */
@RequiredArgsConstructor
public class BroadcasterServe {

    private final StreamBridge streamBridge;

    /**
     * Send.
     *
     * @param topic the topic
     * @param event the event
     */
    public void send(String topic, MsgEvent event) {
        streamBridge.send(topic, event);
    }
}
