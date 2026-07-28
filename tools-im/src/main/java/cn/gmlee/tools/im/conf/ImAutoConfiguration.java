package cn.gmlee.tools.im.conf;

import cn.gmlee.tools.im.core.Msg;
import cn.gmlee.tools.im.core.Publisher;
import cn.gmlee.tools.im.core.Subscriber;
import cn.gmlee.tools.im.core.TopicRouter;
import cn.gmlee.tools.im.sse.SseConnectionManager;
import cn.gmlee.tools.im.stream.ImBroadcastConsumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import reactor.core.publisher.Flux;

import java.io.Serializable;
import java.util.List;

@Slf4j
@AutoConfiguration
@EnableConfigurationProperties({
        ImProperties.class, StreamProperties.class, SseProperties.class,
})
public class ImAutoConfiguration {

    @Bean
    public SseConnectionManager sseConnectionManager(SseProperties sseProperties) {
        return new SseConnectionManager(sseProperties);
    }

    @Bean
    public ImBroadcastConsumer imBroadcastConsumer(SseConnectionManager sseConnectionManager,
                                                      StreamProperties streamProperties) {
        return new ImBroadcastConsumer(sseConnectionManager, streamProperties);
    }

    @Bean
    public TopicRouter<Serializable, Msg> topicRouter(List<Publisher<Serializable, Msg>> publishers,
                                                      List<Subscriber<Flux<Msg>>> subscribers) {
        return new TopicRouter<>(publishers, subscribers);
    }
}
