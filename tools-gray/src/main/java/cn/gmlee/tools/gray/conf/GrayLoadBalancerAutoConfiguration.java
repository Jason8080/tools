package cn.gmlee.tools.gray.conf;

import cn.gmlee.tools.gray.balancer.GrayReactorServiceInstanceLoadBalancer;
import cn.gmlee.tools.gray.server.GrayServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClients;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.context.annotation.Bean;

/**
 * 灰度客户端自动装配.
 */
@Slf4j
@EnableConfigurationProperties(GrayProperties.class)
@LoadBalancerClients(defaultConfiguration = GrayLoadBalancerAutoConfiguration.class)
@ConditionalOnMissingClass({"org.springframework.cloud.gateway.filter.GlobalFilter"})
@ConditionalOnClass(name = {"com.alibaba.cloud.nacos.discovery.NacosDiscoveryClientConfiguration"})
@AutoConfigureBefore(name = {"com.alibaba.cloud.nacos.discovery.NacosDiscoveryClientConfiguration"})
public class GrayLoadBalancerAutoConfiguration {
    /**
     * Gray reactor service instance load balancer gray reactor service instance load balancer.
     *
     * @param clientFactory the client factory
     * @param grayServer    the gray server
     * @return the gray reactor service instance load balancer
     */
    @Bean
    @ConditionalOnMissingBean({GrayReactorServiceInstanceLoadBalancer.class})
    public GrayReactorServiceInstanceLoadBalancer grayReactorServiceInstanceLoadBalancer(LoadBalancerClientFactory clientFactory, GrayServer grayServer) {
        return new GrayReactorServiceInstanceLoadBalancer(clientFactory, grayServer);
    }
}
