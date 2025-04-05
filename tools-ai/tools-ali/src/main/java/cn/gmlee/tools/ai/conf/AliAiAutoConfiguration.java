package cn.gmlee.tools.ai.conf;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(AliAiProperties.class)
public class AliAiAutoConfiguration {

}
