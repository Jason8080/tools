package cn.gmlee.tools.webapp.config.vc;

import cn.gmlee.tools.redis.util.RedisClient;
import cn.gmlee.tools.webapp.filter.VcFilter;
import cn.gmlee.tools.webapp.service.VcService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;

import javax.annotation.Resource;

/**
 * 跨域问题
 *
 * @author Jas°
 */
@ConditionalOnBean(RedisClient.class)
public class VcAutoConfiguration {

    @Resource
    private RedisClient rc;

    @Bean
    public VcService vcService(VcProperties vcProperties) {
        return new VcService(rc, vcProperties);
    }

    @Bean("FilterRegistrationBean-VcFilter")
    public FilterRegistrationBean<VcFilter> filterRegistrationBean(VcService vcService) {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(new VcFilter(vcService));
        registration.addUrlPatterns(new String[]{"/*"});
        registration.setName("vcFilter");
        return registration;
    }
}
