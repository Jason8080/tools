package cn.gmlee.tools.im.serve;

import cn.gmlee.tools.im.core.MsgEvent;
import reactor.core.publisher.Sinks;
import org.springframework.context.annotation.Bean;

/**
 * 广播服务
 */
public class BroadcasterServe {

    @Bean
    public Sinks.Many<MsgEvent> priceSink() {
        return Sinks.many().multicast().onBackpressureBuffer(1024);
    }

}
