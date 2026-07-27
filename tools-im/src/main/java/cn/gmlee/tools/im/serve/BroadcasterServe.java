package cn.gmlee.tools.im.serve;

import cn.gmlee.tools.im.core.Msg;
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
     * @param msg   the msg
     */
    public void send(String topic, Msg msg) {
        streamBridge.send(topic, msg.build());
    }
}
