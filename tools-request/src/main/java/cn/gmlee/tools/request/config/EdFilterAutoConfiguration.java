package cn.gmlee.tools.request.config;

import cn.gmlee.tools.request.filter.EdFilter;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;

import javax.annotation.Resource;

/**
 * 过滤器配置
 *
 * @author Jas°
 * @date 2020/8/28 (周五)
 */
@EnableConfigurationProperties(EdProperties.class)
@AutoConfigureAfter(ReplaceStreamFilterAutoConfiguration.class)
public class EdFilterAutoConfiguration {

    @Resource
    private EdProperties edProperties;

    @Bean("FilterRegistrationBean-EdFilter")
    @ConditionalOnBean(ReplaceStreamFilterAutoConfiguration.class)
    public FilterRegistrationBean<EdFilter> edFilter() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(new EdFilter(edProperties));
        registration.addUrlPatterns("/*");
        registration.setName("edFilter");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 1);
        return registration;
    }
}
