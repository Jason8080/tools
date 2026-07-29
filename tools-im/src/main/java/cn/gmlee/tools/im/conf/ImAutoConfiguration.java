package cn.gmlee.tools.im.conf;

import cn.gmlee.tools.im.core.Msg;
import cn.gmlee.tools.im.core.Publisher;
import cn.gmlee.tools.im.core.Subscriber;
import cn.gmlee.tools.im.core.TopicRouter;
import cn.gmlee.tools.im.sse.SseConnectionListener;
import cn.gmlee.tools.im.sse.SseConnectionManager;
import cn.gmlee.tools.im.sse.SseConnectionRegistry;
import cn.gmlee.tools.im.sse.backpressure.*;
import cn.gmlee.tools.im.sse.cleanup.ConnectionReaper;
import cn.gmlee.tools.im.sse.metrics.MicrometerSseMetrics;
import cn.gmlee.tools.im.sse.metrics.NoOpSseMetrics;
import cn.gmlee.tools.im.sse.metrics.SseMetrics;
import cn.gmlee.tools.im.stream.ImBroadcastConsumer;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
                                                      ConnectionReaper reaper,
                                                      @Autowired(required = false) List<SseConnectionListener> listeners) {
        return new SseConnectionManager(properties, registry, strategyResolver, metrics, reaper, listeners);
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

    /**
     * Micrometer 可用时的指标收集器配置.
     * <p>
     * 当 classpath 中存在 MeterRegistry 时，使用基于 Micrometer 的实现。
     * </p>
     */
    @Configuration
    @ConditionalOnClass(MeterRegistry.class)
    static class MicrometerMetricsConfiguration {

        @Bean
        @ConditionalOnMissingBean(SseMetrics.class)
        public SseMetrics sseMetrics(@Autowired(required = false) MeterRegistry meterRegistry,
                                      SseConnectionRegistry registry,
                                      SseProperties properties) {
            return new MicrometerSseMetrics(meterRegistry, registry, properties);
        }
    }

    /**
     * 默认的指标收集器配置（降级方案）.
     * <p>
     * 当 Micrometer 配置类未创建 SseMetrics Bean 时（如 classpath 无 MeterRegistry），
     * 使用 NoOp 实现。
     * </p>
     */
    @Configuration
    @ConditionalOnMissingBean(SseMetrics.class)
    static class NoOpMetricsConfiguration {

        @Bean
        public SseMetrics sseMetrics() {
            log.debug("[Metrics] 使用 NoOp 指标收集器（Micrometer 不可用或未配置）");
            return NoOpSseMetrics.INSTANCE;
        }
    }
}
