package cn.gmlee.tools.webapp.config.cors;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import javax.annotation.Resource;

/**
 * 跨域问题
 *
 * @author Jas°
 */
@ConditionalOnMissingBean(CorsFilter.class)
public class CorsAutoConfiguration {

    @Resource
    private CorsProperties corsProperties;

    private CorsConfiguration buildConfig() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowedOrigins(corsProperties.getOrigins());
        corsConfiguration.setAllowedHeaders(corsProperties.getHeaders());
        corsConfiguration.setAllowedMethods(corsProperties.getMethods());
        corsConfiguration.setExposedHeaders(corsProperties.getExposedHeaders());
        corsConfiguration.setAllowCredentials(corsProperties.getCredentials());
        corsConfiguration.setMaxAge(corsProperties.getMaxAge());
        return corsConfiguration;
    }

    /**
     * Cors filter cors filter.
     *
     * @return the cors filter
     */
    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration(corsProperties.getPath(), buildConfig());
        return new CorsFilter(source);
    }



    @Bean("registration-CorsFilter")
    public FilterRegistrationBean<CorsFilter> filterRegistrationBean(CorsFilter filter) {
        FilterRegistrationBean register = new FilterRegistrationBean();
        register.setFilter(filter);
        register.addUrlPatterns(new String[]{"/*"});
        register.setOrder(1);
        return register;
    }


}
