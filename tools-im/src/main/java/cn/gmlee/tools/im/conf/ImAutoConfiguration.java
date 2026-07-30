package cn.gmlee.tools.im.conf;

import cn.gmlee.tools.im.spi.listener.SseConnectionListener;
import cn.gmlee.tools.im.sse.SseConnectionManager;
import cn.gmlee.tools.im.sse.SseConnectionRegistry;
import cn.gmlee.tools.im.sse.backpressure.BackpressureStrategy;
import cn.gmlee.tools.im.sse.backpressure.BackpressureStrategyResolver;
import cn.gmlee.tools.im.sse.backpressure.BufferBackpressureStrategy;
import cn.gmlee.tools.im.sse.backpressure.DefaultBackpressureStrategyResolver;
import cn.gmlee.tools.im.sse.backpressure.DropOldestBackpressureStrategy;
import cn.gmlee.tools.im.sse.backpressure.ErrorBackpressureStrategy;
import cn.gmlee.tools.im.sse.cleanup.ConnectionReaper;
import cn.gmlee.tools.im.sse.metrics.MicrometerSseMetrics;
import cn.gmlee.tools.im.sse.metrics.NoOpSseMetrics;
import cn.gmlee.tools.im.sse.metrics.SseMetrics;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * IM 框架核心自动配置.
 * <p>
 * 注册 SSE 子系统核心 Bean（连接管理器、注册表、收割器、指标等）。
 * 端点路由由 {@link EndpointAutoConfiguration} 负责。
 * </p>
 *
 * @since 5.6.0
 */
@Slf4j
@AutoConfiguration
@EnableConfigurationProperties({ImProperties.class, SseProperties.class})
public class ImAutoConfiguration {

    @Bean
    public BackpressureStrategyResolver backpressureStrategyResolver(SseProperties properties) {
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
                                                      SseMetrics metrics,
                                                      ConnectionReaper reaper,
                                                      @Autowired(required = false) List<SseConnectionListener> listeners) {
        return new SseConnectionManager(properties, registry, metrics, reaper, listeners);
    }

    /**
     * Micrometer 可用时的指标收集器配置.
     */
    @Configuration
    @ConditionalOnClass(MeterRegistry.class)
    static class MicrometerMetricsConfiguration {

        @Bean
        @ConditionalOnMissingBean(SseMetrics.class)
        public SseMetrics sseMetrics(@Autowired(required = false) MeterRegistry meterRegistry,
                                      SseConnectionRegistry registry,
                                      SseProperties properties) {
            MicrometerSseMetrics metrics = new MicrometerSseMetrics(meterRegistry, registry, properties);
            // 延迟注入 metrics 到 registry，避免循环依赖
            registry.setMetrics(metrics);
            return metrics;
        }
    }

    /**
     * 默认的指标收集器配置（降级方案）.
     */
    @Configuration
    @ConditionalOnMissingBean(SseMetrics.class)
    static class NoOpMetricsConfiguration {

        @Bean
        public SseMetrics sseMetrics(SseConnectionRegistry registry) {
            log.debug("[Metrics] 使用 NoOp 指标收集器（Micrometer 不可用或未配置）");
            NoOpSseMetrics metrics = NoOpSseMetrics.INSTANCE;
            // 延迟注入 metrics 到 registry，避免循环依赖
            registry.setMetrics(metrics);
            return metrics;
        }
    }
}
