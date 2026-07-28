package cn.gmlee.tools.im.conf;

import cn.gmlee.tools.im.controller.PublisherController;
import cn.gmlee.tools.im.controller.SubscriberController;
import cn.gmlee.tools.im.core.Msg;
import cn.gmlee.tools.im.core.Publisher;
import cn.gmlee.tools.im.core.Subscriber;
import cn.gmlee.tools.im.core.TopicRouter;
import cn.gmlee.tools.im.sse.SseConnectionManager;
import cn.gmlee.tools.im.stream.StreamBroadcastConsumer;
import cn.gmlee.tools.im.topic.ImBroadcastPublisher;
import cn.gmlee.tools.im.topic.ImBroadcastSubscriber;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.function.StreamBridge;
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

    @Bean("sseImBroadcastConsumer")
    public StreamBroadcastConsumer sseImBroadcastConsumer(SseConnectionManager sseConnectionManager,
                                                          StreamProperties streamProperties) {
        return new StreamBroadcastConsumer(sseConnectionManager, streamProperties);
    }

    @Bean
    public ImBroadcastPublisher imBroadcastPublisher(StreamBridge streamBridge) {
        return new ImBroadcastPublisher(streamBridge);
    }

    @Bean
    public ImBroadcastSubscriber imBroadcastSubscriber(SseConnectionManager sseConnectionManager) {
        return new ImBroadcastSubscriber(sseConnectionManager);
    }

    @Bean
    public TopicRouter<Serializable, Msg> topicRouter(List<Publisher<Serializable, Msg>> publishers,
                                                      List<Subscriber<Flux<Msg>>> subscribers) {
        return new TopicRouter<>(publishers, subscribers);
    }

    @Bean
    public PublisherController publisherController(TopicRouter<Serializable, Msg> topicRouter) {
        return new PublisherController(topicRouter);
    }

    @Bean
    public SubscriberController subscriberController(TopicRouter<Serializable, Msg> topicRouter) {
        return new SubscriberController(topicRouter);
    }
}
