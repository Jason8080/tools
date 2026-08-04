package cn.gmlee.tools.im.conf;

import cn.gmlee.tools.im.spi.routing.DefaultRoutingKeyComposer;
import cn.gmlee.tools.im.spi.routing.RoutingKeyComposer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class SpiAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(RoutingKeyComposer.class)
    public RoutingKeyComposer routingKeyComposer() {
        return new DefaultRoutingKeyComposer();
    }
}
