package cn.gmlee.tools.im.controller;

import cn.gmlee.tools.base.mod.R;
import cn.gmlee.tools.im.core.Ctl;
import cn.gmlee.tools.im.core.Msg;
import cn.gmlee.tools.im.serve.BroadcasterServe;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 发布者控制器
 */
@RequiredArgsConstructor
public class PublisherController implements Ctl<Msg> {

    private final BroadcasterServe broadcasterServe;

    /**
     * 推送.
     *
     * @param urlParams 地址参数集
     * @return r 返回结果
     */
    @Override
    @PostMapping(value = "push/{topic}", produces = MediaType.APPLICATION_JSON_VALUE)
    public R<?> push(@PathVariable String topic, @RequestParam MultiValueMap<String, String> urlParams, @RequestBody Msg msg) {
        broadcasterServe.send(topic, msg);
        return R.OK;
    }
}
