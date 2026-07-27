package cn.gmlee.tools.im.controller;

import cn.gmlee.tools.base.mod.R;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 发布者控制器
 */
public class PublisherController {

    /**
     * Push r.
     *
     * @param urlParams 地址参数集
     * @return r 返回结果
     */
    @PostMapping(value = "push", produces = MediaType.APPLICATION_JSON_VALUE)
    public R<?> push(@RequestParam MultiValueMap<String, String> urlParams) {
        return R.OK.newly("success");
    }
}
