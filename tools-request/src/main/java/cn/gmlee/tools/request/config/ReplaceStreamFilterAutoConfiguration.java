package cn.gmlee.tools.request.config;

import cn.gmlee.tools.request.filter.ReplaceStreamFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;

/**
 * 过滤器配置
 *
 * @author Jas°
 * @date 2020/8/28 (周五)
 */
public class ReplaceStreamFilterAutoConfiguration {
    @Bean("FilterRegistrationBean-ReplaceStreamFilter")
    public FilterRegistrationBean<ReplaceStreamFilter> replaceStreamFilter() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(new ReplaceStreamFilter());
        registration.addUrlPatterns("/*");
        registration.setName("replaceStreamFilter");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }
}
