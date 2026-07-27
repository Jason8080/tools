package cn.gmlee.tools.im.controller;

import cn.gmlee.tools.base.mod.R;
import org.springframework.http.MediaType;
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
     * @param message 消息内容
     * @return flux 返回结果
     */
    @GetMapping(value = "pull", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<?> pull(@RequestParam String message) {
        return Flux.just(R.OK.newly("success"));
    }
}
