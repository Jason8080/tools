package cn.gmlee.tools.gray.conf;

import cn.gmlee.tools.base.util.BoolUtil;
import cn.gmlee.tools.base.util.NullUtil;
import cn.gmlee.tools.gray.balancer.GrayReactorServiceInstanceLoadBalancer;
import cn.gmlee.tools.gray.server.GrayServer;
import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import lombok.Getter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClients;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.context.annotation.Bean;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 灰度客户端自动装配.
 */
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
            metadata.put(grayProperties.getHead(), grayProperties.getVersion());
            return;
        }
        metadata.put(grayProperties.getHead(), getNewestVersion(discoveryClient, grayProperties));
    }

    private String getNewestVersion(DiscoveryClient discoveryClient, GrayProperties grayProperties) {
        List<ServiceInstance> instances = discoveryClient.getInstances(serviceId);
        if (BoolUtil.isEmpty(instances)) {
            return "0";
        }
        Map<String, List<ServiceInstance>> instanceMap = instances.stream().collect(Collectors.groupingBy(x -> x.getMetadata().get(grayProperties.getHead())));
        Stream<String> stream = instanceMap.keySet().stream().filter(BoolUtil::isDigit);
        if(stream.count() < 1){
            return "0";
        }
        List<Long> vs = stream.distinct().map(Long::getLong).sorted().collect(Collectors.toList());
        if (vs.isEmpty()) {
            vs.add(0L);
        }
        int index = vs.size() - 1;
        Long v = NullUtil.get(vs.get(index), 0L);
        if (v >= Long.MAX_VALUE) {
            return "0";
        }
        if (index == 0) {
            return String.valueOf(v + 1);
        }
        // 加入到最后1个版本集群
        return v.toString();
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
