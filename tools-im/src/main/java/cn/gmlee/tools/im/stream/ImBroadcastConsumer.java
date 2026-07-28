package cn.gmlee.tools.im.stream;

import cn.gmlee.tools.im.conf.StreamProperties;
import cn.gmlee.tools.im.core.Msg;
import cn.gmlee.tools.im.core.TopicMessage;
import cn.gmlee.tools.im.sse.SseConnectionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Consumer;

/**
 * Spring Cloud Stream 消息消费者
 */
@Slf4j
@RequiredArgsConstructor
public class ImBroadcastConsumer implements Consumer<TopicMessage<Msg>> {

    private final SseConnectionManager sseConnectionManager;

    private final StreamProperties properties;

    /**
     * 接收来自 Spring Cloud Stream 的消息
     */
    @Override
    public void accept(TopicMessage<Msg> message) {
        if (message == null) {
            return;
        }
        sseConnectionManager.publish(message);
    }
}
