package cn.gmlee.tools.gray.conf;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.cloud.nacos.discovery.NacosDiscoveryClientConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * The type Gray node auto configuration.
 */
@Configuration
@AutoConfigureBefore({NacosDiscoveryClientConfiguration.class})
public class GrayNodeAutoConfiguration {

    /**
     * Instantiates a new Gray node auto configuration.
     *
     * @param discoveryProperties the discovery properties
     * @param grayProperties      the gray properties
     */
    public GrayNodeAutoConfiguration(NacosDiscoveryProperties discoveryProperties, GrayProperties grayProperties) {
        // 获取原生
        Map<String, String> metadata = discoveryProperties.getMetadata();
        if (metadata == null) {
            metadata = new HashMap<>();
            discoveryProperties.setMetadata(metadata);
        }
        // 开关检测
        if (Boolean.TRUE.toString().equalsIgnoreCase(grayProperties.getEnable())) {
            metadata.put(grayProperties.getHead(), grayProperties.getVersion());
        }
    }
}
