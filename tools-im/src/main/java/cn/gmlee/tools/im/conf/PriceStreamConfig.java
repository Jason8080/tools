package cn.gmlee.tools.im.conf;

import cn.gmlee.tools.im.core.MsgEvent;
import org.springframework.context.annotation.Bean;

public class PriceStreamConfig {

    @Bean
    public Sinks.Many<MsgEvent> priceSink() {
        return Sinks.many().multicast().onBackpressureBuffer(1024);
    }
}
