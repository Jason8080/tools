package cn.gmlee.tools.im.conf;

import cn.gmlee.tools.im.endpoint.EndpointRegistry;
import cn.gmlee.tools.im.resume.AtomicSequenceEventIdGenerator;
import cn.gmlee.tools.im.resume.EventIdCodec;
import cn.gmlee.tools.im.resume.EventIdComparator;
import cn.gmlee.tools.im.resume.EventIdGenerator;
import cn.gmlee.tools.im.resume.InMemoryMessageHistoryStore;
import cn.gmlee.tools.im.resume.MessageHistoryStore;
import cn.gmlee.tools.im.resume.ResumeSupport;
import cn.gmlee.tools.im.resume.SnowflakeEventIdGenerator;
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
import cn.gmlee.tools.im.sse.retry.EmitRetryStrategyFactory;
import cn.gmlee.tools.im.sse.retry.EmitRetryStrategyResolver;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

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

    /**
     * 端点注册表 Bean.
     * <p>
     * 由核心配置类创建，{@link EndpointAutoConfiguration} 通过构造器注入使用。
     * 若放在 {@code EndpointAutoConfiguration} 内部作为 {@code @Bean}，
     * 会与自身的构造器参数形成循环依赖，故提前至此处。
     * </p>
     */
    @Bean
    @ConditionalOnMissingBean
    public EndpointRegistry endpointRegistry() {
        return new EndpointRegistry();
    }

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
    @ConditionalOnMissingBean
    public EmitRetryStrategyResolver emitRetryStrategyResolver(SseProperties properties) {
        return EmitRetryStrategyFactory.createResolver(properties.getEmitRetry());
    }

    @Bean
    public SseConnectionManager sseConnectionManager(SseProperties properties,
                                                      SseConnectionRegistry registry,
                                                      SseMetrics metrics,
                                                      ConnectionReaper reaper,
                                                      EmitRetryStrategyResolver retryStrategyResolver,
                                                      @Autowired(required = false) List<SseConnectionListener> listeners) {
        return new SseConnectionManager(properties, registry, metrics, reaper, listeners, retryStrategyResolver);
    }

    // ==================== 断点续传（v5.7.0+） ====================

    /**
     * 事件 ID 编解码器（SSE {@code id:} 字段 ↔ 位点对象）.
     *
     * @since 5.7.0
     */
    @Bean
    @ConditionalOnMissingBean
    public EventIdCodec eventIdCodec() {
        return EventIdCodec.DEFAULT;
    }

    /**
     * 事件 ID 顺序比较器（水位线去重）.
     *
     * @since 5.7.0
     */
    @Bean
    @ConditionalOnMissingBean
    public EventIdComparator eventIdComparator() {
        return EventIdComparator.DEFAULT;
    }

    /**
     * 事件 ID 生成器（push 路径统一分配消息 ID）.
     * <p>
     * {@code im.sse.resume.snowflake.enabled=true} 时使用雪花算法
     * （CLUSTER 多实例 + 续传场景必需），否则使用单 JVM 自增序列。
     * </p>
     *
     * @since 5.7.0
     */
    @Bean
    @ConditionalOnMissingBean
    public EventIdGenerator eventIdGenerator(SseProperties properties) {
        SseProperties.SnowflakeConfig snowflake = properties.getResume().getSnowflake();
        if (!snowflake.isEnabled()) {
            return new AtomicSequenceEventIdGenerator();
        }
        long workerId = snowflake.getWorkerId();
        if (workerId < 0) {
            workerId = autoWorkerId();
            log.warn("[ImAutoConfiguration] 未配置 im.sse.resume.snowflake.worker-id，"
                    + "按主机名哈希自动分配 workerId={}（存在小概率冲突，生产环境建议显式配置）", workerId);
        }
        log.info("[ImAutoConfiguration] 启用雪花算法事件 ID 生成器: workerId={}", workerId);
        return new SnowflakeEventIdGenerator(workerId);
    }

    /**
     * 断点续传支持门面.
     * <p>
     * 汇总全部 {@link MessageHistoryStore} Bean（无存储时读取侧自动退化为仅实时流，
     * 写入侧挂点空转，开销可忽略）。
     * </p>
     *
     * @since 5.7.0
     */
    @Bean
    @ConditionalOnMissingBean
    public ResumeSupport resumeSupport(SseProperties properties,
                                       EventIdCodec eventIdCodec,
                                       EventIdComparator eventIdComparator,
                                       @Autowired(required = false) List<MessageHistoryStore> stores,
                                       SseMetrics metrics) {
        int storeCount = stores != null ? stores.size() : 0;
        log.info("[ImAutoConfiguration] 断点续传支持已装配: enabled={}, 历史存储数={}",
                properties.getResume().isEnabled(), storeCount);
        return new ResumeSupport(properties.getResume(), eventIdCodec, eventIdComparator, stores, metrics);
    }

    /**
     * 内存历史存储配置（开发/单机场景）.
     * <p>
     * {@code im.sse.resume.in-memory.enabled=true} 时激活。
     * </p>
     *
     * @since 5.7.0
     */
    @Configuration
    @ConditionalOnProperty(prefix = "im.sse.resume.in-memory", name = "enabled", havingValue = "true")
    static class InMemoryHistoryConfiguration {

        @Bean
        @ConditionalOnMissingBean
        InMemoryMessageHistoryStore inMemoryMessageHistoryStore(SseProperties properties) {
            SseProperties.InMemoryHistoryConfig config = properties.getResume().getInMemory();
            log.info("[ImAutoConfiguration] 启用内存消息历史存储: capacityPerTopic={}",
                    config.getCapacityPerTopic());
            return new InMemoryMessageHistoryStore(config.getCapacityPerTopic());
        }
    }

    /**
     * 按主机名哈希分配雪花算法 workerId（0~1023）.
     * <p>
     * 主机名解析失败时随机分配。自动分配存在小概率冲突，仅为兜底。
     * </p>
     */
    private static long autoWorkerId() {
        String host;
        try {
            host = InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            host = null;
        }
        if (host == null || host.isBlank()) {
            return ThreadLocalRandom.current().nextLong(1024);
        }
        return (host.hashCode() & 0x7FFFFFFF) % 1024;
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
