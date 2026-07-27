package cn.gmlee.tools.im.controller;

import cn.gmlee.tools.base.mod.R;
import cn.gmlee.tools.im.core.Ctl;
import cn.gmlee.tools.im.core.Msg;
import cn.gmlee.tools.im.serve.TopicRouterServe;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Flux;

import java.io.Serializable;

/**
 * 订阅者控制器
 */
@RequiredArgsConstructor
public class SubscriberController implements Ctl<Msg> {

    private final TopicRouterServe<Serializable, Msg> topicRouteServe;

    /**
     * 拉取.
     *
     * @param urlParams the url params
     * @return flux 返回结果
     */
    @GetMapping(value = "pull/{topic}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<R<Msg>> pull(@PathVariable String topic, @RequestParam MultiValueMap<String, String> urlParams) {
        return topicRouteServe.pull(topic, urlParams).map(R::of);
    }
}
