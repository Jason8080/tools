package cn.gmlee.tools.ai.conf;

import cn.gmlee.tools.ai.server.impl.DashScopeServer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * The type Ali ai auto configuration.
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(AliAiProperties.class)
public class AliAiAutoConfiguration {

    /**
     * Dash scope server dash scope server.
     *
     * @param aliAiProperties the ali ai properties
     * @return the dash scope server
     */
    @Bean
    public DashScopeServer dashScopeServer(AliAiProperties aliAiProperties) {
        return new DashScopeServer(aliAiProperties);
    }
}
