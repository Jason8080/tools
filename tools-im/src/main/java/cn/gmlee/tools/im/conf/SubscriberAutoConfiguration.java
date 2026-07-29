package cn.gmlee.tools.im.conf;

import cn.gmlee.tools.im.endpoint.SubscriberEndpoint;
import cn.gmlee.tools.im.core.Msg;
import cn.gmlee.tools.im.core.TopicRouter;
import cn.gmlee.tools.im.sse.SseConnectionManager;
import cn.gmlee.tools.im.topic.ImBroadcastSubscriber;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

import java.io.Serializable;

@AutoConfiguration(after = ImAutoConfiguration.class)
public class SubscriberAutoConfiguration {

    @Bean
    public ImBroadcastSubscriber imBroadcastSubscriber(SseConnectionManager sseConnectionManager) {
        return new ImBroadcastSubscriber(sseConnectionManager);
    }

    @Bean
    public SubscriberEndpoint subscriberEndpoint(TopicRouter<Serializable, Msg> topicRouter,
                                                  SseProperties sseProperties) {
        return new SubscriberEndpoint(topicRouter, sseProperties);
    }
}
