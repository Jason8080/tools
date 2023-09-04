package cn.gmlee.tools.gray.conf;

import cn.gmlee.tools.gray.feign.GrayFeignRequestInterceptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * 灰度客户端自动装配.
 */
@EnableConfigurationProperties(GrayProperties.class)
@ConditionalOnClass(name = {"feign.RequestInterceptor"})
@ConditionalOnMissingClass({"org.springframework.cloud.gateway.filter.GlobalFilter"})
public class GrayFeignAutoConfiguration {
    /**
     * Gray reactor service instance load balancer gray reactor service instance load balancer.
     *
     * @param properties the properties
     * @return the gray reactor service instance load balancer
     */
    @Bean
    public GrayFeignRequestInterceptor grayRequestInterceptor(GrayProperties properties) {
        return new GrayFeignRequestInterceptor(properties);
    }
}
