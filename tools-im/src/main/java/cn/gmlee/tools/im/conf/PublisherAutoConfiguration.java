package cn.gmlee.tools.im.conf;

import cn.gmlee.tools.im.endpoint.PublisherEndpoint;
import cn.gmlee.tools.im.core.Msg;
import cn.gmlee.tools.im.core.TopicRouter;
import cn.gmlee.tools.im.stream.ImBroadcastPublisher;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;

import java.io.Serializable;

@AutoConfiguration(after = ImAutoConfiguration.class)
public class PublisherAutoConfiguration {

    @Bean
    public ImBroadcastPublisher imBroadcastPublisher(StreamBridge streamBridge) {
        return new ImBroadcastPublisher(streamBridge);
    }

    @Bean
    public PublisherEndpoint publisherController(TopicRouter<Serializable, Msg> topicRouter) {
        return new PublisherEndpoint(topicRouter);
    }
}
