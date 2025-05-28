package cn.gmlee.tools.microphone.conf;

import io.rsocket.AbstractRSocket;
import org.springframework.boot.rsocket.server.RSocketServerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import reactor.core.publisher.Mono;

/**
 * RSocket自动装配.
 */
@Configuration(proxyBeanMethods = false)
@PropertySource(value = {"classpath:microphone.properties", "classpath:microphone.properties"}, ignoreResourceNotFound = true)
public class RSocketAutoConfiguration {

    @Bean
    public RSocketServerCustomizer rSocketServerCustomizer() {
        return server -> server.acceptor((setup, sendingSocket) -> Mono.just(new AbstractRSocket() {}));
    }
}