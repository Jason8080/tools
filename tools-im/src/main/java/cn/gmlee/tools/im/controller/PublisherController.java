package cn.gmlee.tools.im.controller;

import cn.gmlee.tools.base.mod.R;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * 发布者控制器
 */
public class PublisherController {

    /**
     * Push r.
     *
     * @param params the params
     * @return the r
     */
    @PostMapping(value = "push", produces = MediaType.APPLICATION_JSON_VALUE)
    public R<?> push(@RequestParam Map<String, String> params) {
        return R.OK.newly("success");
    }
}
