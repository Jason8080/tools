package cn.gmlee.tools.im.stream;

import cn.gmlee.tools.im.core.DefaultTopicPublisher;
import cn.gmlee.tools.im.event.TopicMessage;
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
public class StreamBroadcastConsumer implements Consumer<TopicMessage<?>> {

    private final DefaultTopicPublisher publisher;
    private final StreamProperties properties;
    private final String instanceId;

    public StreamBroadcastConsumer(DefaultTopicPublisher publisher,
                                   StreamProperties properties,
                                   String instanceId) {
        this.publisher = publisher;
        this.properties = properties;
        this.instanceId = instanceId;
    }

    /**
     * 接收来自 Spring Cloud Stream 的消息（来自其他实例）
     */
    @Override
    public void accept(TopicMessage<?> message) {
        if (message == null) {
            return;
        }

        // 跳过自己发出的消息（避免重复处理）
        if (properties.isSkipSelfMessages() && instanceId.equals(message.getSourceInstanceId())) {
            log.trace("[SseIm-Stream] Skipping self message, topic: {}", message.getTopic());
            return;
        }

        log.debug("[SseIm-Stream] Received from remote instance, topic: {}, source: {}",
                message.getTopic(), message.getSourceInstanceId());

        // 路由到本地订阅者
        publisher.receiveFromRemote(message);
    }
}
