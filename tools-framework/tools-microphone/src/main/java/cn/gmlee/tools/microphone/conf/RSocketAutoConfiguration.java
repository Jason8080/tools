package cn.gmlee.tools.microphone.conf;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.util.MimeType;

import java.util.Map;

/**
 * RSocket自动装配.
 */
@Configuration(proxyBeanMethods = false)
@PropertySource(value = {"classpath:microphone.properties", "classpath:microphone.properties"}, ignoreResourceNotFound = true)
public class RSocketAutoConfiguration {
    @Bean
    public RSocketStrategies rsocketStrategies() {
        return RSocketStrategies.builder()
                .metadataExtractorRegistry(registry -> {
                    registry.metadataToExtract(
                            MimeType.valueOf("application/json"),
                            new ParameterizedTypeReference<Map<String, Object>>() {
                            }, "headers"
                    );
                })
                .build();
    }
}