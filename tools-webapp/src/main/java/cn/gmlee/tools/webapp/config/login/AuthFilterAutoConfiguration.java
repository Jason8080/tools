package cn.gmlee.tools.webapp.config.login;

import cn.gmlee.tools.webapp.filter.AuthFilter;
import cn.gmlee.tools.webapp.controller.AuthController;
import cn.gmlee.tools.webapp.service.LoginService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;

import java.util.ArrayList;
import java.util.List;

/**
 * 过滤器配置
 *
 * @author Jas°
 * @date 2020/8/28 (周五)
 */
@ConditionalOnMissingBean(AuthController.class)
public class AuthFilterAutoConfiguration {

    @Value("${tools.webapp.login.urlPatterns:/*}")
    private List<String> urlPatterns = new ArrayList();

    @Value("${tools.webapp.login.urlExcludes:}")
    private List<String> urlExcludes = new ArrayList();

    @Bean("FilterRegistrationBean-AuthFilter")
    @ConditionalOnBean(LoginService.class)
    public FilterRegistrationBean<AuthFilter> authFilter(LoginService loginService) {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(new AuthFilter(loginService, urlExcludes.toArray(new String[0])));
        registration.addUrlPatterns(urlPatterns.toArray(new String[0]));
        registration.setName("authFilter");
        return registration;
    }
}
