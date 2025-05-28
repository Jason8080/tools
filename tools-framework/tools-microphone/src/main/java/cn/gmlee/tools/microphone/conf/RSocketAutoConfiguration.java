package cn.gmlee.tools.microphone.conf;

import org.springframework.boot.rsocket.messaging.RSocketStrategiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.codec.ByteBufferDecoder;
import org.springframework.core.codec.ByteBufferEncoder;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;

/**
 * RSocket自动装配.
 */
@Configuration(proxyBeanMethods = false)
@PropertySource(value = {"classpath:microphone.properties", "classpath:microphone.properties"}, ignoreResourceNotFound = true)
public class RSocketAutoConfiguration {

    @Bean
    public RSocketStrategiesCustomizer strategiesCustomizer() {
        return strategies -> strategies
                .decoder(new Jackson2JsonDecoder()) // 处理String部分
                .decoder(new ByteBufferDecoder())   // 处理byte[]部分
                .encoder(new Jackson2JsonEncoder()) // 处理String部分
                .encoder(new ByteBufferEncoder());  // 处理byte[]部分
    }
}