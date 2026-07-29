package cn.gmlee.tools.im.conf;

import cn.gmlee.tools.im.core.Msg;
import cn.gmlee.tools.im.core.Publisher;
import cn.gmlee.tools.im.core.Subscriber;
import cn.gmlee.tools.im.core.TopicRouter;
import cn.gmlee.tools.im.sse.SseConnectionManager;
import cn.gmlee.tools.im.sse.SseConnectionRegistry;
import cn.gmlee.tools.im.sse.SseMetrics;
import cn.gmlee.tools.im.sse.backpressure.*;
import cn.gmlee.tools.im.sse.cleanup.ConnectionReaper;
import cn.gmlee.tools.im.stream.ImBroadcastConsumer;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import reactor.core.publisher.Flux;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * IM 框架核心自动配置.
 */
@Slf4j
@AutoConfiguration
@EnableConfigurationProperties({
        ImProperties.class, StreamProperties.class, SseProperties.class,
})
public class ImAutoConfiguration {

    @Bean
    public BackpressureStrategyResolver backpressureStrategyResolver(SseProperties properties) {
        // 构建策略映射
        Map<String, BackpressureStrategy> strategyMap = new HashMap<>();
        strategyMap.put(BufferBackpressureStrategy.NAME, new BufferBackpressureStrategy());
        strategyMap.put(DropOldestBackpressureStrategy.NAME, new DropOldestBackpressureStrategy());
        strategyMap.put(ErrorBackpressureStrategy.NAME, new ErrorBackpressureStrategy());

        return new DefaultBackpressureStrategyResolver(properties, strategyMap);
    }

    @Bean
    public SseConnectionRegistry sseConnectionRegistry(SseProperties properties,
                                                        BackpressureStrategyResolver strategyResolver) {
        return new SseConnectionRegistry(properties, strategyResolver);
    }

    @Bean
    public SseMetrics sseMetrics(@Autowired(required = false) MeterRegistry meterRegistry,
                                  SseConnectionRegistry registry,
                                  SseProperties properties) {
        return new SseMetrics(meterRegistry, registry, properties);
    }

    @Bean
    public ConnectionReaper connectionReaper(SseConnectionRegistry registry,
                                              SseMetrics metrics,
                                              SseProperties properties) {
        return new ConnectionReaper(registry, metrics, properties);
    }

    @Bean
    public SseConnectionManager sseConnectionManager(SseProperties properties,
                                                      SseConnectionRegistry registry,
                                                      BackpressureStrategyResolver strategyResolver,
                                                      SseMetrics metrics,
                                                      ConnectionReaper reaper) {
        return new SseConnectionManager(properties, registry, strategyResolver, metrics, reaper);
    }

    @Bean
    public ImBroadcastConsumer imBroadcastConsumer(SseConnectionManager sseConnectionManager) {
        return new ImBroadcastConsumer(sseConnectionManager);
    }

    @Bean
    public TopicRouter<Serializable, Msg> topicRouter(List<Publisher<Serializable, Msg>> publishers,
                                                      List<Subscriber<Flux<Msg>>> subscribers) {
        return new TopicRouter<>(publishers, subscribers);
    }
}
