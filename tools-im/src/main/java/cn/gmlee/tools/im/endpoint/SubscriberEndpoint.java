package cn.gmlee.tools.im.endpoint;

import cn.gmlee.tools.base.mod.R;
import cn.gmlee.tools.im.core.Endpoint;
import cn.gmlee.tools.im.core.Msg;
import cn.gmlee.tools.im.core.TopicRouter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.io.Serializable;

/**
 * 订阅者端点.
 * <p>
 * 处理 SSE 订阅请求。
 * </p>
 */
@RequiredArgsConstructor
@RequestMapping("${im.base-path:/}")
public class SubscriberEndpoint implements Endpoint<Msg> {

    private final TopicRouter<Serializable, Msg> topicRouteServe;

    /**
     * 拉取消息.
     *
     * @param topic     Topic 名称
     * @param urlParams URL 参数
     * @return SSE 事件流
     */
    @Override
    @GetMapping(value = "pull/{topic}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public @ResponseBody Flux<R<Msg>> pull(
            @PathVariable String topic,
            @RequestParam MultiValueMap<String, String> urlParams) {
        return topicRouteServe.pull(topic, urlParams).map(R::of);
    }
}
