package cn.gmlee.tools.im.conf;

import cn.gmlee.tools.im.core.Msg;
import cn.gmlee.tools.im.core.MsgEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.util.function.Function;

@Slf4j
@EnableConfigurationProperties(ImProperties.class)
public class MsgStreamAutoConfiguration {

    @Bean
    public Sinks.Many<MsgEvent<Msg>> sinkMany() {
        return Sinks.many().multicast().onBackpressureBuffer(1024);
    }

    @Bean
    public Function<Flux<Message<MsgEvent<Msg>>>, Mono<Void>> consume(Sinks.Many<MsgEvent<Msg>> sinkMany) {
        return flux -> flux
                .doOnNext(msg -> {
                    MsgEvent<Msg> event = msg.getPayload();
                    Sinks.EmitResult result = sinkMany.tryEmitNext(event);
                    if (result.isFailure()) {
                        log.warn("消息发送失败: {}", result);
                    }
                }).then();
    }
}
