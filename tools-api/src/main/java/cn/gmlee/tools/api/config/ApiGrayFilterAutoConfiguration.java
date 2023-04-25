package cn.gmlee.tools.api.config;


import cn.gmlee.tools.api.gray.MicroServiceApiCoexistGrayFilter;
import cn.gmlee.tools.api.gray.GrayFilter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;

import javax.annotation.Resource;

/**
 * Api灰度选择过滤器
 *
 * @author Jas°
 */
@ConditionalOnMissingBean(GrayFilter.class)
public class ApiGrayFilterAutoConfiguration {

    @Resource
    ApiGrayRegistrationCenterProperties apiGrayRegistrationCenterProperties;

    @Bean
    public MicroServiceApiCoexistGrayFilter microServiceApiCoexistGrayFilter() {
        return new MicroServiceApiCoexistGrayFilter(apiGrayRegistrationCenterProperties);
    }

    @Bean("FilterRegistrationBean-MicroServiceApiCoexistGrayFilter")
    public FilterRegistrationBean filterRegistrationBean(MicroServiceApiCoexistGrayFilter filter) {
        FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean();
        filterRegistrationBean.setFilter(filter);
        filterRegistrationBean.addUrlPatterns("/*");
        filterRegistrationBean.setOrder(1);
        return filterRegistrationBean;
    }
}
