package cn.gmlee.tools.im.endpoint;

import cn.gmlee.tools.im.core.Endpoint;
import cn.gmlee.tools.im.core.Msg;
import cn.gmlee.tools.im.core.TopicRouter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.io.Serializable;

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
@RequiredArgsConstructor
@RequestMapping("${im.base-path:/}")
public class SubscriberEndpoint implements Endpoint<Msg> {

    private final TopicRouter<Serializable, Msg> topicRouteServe;

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
        return topicRouteServe.pull(topic, urlParams)
                .map(msg -> ServerSentEvent.<Msg>builder()
                        .data(msg)
                        .build());
    }
}
