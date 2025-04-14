package cn.gmlee.tools.ai.conf;

import cn.gmlee.tools.ai.server.impl.DashScopeServer;
import cn.gmlee.tools.ai.server.impl.GenerateServer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(AliAiProperties.class)
public class AliAiAutoConfiguration {
    @Bean
    public GenerateServer generateServer(AliAiProperties aliAiProperties) {
        return new GenerateServer(aliAiProperties);
    }
    @Bean
    public DashScopeServer dashScopeServer(AliAiProperties aliAiProperties) {
        return new DashScopeServer(aliAiProperties);
    }
}
