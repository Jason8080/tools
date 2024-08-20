package cn.gmlee.tools.sign.conf;

import cn.gmlee.tools.sign.aspect.SignAspect;
import cn.gmlee.tools.sign.fiegn.SignRequestInterceptor;
import feign.RequestInterceptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@EnableConfigurationProperties(LdwSignProperties.class)
public class LdwSignatureAutoConfiguration {

    @Bean
    public SignAspect signAspect(LdwSignProperties properties) {
        return new SignAspect(properties);
    }

    @Bean
    @ConditionalOnClass(RequestInterceptor.class)
    public SignRequestInterceptor signRequestInterceptor(LdwSignProperties properties) {
        return new SignRequestInterceptor(properties);
    }
}
