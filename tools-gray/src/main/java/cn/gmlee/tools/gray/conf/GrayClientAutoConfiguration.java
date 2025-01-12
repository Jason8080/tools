package cn.gmlee.tools.gray.conf;

import cn.gmlee.tools.base.util.BoolUtil;
import cn.gmlee.tools.base.util.ExceptionUtil;
import cn.gmlee.tools.gray.assist.VersionAssist;
import cn.gmlee.tools.gray.balancer.GrayReactorServiceInstanceLoadBalancer;
import cn.gmlee.tools.gray.server.GrayServer;
import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClients;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.context.annotation.Bean;

import java.util.HashMap;
import java.util.Map;

/**
 * 灰度客户端自动装配.
 */
@Slf4j
@EnableConfigurationProperties(GrayProperties.class)
@LoadBalancerClients(defaultConfiguration = GrayClientAutoConfiguration.class)
@ConditionalOnMissingClass({"org.springframework.cloud.gateway.filter.GlobalFilter"})
@ConditionalOnClass(name = {"com.alibaba.cloud.nacos.discovery.NacosDiscoveryClientConfiguration"})
@AutoConfigureBefore(name = {"com.alibaba.cloud.nacos.discovery.NacosDiscoveryClientConfiguration"})
public class GrayClientAutoConfiguration {

    @Getter
    private final String serviceId;

    /**
     * Instantiates a new Gray node auto configuration.
     *
     * @param discoveryProperties the discovery properties
     * @param grayProperties      the gray properties
     */
    public GrayClientAutoConfiguration(DiscoveryClient discoveryClient, NacosDiscoveryProperties discoveryProperties, GrayProperties grayProperties) {
        // 获取服务
        serviceId = discoveryProperties.getService();
        // 获取原生
        Map<String, String> metadata = discoveryProperties.getMetadata();
        if (metadata == null) {
            metadata = new HashMap<>();
            discoveryProperties.setMetadata(metadata);
        }
        // 设置版本
        if (BoolUtil.notEmpty(grayProperties.getVersion())) {
            log.warn("服务[{}]指定版本: {}", serviceId, grayProperties.getVersion());
            metadata.put(grayProperties.getHead(), grayProperties.getVersion());
            return;
        }
        metadata.put(grayProperties.getHead(), ExceptionUtil.sandbox(() -> VersionAssist.getNewestVersion(discoveryClient, grayProperties, serviceId), e -> "0"));
        grayProperties.setVersion(metadata.get(grayProperties.getHead()));
    }

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
