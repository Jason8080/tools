package cn.gmlee.tools.profile.conf;

import cn.gmlee.tools.base.util.BoolUtil;
import cn.gmlee.tools.profile.helper.ProfileHelper;
import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;

import java.util.Map;

/**
 * 环境节点自动装配.
 */
@ConditionalOnClass(name = {"com.alibaba.cloud.nacos.discovery.NacosDiscoveryClientConfiguration"})
@AutoConfigureAfter(name = {"com.alibaba.cloud.nacos.discovery.NacosDiscoveryClientConfiguration"})
public class ProfileNodeAutoConfiguration {

    /**
     * Instantiates a new Gray node auto configuration.
     *
     * @param discoveryProperties the discovery properties
     * @param profileProperties      the gray properties
     */
    public ProfileNodeAutoConfiguration(NacosDiscoveryProperties discoveryProperties, ProfileProperties profileProperties) {
        Map<String, String> metadata = discoveryProperties.getMetadata();
        if(BoolUtil.isEmpty(metadata)){
            // 关闭数据环境
            ProfileHelper.enable(false);
        }
        String version = metadata.get(profileProperties.getHead());
        if(!profileProperties.getVersions().contains(version)){
            // 关闭数据环境
            ProfileHelper.enable(false);
        }
    }
}
