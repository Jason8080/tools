package cn.gmlee.tools.im.endpoint;

import cn.gmlee.tools.im.conf.SseProperties;
import cn.gmlee.tools.im.core.Endpoint;
import cn.gmlee.tools.im.core.Msg;
import cn.gmlee.tools.im.core.TopicRouter;
import cn.gmlee.tools.im.sse.SseConnectionManager;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.io.Serializable;
import java.time.Duration;

/**
 * 订阅者端点.
 * <p>
 * 处理 SSE 订阅请求，支持心跳注释。
 * </p>
 * <p>
 * 数据流统一走 {@link TopicRouter} 路由；心跳由端点层独立管理，
 * 通过 {@link SseConnectionManager#heartbeat(String, Duration)} 获取轻量心跳流，
 * 不创建实际 SSE 连接。
 * </p>
 */
@RequiredArgsConstructor
@RequestMapping("${im.base-path:/}")
public class SubscriberEndpoint implements Endpoint<Msg> {

    private final TopicRouter<Serializable, Msg> topicRouteServe;
    private final SseConnectionManager sseConnectionManager;
    private final SseProperties sseProperties;

    /**
     * SSE 拉取.
     * <p>
     * 数据通过 {@link TopicRouter} 路由获取，心跳由端点层合并。
     * 心跳以 SSE 注释形式发送（{@code : heartbeat <timestamp>}），
     * 符合 SSE 规范，客户端无需改动。
     * </p>
     *
     * @param topic     Topic 名称
     * @param urlParams URL 参数
     * @return SSE 事件流
     */
    @GetMapping(value = "pull/{topic}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public @ResponseBody Flux<ServerSentEvent<Object>> sse(
            @PathVariable String topic,
            @RequestParam MultiValueMap<String, String> urlParams) {

        // 数据流：通过 TopicRouter 路由
        Flux<ServerSentEvent<Object>> dataFlux = topicRouteServe.pull(topic, urlParams)
                .map(msg -> ServerSentEvent.builder()
                        .data(msg)
                        .build());

        // 心跳流：端点层独立管理，不创建 SSE 连接
        if (sseProperties.getHeartbeat().isEnabled()) {
            Duration interval = sseProperties.getHeartbeat().getInterval();
            Flux<ServerSentEvent<Object>> heartbeatFlux = sseConnectionManager.heartbeat(topic, interval)
                    .map(timestamp -> ServerSentEvent.builder()
                            .comment("heartbeat " + timestamp)
                            .build());

            return Flux.merge(dataFlux, heartbeatFlux);
        }

        return dataFlux;
    }
}
