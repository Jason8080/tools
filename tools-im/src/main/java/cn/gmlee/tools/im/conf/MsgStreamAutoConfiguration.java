package cn.gmlee.tools.im.conf;

import cn.gmlee.tools.im.core.MsgEvent;
import org.springframework.context.annotation.Bean;
import reactor.core.publisher.Sinks;

public class MsgStreamAutoConfiguration {

    @Bean
    public Sinks.Many<MsgEvent> msgEventSink() {
        return Sinks.many().multicast().onBackpressureBuffer(1024);
    }
}
