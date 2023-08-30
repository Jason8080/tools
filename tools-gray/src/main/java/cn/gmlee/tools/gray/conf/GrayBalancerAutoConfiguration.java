package cn.gmlee.tools.gray.conf;

import cn.gmlee.tools.gray.filter.GrayBalancerFilter;
import cn.gmlee.tools.gray.server.GrayServer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.context.annotation.Bean;

/**
 * 灰度负载自动装配
 */
@EnableConfigurationProperties(GrayProperties.class)
@ConditionalOnClass(name = {"org.springframework.cloud.gateway.filter.GlobalFilter"})
public class GrayBalancerAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean({GrayServer.class})
    public GrayServer grayBalancerFilter(GrayProperties properties) {
        return new GrayServer(properties);
    }

    /**
     * Gray balancer filter gray balancer filter.
     *
     * @param clientFactory the client factory
     * @param properties    the properties
     * @return the gray load balancer client filter
     */
    @Bean
    @ConditionalOnMissingBean({GrayBalancerFilter.class})
    public GrayBalancerFilter grayBalancerFilter(LoadBalancerClientFactory clientFactory, GrayServer grayServer, GrayProperties properties) {
        return new GrayBalancerFilter(clientFactory, grayServer, properties);
    }
}