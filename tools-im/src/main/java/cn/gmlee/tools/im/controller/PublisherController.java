package cn.gmlee.tools.im.controller;

import cn.gmlee.tools.base.mod.R;
import cn.gmlee.tools.im.core.Ctl;
import cn.gmlee.tools.im.core.Msg;
import cn.gmlee.tools.im.core.TopicRouter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import java.io.Serializable;

/**
 * 发布者控制器
 */
@RequiredArgsConstructor
@RequestMapping("${im.base-path:/}")
public class PublisherController implements Ctl<Msg> {

    private final TopicRouter<Serializable, Msg> topicRouteServe;

    /**
     * 推送.
     *
     * @param urlParams 地址参数集
     * @return r 返回结果
     */
    @Override
    @PostMapping(value = "push/{topic}", produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody R<Serializable> push(@PathVariable String topic, @RequestParam MultiValueMap<String, String> urlParams, @RequestBody Msg msg) {
        return R.of(topicRouteServe.push(topic, urlParams, msg));
    }
}
