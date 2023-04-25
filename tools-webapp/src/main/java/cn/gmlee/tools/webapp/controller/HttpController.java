package cn.gmlee.tools.webapp.controller;

import cn.gmlee.tools.base.util.WebUtil;
import org.springframework.web.bind.annotation.ModelAttribute;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 通用基础控制器
 *
 * @author Jas°
 * @date 2020 /8/28 (周五)
 */
public class HttpController extends BaseController {
    protected HttpServletRequest request;

    protected HttpServletResponse response;

    protected String ip;

    /**
     * 保存请求对象和响应对象.
     */
    @ModelAttribute
    public void httpPre(HttpServletRequest request, HttpServletResponse response) {
        this.request = request;
        this.response = response;
        this.ip = WebUtil.getIp(request);
    }
}
