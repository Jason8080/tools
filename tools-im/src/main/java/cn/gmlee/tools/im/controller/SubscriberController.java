package cn.gmlee.tools.im.controller;

import cn.gmlee.tools.base.mod.R;
import cn.gmlee.tools.im.core.Ctl;
import cn.gmlee.tools.im.core.Msg;
import cn.gmlee.tools.im.core.TopicRouter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.io.Serializable;

/**
 * 订阅者控制器
 */
@RequiredArgsConstructor
@RequestMapping("${im.base-path:/}")
public class SubscriberController implements Ctl<Msg> {

    private final TopicRouter<Serializable, Msg> topicRouteServe;

    /**
     * 拉取.
     *
     * @param urlParams the url params
     * @return flux 返回结果
     */
    @GetMapping(value = "pull/{topic}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public @ResponseBody Flux<R<Msg>> pull(@PathVariable String topic, @RequestParam MultiValueMap<String, String> urlParams) {
        return topicRouteServe.pull(topic, urlParams).map(R::of);
    }
}
