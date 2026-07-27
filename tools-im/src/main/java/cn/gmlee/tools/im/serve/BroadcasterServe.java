package cn.gmlee.tools.im.serve;

import cn.gmlee.tools.im.core.MsgEvent;
import org.springframework.context.annotation.Bean;
import reactor.core.publisher.Sinks;

/**
 * 广播服务
 */
public class BroadcasterServe {

    @Bean
    public Sinks.Many<MsgEvent> msgEventSink() {
        return Sinks.many().multicast().onBackpressureBuffer(1024);
    }

}
