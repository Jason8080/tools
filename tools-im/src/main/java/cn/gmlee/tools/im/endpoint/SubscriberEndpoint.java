package cn.gmlee.tools.im.endpoint;

import cn.gmlee.tools.im.conf.SseProperties;
import cn.gmlee.tools.im.core.Endpoint;
import cn.gmlee.tools.im.core.Msg;
import cn.gmlee.tools.im.core.TopicRouter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
 * 处理 SSE 订阅请求，数据流统一走 {@link TopicRouter} 路由。
 * </p>
 * <p>
 * 连接生命周期由以下机制保证：
 * <ul>
 *   <li>doFinally：响应式清理，处理正常断开（~95%）</li>
 *   <li>ConnectionReaper：定时扫描，处理残留连接（~5%）</li>
 *   <li>TCP keepalive：OS 层检测半开连接</li>
 * </ul>
 * </p>
 */
@Slf4j
@RequiredArgsConstructor
@RequestMapping("${im.base-path:/}")
public class SubscriberEndpoint implements Endpoint<Msg> {

    private final TopicRouter<Serializable, Msg> topicRouteServe;
    private final SseProperties sseProperties;

    /**
     * SSE 拉取.
     *
     * @param topic     Topic 名称
     * @param urlParams URL 参数
     * @return SSE 事件流
     */
    @GetMapping(value = "pull/{topic}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public @ResponseBody Flux<ServerSentEvent<Msg>> sse(
            @PathVariable String topic,
            @RequestParam MultiValueMap<String, String> urlParams) {

        Flux<ServerSentEvent<Msg>> dataFlux = topicRouteServe.pull(topic, urlParams)
                .map(msg -> ServerSentEvent.<Msg>builder()
                        .data(msg)
                        .build());

        // 如果启用心跳，合并心跳注释流
        if (sseProperties.getHeartbeat().isEnabled()) {
            // 使用 share() 使 dataFlux 可被多次订阅
            Flux<ServerSentEvent<Msg>> sharedDataFlux = dataFlux.share();
            Flux<ServerSentEvent<Msg>> heartbeatFlux = createHeartbeatFlux();

            // 关键：当 dataFlux 完成时，心跳也必须停止
            // takeUntilOther 在 dataFlux 完成时终止心跳流
            // 这样 Flux.merge 才能在连接关闭时正确完成
            return Flux.merge(
                    sharedDataFlux,
                    heartbeatFlux.takeUntilOther(sharedDataFlux.ignoreElements())
            );
        }

        return dataFlux;
    }

    /**
     * 创建心跳事件流.
     * <p>
     * 定期发送 SSE 注释（comment），保持连接活性。
     * SSE 注释以 {@code :} 开头，浏览器的 EventSource API 会忽略注释，
     * 不会触发 onmessage 回调，但能防止反向代理因空闲超时而断开连接。
     * </p>
     *
     * @return 心跳事件流
     */
    private Flux<ServerSentEvent<Msg>> createHeartbeatFlux() {
        SseProperties.HeartbeatConfig config = sseProperties.getHeartbeat();
        Duration interval = config.getInterval();
        String comment = config.getComment();

        return Flux.interval(interval)
                .map(tick -> ServerSentEvent.<Msg>builder()
                        .comment(comment)
                        .build());
    }
}
