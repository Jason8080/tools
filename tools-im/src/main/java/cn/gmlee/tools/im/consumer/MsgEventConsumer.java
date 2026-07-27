package cn.gmlee.tools.im.consumer;

import cn.gmlee.tools.im.core.Msg;
import cn.gmlee.tools.im.core.MsgEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.util.function.Function;

@Slf4j
public class MsgEventConsumer {

    @Bean
    public Function<Flux<Message<MsgEvent<Msg>>>, Mono<Void>> consumeMsgEvents(Sinks.Many<MsgEvent<Msg>> msgSink) {
        return flux -> flux
                .doOnNext(msg -> {
                    MsgEvent<Msg> event = msg.getPayload();
                    Sinks.EmitResult result = msgSink.tryEmitNext(event);
                    if (result.isFailure()) {
                        log.warn("消息发送失败: {}", result);
                    }
                }).then();
    }
}