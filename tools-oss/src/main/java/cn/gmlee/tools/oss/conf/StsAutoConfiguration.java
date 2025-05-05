package cn.gmlee.tools.oss.conf;

import cn.gmlee.tools.base.util.ProxyUtil;
import cn.gmlee.tools.oss.STS;
import cn.gmlee.tools.oss.STSClient;
import com.alibaba.alicloud.context.oss.OssContextAutoConfiguration;
import com.alibaba.alicloud.context.oss.OssProperties;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.OSSClientBuilder;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.lang.reflect.Method;

@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(name = {"com.alibaba.alicloud.oss.OssAutoConfiguration"})
@ConditionalOnProperty(name = {"spring.cloud.alicloud.oss.enabled"}, matchIfMissing = true)
@EnableConfigurationProperties({OssProperties.class, StsProperties.class})
@AutoConfigureBefore(OssContextAutoConfiguration.class)
public class StsAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(value = "spring.cloud.alicloud.oss.authorization-mode", havingValue = "STS")
    public STS ossClient(OssProperties ossProperties, StsProperties stsProperties) {
        STS sts = new STSClient(build(ossProperties), stsProperties);
        return ProxyUtil.JdkProxy(STSClient.class, (Object proxy, Method method, Object[] as) ->
                method.invoke(sts.refresh(), as), Serializable.class
        );
    }

    private static OSSClient build(OssProperties ossProperties) {
        Assert.isTrue(!StringUtils.isEmpty(ossProperties.getEndpoint()), "Oss endpoint can't be empty.");
        Assert.isTrue(!StringUtils.isEmpty(ossProperties.getSts().getAccessKey()), "Access key can't be empty.");
        Assert.isTrue(!StringUtils.isEmpty(ossProperties.getSts().getSecretKey()), "Secret key can't be empty.");
        return (OSSClient) (new OSSClientBuilder()).build(
                ossProperties.getEndpoint(),
                ossProperties.getSts().getAccessKey(),
                ossProperties.getSts().getSecretKey(),
                ossProperties.getSts().getSecurityToken(),
                ossProperties.getConfig()
        );
    }
}
