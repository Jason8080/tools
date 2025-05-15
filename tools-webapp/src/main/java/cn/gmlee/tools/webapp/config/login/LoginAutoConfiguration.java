package cn.gmlee.tools.webapp.config.login;

import cn.gmlee.tools.webapp.filter.AuthFilter;
import cn.gmlee.tools.webapp.service.LoginServer;
import cn.gmlee.tools.webapp.service.LoginService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;

/**
 * 过滤器配置
 *
 * @author Jas °
 * @date 2020 /8/28 (周五)
 */
@EnableConfigurationProperties(LoginProperties.class)
public class LoginAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(LoginServer.class)
    @ConditionalOnBean(type = "cn.gmlee.tools.redis.util.RedisClient")
    public LoginServer loginServer(){
        return new LoginService();
    }

    @ConditionalOnBean(LoginServer.class)
    @Bean("FilterRegistrationBean-AuthFilter")
    public FilterRegistrationBean<AuthFilter> authFilter(LoginServer loginServer, LoginProperties lp) {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(new AuthFilter(loginServer, lp.getUrlExcludes().toArray(new String[0])));
        registration.addUrlPatterns(lp.getUrlPatterns().toArray(new String[0]));
        registration.setName("authFilter");
        return registration;
    }
}
