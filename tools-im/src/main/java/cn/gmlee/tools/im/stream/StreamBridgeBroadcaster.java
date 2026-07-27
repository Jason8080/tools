package cn.gmlee.tools.im.stream;

import cn.gmlee.tools.im.core.DefaultTopicPublisher;
import cn.gmlee.tools.im.event.TopicMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;

/**
 * Spring Cloud Stream 广播集成
 * <p>
 * 通过 {@link StreamBridge} 将消息广播到所有实例。
 * 不绑定任何具体 Broker（RabbitMQ / Kafka / 等），由 Spring Cloud Stream Binder 决定。
 * </p>
 * <p>
 * 使用方式：在 application.yml 中配置：
 * <pre>
 * sse-im:
 *   stream:
 *     enabled: true
 *     broadcast-destination: sse-im-broadcast
 *
 * spring:
 *   cloud:
 *     stream:
 *       bindings:
 *         sseImBroadcastConsumer-in-0:
 *           destination: sse-im-broadcast
 *           # 不设 group → Fanout 广播到所有实例
 *       # 如果 rabbit binder 需要调优：
 *       rabbit:
 *         bindings:
 *           sseImBroadcastConsumer-in-0:
 *             consumer:
 *               prefetch: 250
 * </pre>
 *
 * @author SseIm Framework
 * @since 1.0.0
 */
@Slf4j
public class StreamBridgeBroadcaster implements DefaultTopicPublisher.MessageBroadcastCallback {

    private final StreamBridge streamBridge;
    private final StreamProperties properties;
    private final String instanceId;

    public StreamBridgeBroadcaster(StreamBridge streamBridge,
                                   StreamProperties properties,
                                   String instanceId) {
        this.streamBridge = streamBridge;
        this.properties = properties;
        this.instanceId = instanceId;
    }

    /**
     * 通过 Spring Cloud Stream 广播消息到所有实例
     */
    @Override
    public void broadcast(TopicMessage<?> message) {
        if (!properties.isEnabled()) {
            return;
        }

        try {
            String destination = properties.getBroadcastDestination();
            boolean sent = streamBridge.send(destination, message);
            if (sent) {
                log.debug("[SseIm-Stream] Broadcast to '{}', topic: {}", destination, message.getTopic());
            } else {
                log.warn("[SseIm-Stream] Failed to broadcast, topic: {}", message.getTopic());
            }
        } catch (Exception e) {
            log.error("[SseIm-Stream] Broadcast error, topic: {}", message.getTopic(), e);
        }
    }

    public String getInstanceId() {
        return instanceId;
    }
}
