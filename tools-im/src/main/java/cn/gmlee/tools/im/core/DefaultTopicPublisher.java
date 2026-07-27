package cn.gmlee.tools.im.core;

import cn.gmlee.tools.im.event.TopicMessage;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Topic 发布者默认实现
 *
 * @author SseIm Framework
 * @since 1.0.0
 */
@Slf4j
public class DefaultTopicPublisher implements TopicPublisher {

    private final TopicRouter router;
    private final String instanceId;
    private final AtomicLong publishCount = new AtomicLong(0);

    /**
     * 多实例发布回调（用于 RabbitMQ 集成）
     * -- SETTER --
     *  设置多实例广播回调

     */
    @Setter
    private MessageBroadcastCallback broadcastCallback;

    public DefaultTopicPublisher(TopicRouter router, String instanceId) {
        this.router = router;
        this.instanceId = instanceId;
    }

    @Override
    public void publish(String topic, Object message) {
        TopicMessage<Object> topicMessage = TopicMessage.of(topic, message);
        topicMessage.setSourceInstanceId(instanceId);
        publishMessage(topicMessage);
    }

    @Override
    public void publish(List<String> topics, Object message) {
        if (topics == null || topics.isEmpty()) {
            return;
        }
        for (String topic : topics) {
            publish(topic, message);
        }
    }

    @Override
    public void publish(String topic, Object message, Map<String, Object> metadata) {
        TopicMessage<Object> topicMessage = TopicMessage.of(topic, message, metadata);
        topicMessage.setSourceInstanceId(instanceId);
        publishMessage(topicMessage);
    }

    @Override
    public void publishBatch(String topic, List<?> messages) {
        if (messages == null || messages.isEmpty()) {
            return;
        }
        for (Object message : messages) {
            publish(topic, message);
        }
    }

    @Override
    public CompletableFuture<Void> publishAsync(String topic, Object message) {
        return CompletableFuture.runAsync(() -> publish(topic, message));
    }

    @Override
    public boolean hasSubscribers(String topic) {
        return !router.getSubscribers(topic).isEmpty();
    }

    @Override
    public int getSubscriberCount(String topic) {
        return router.getSubscribers(topic).size();
    }

    @Override
    public void publishMessage(TopicMessage<?> message) {
        if (message == null || message.getTopic() == null) {
            log.warn("Cannot publish null message or message with null topic");
            return;
        }

        // 设置来源实例 ID
        if (message.getSourceInstanceId() == null) {
            message.setSourceInstanceId(instanceId);
        }

        long count = publishCount.incrementAndGet();
        log.debug("Publishing message #{} to topic: {}", count, message.getTopic());

        // 1. 本地路由
        router.route(message);

        // 2. 多实例广播（如果配置了）
        if (broadcastCallback != null) {
            try {
                broadcastCallback.broadcast(message);
            } catch (Exception e) {
                log.error("Failed to broadcast message to other instances", e);
            }
        }
    }

    /**
     * 接收来自其他实例的消息
     */
    public void receiveFromRemote(TopicMessage<?> message) {
        if (message == null) {
            return;
        }
        // 只路由，不再广播（防止循环）
        router.route(message);
    }

    /**
     * 获取发布计数
     */
    public long getPublishCount() {
        return publishCount.get();
    }

    /**
     * 消息广播回调接口
     */
    public interface MessageBroadcastCallback {
        void broadcast(TopicMessage<?> message);
    }
}
