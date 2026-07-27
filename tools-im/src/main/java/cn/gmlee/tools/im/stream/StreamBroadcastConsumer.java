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
 * <p>
 * 作为 Spring Cloud Stream 的函数式消费者 Bean，
 * 接收来自其他实例的广播消息并路由到本地订阅者。
 * </p>
 * <p>
 * Bean 名称必须为 {@code sseImBroadcastConsumer}，
 * 对应 Spring Cloud Stream 绑定名 {@code sseImBroadcastConsumer-in-0}。
 * </p>
 *
 * @author SseIm Framework
 * @since 1.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class StreamBroadcastConsumer implements Consumer<TopicMessage<Msg>> {

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
