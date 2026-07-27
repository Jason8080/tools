package cn.gmlee.tools.im.controller;

import cn.gmlee.tools.im.core.MsgEvent;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Flux;

/**
 * 订阅者控制器
 */
public class SubscriberController {

    /**
     * Pull flux.
     *
     * @param urlParams the url params
     * @return flux 返回结果
     */
    @GetMapping(value = "pull", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<MsgEvent>> pull(@RequestParam MultiValueMap<String, String> urlParams) {
        return null;
    }
}
