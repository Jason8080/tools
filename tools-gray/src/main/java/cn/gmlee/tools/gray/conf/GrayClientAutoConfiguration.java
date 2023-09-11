package cn.gmlee.tools.gray.conf;

import cn.gmlee.tools.gray.server.GrayServer;
import cn.gmlee.tools.gray.supplier.GrayServiceInstanceListSupplier;
import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClients;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

import java.util.HashMap;
import java.util.Map;

/**
 * 灰度客户端自动装配.
 */
@EnableConfigurationProperties(GrayProperties.class)
@LoadBalancerClients(defaultConfiguration = GrayClientAutoConfiguration.class)
@ConditionalOnMissingClass({"org.springframework.cloud.gateway.filter.GlobalFilter"})
@ConditionalOnClass(name = {"com.alibaba.cloud.nacos.discovery.NacosDiscoveryClientConfiguration"})
@AutoConfigureBefore(name = {"com.alibaba.cloud.nacos.discovery.NacosDiscoveryClientConfiguration"})
public class GrayClientAutoConfiguration {

    private final String serviceId;

    /**
     * Instantiates a new Gray node auto configuration.
     *
     * @param discoveryProperties the discovery properties
     * @param grayProperties      the gray properties
     */
    public GrayClientAutoConfiguration(NacosDiscoveryProperties discoveryProperties, GrayProperties grayProperties) {
        // 获取服务
        serviceId = discoveryProperties.getService();
        // 获取原生
        Map<String, String> metadata = discoveryProperties.getMetadata();
        if (metadata == null) {
            metadata = new HashMap<>();
            discoveryProperties.setMetadata(metadata);
        }
        metadata.put(grayProperties.getHead(), grayProperties.getVersion());
    }


    @Bean
    @ConditionalOnClass(name = "org.springframework.web.servlet.DispatcherServlet")
    public GrayServiceInstanceListSupplier grayServiceInstanceListSupplier(ConfigurableApplicationContext context
            , GrayServer grayServer) {
        ServiceInstanceListSupplier delegate = ServiceInstanceListSupplier.builder()
                .withBlockingDiscoveryClient()
                .withCaching()
                .build(context);
        return new GrayServiceInstanceListSupplier(delegate, grayServer);
    }

    @Bean
    @ConditionalOnClass(name = "org.springframework.web.reactive.DispatcherHandler")
    public GrayServiceInstanceListSupplier grayServiceInstanceListSupplierByReactive(ConfigurableApplicationContext context
            , GrayServer grayServer) {
        ServiceInstanceListSupplier delegate = ServiceInstanceListSupplier.builder()
                .withDiscoveryClient()
                .withCaching()
                .build(context);
        return new GrayServiceInstanceListSupplier(delegate, grayServer);
    }
}
