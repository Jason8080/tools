package cn.gmlee.tools.im.sse;

import cn.gmlee.tools.im.core.TopicRouter;
import cn.gmlee.tools.im.event.TopicMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.Map;

/**
 * 动态 SSE Controller
 * <p>
 * 自动为所有 Topic 提供 SSE 订阅端点
 * </p>
 *
 * <pre>
 * // 客户端订阅示例：
 * // GET /sse/stock.AAPL          - 订阅单个 Topic
 * // GET /sse/events?pattern=user.*  - 通配符订阅
 * </pre>
 *
 * @author SseIm Framework
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("${sse-im.sse.base-path:/sse}")
public class SseController {

    private final SseConnectionManager connectionManager;
    private final SseProperties properties;
    private final TopicRouter router;

    public SseController(SseConnectionManager connectionManager,
                         SseProperties properties,
                         TopicRouter router) {
        this.connectionManager = connectionManager;
        this.properties = properties;
        this.router = router;
    }

    /**
     * 订阅指定 Topic
     *
     * @param topic Topic 名称
     * @return SSE 事件流
     */
    @GetMapping(value = "/{topic}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<TopicMessage<?>>> subscribe(
            @PathVariable String topic,
            @RequestParam(value = "lastEventId", required = false) String lastEventId) {

        log.debug("Client subscribing to topic: {}", topic);

        // 主事件流
        Flux<ServerSentEvent<TopicMessage<?>>> mainStream = connectionManager.subscribe(topic)
                .map(this::toServerSentEvent);

        // 心跳流
        Flux<ServerSentEvent<TopicMessage<?>>> heartbeat = Flux.interval(properties.getHeartbeatInterval())
                .map(i -> ServerSentEvent.<TopicMessage<?>>builder()
                        .comment("heartbeat")
                        .build());

        // 合并主事件流和心跳流
        return Flux.merge(mainStream, heartbeat)
                .doOnSubscribe(sub -> log.debug("SSE stream started for topic: {}", topic))
                .doOnCancel(() -> log.debug("SSE stream cancelled for topic: {}", topic));
    }

    /**
     * 通配符订阅
     *
     * @param pattern Topic 模式（支持 * 和 #）
     * @return SSE 事件流
     */
    @GetMapping(value = "/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<TopicMessage<?>>> subscribeByPattern(
            @RequestParam String pattern,
            @RequestParam(value = "lastEventId", required = false) String lastEventId) {

        log.debug("Client subscribing to pattern: {}", pattern);

        // 创建动态订阅者
        Flux<ServerSentEvent<TopicMessage<?>>> mainStream = connectionManager.subscribe(pattern)
                .filter(msg -> router.matches(msg.getTopic(), pattern))
                .map(this::toServerSentEvent);

        // 心跳流
        Flux<ServerSentEvent<TopicMessage<?>>> heartbeat = Flux.interval(properties.getHeartbeatInterval())
                .map(i -> ServerSentEvent.<TopicMessage<?>>builder()
                        .comment("heartbeat")
                        .build());

        return Flux.merge(mainStream, heartbeat);
    }

    /**
     * 获取 Topic 连接信息
     */
    @GetMapping("/info/{topic}")
    public Map<String, Object> getTopicInfo(@PathVariable String topic) {
        return Map.of(
            "topic", topic,
            "connections", connectionManager.getConnectionCount(topic),
            "hasSubscribers", connectionManager.hasConnections(topic)
        );
    }

    /**
     * 获取所有 Topic 信息
     */
    @GetMapping("/info")
    public Map<String, Object> getAllTopicsInfo() {
        return Map.of(
            "totalConnections", connectionManager.getTotalConnections(),
            "topics", connectionManager.getAllTopics()
        );
    }

    /**
     * 转换为 ServerSentEvent
     */
    private ServerSentEvent<TopicMessage<?>> toServerSentEvent(TopicMessage<?> message) {
        return ServerSentEvent.<TopicMessage<?>>builder()
                .id(String.valueOf(message.getTimestamp()))
                .event("message")
                .data(message)
                .retry(properties.getHeartbeatInterval().multipliedBy(2))
                .build();
    }
}
