package cn.gmlee.tools.im.sse;

import cn.gmlee.tools.im.event.TopicMessage;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * SSE 连接管理器
 * <p>
 * 管理所有 SSE 连接，提供订阅/取消订阅能力
 * </p>
 *
 * @author SseIm Framework
 * @since 1.0.0
 */
@Slf4j
public class SseConnectionManager {

    private final SseProperties properties;

    /**
     * Topic -> Sinks.Many 映射
     */
    private final ConcurrentHashMap<String, Sinks.Many<TopicMessage<?>>> topicSinks = new ConcurrentHashMap<>();

    /**
     * Topic -> 连接数映射
     */
    private final ConcurrentHashMap<String, AtomicInteger> connectionCounts = new ConcurrentHashMap<>();

    /**
     * 总连接数
     */
    private final AtomicInteger totalConnections = new AtomicInteger(0);

    public SseConnectionManager(SseProperties properties) {
        this.properties = properties;
    }

    /**
     * 订阅 Topic
     *
     * @param topic Topic 名称
     * @return SSE 事件流
     */
    public Flux<TopicMessage<?>> subscribe(String topic) {
        // 检查连接数限制
        if (totalConnections.get() >= properties.getMaxTotalConnections()) {
            log.warn("Max total connections reached: {}", properties.getMaxTotalConnections());
            return Flux.error(new RuntimeException("Max connections reached"));
        }

        AtomicInteger count = connectionCounts.computeIfAbsent(topic, k -> new AtomicInteger(0));
        if (count.get() >= properties.getMaxConnectionsPerTopic()) {
            log.warn("Max connections for topic {} reached: {}", topic, properties.getMaxConnectionsPerTopic());
            return Flux.error(new RuntimeException("Max connections for topic reached"));
        }

        // 获取或创建 Sink
        Sinks.Many<TopicMessage<?>> sink = topicSinks.computeIfAbsent(topic,
            t -> Sinks.many().multicast().onBackpressureBuffer(properties.getBufferSize()));

        // 增加连接计数
        count.incrementAndGet();
        totalConnections.incrementAndGet();

        log.debug("Client subscribed to topic: {}, connections: {}", topic, count.get());

        // 返回事件流，添加心跳和生命周期管理
        return sink.asFlux()
                .doOnCancel(() -> {
                    count.decrementAndGet();
                    totalConnections.decrementAndGet();
                    log.debug("Client unsubscribed from topic: {}, connections: {}", topic, count.get());
                    cleanupIfEmpty(topic);
                })
                .doOnTerminate(() -> {
                    count.decrementAndGet();
                    totalConnections.decrementAndGet();
                    cleanupIfEmpty(topic);
                });
    }

    /**
     * 发布消息到 Topic 的所有 SSE 客户端
     *
     * @param message Topic 消息
     */
    public void publish(TopicMessage<?> message) {
        if (message == null || message.getTopic() == null) {
            return;
        }

        Sinks.Many<TopicMessage<?>> sink = topicSinks.get(message.getTopic());
        if (sink != null) {
            Sinks.EmitResult result = sink.tryEmitNext(message);
            if (result.isFailure()) {
                log.warn("Failed to emit message to topic {}: {}", message.getTopic(), result);
            }
        }
    }

    /**
     * 获取 Topic 连接数
     */
    public int getConnectionCount(String topic) {
        AtomicInteger count = connectionCounts.get(topic);
        return count != null ? count.get() : 0;
    }

    /**
     * 获取总连接数
     */
    public int getTotalConnections() {
        return totalConnections.get();
    }

    /**
     * 获取所有 Topic
     */
    public java.util.Set<String> getAllTopics() {
        return topicSinks.keySet();
    }

    /**
     * 检查 Topic 是否有连接
     */
    public boolean hasConnections(String topic) {
        return getConnectionCount(topic) > 0;
    }

    /**
     * 清理空的 Sink
     */
    private void cleanupIfEmpty(String topic) {
        AtomicInteger count = connectionCounts.get(topic);
        if (count != null && count.get() <= 0) {
            topicSinks.remove(topic);
            connectionCounts.remove(topic);
            log.debug("Cleaned up empty sink for topic: {}", topic);
        }
    }

    /**
     * 关闭所有连接
     */
    public void shutdown() {
        topicSinks.values().forEach(Sinks.Many::tryEmitComplete);
        topicSinks.clear();
        connectionCounts.clear();
        totalConnections.set(0);
        log.info("SSE Connection Manager shutdown");
    }
}
