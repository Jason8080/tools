package cn.gmlee.tools.webapp.controller;

import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestHeader;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 通用基础控制器
 *
 * @author Jas°
 * @date 2020 /8/28 (周五)
 */
public class ParameterController extends HttpController {
    /**
     * 应用编号
     */
    protected String appId;

    @ModelAttribute
    public void parameterPre(@RequestHeader(required = false) String appId) {
        this.appId = appId;
    }

    @ModelAttribute
    public void parameterPre(@RequestHeader(required = false) String appId, HttpServletRequest request, HttpServletResponse response) {
        this.appId = appId;
        super.httpPre(request, response);
    }
}
