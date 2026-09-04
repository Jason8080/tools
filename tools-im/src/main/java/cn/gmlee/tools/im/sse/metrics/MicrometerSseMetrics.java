package cn.gmlee.tools.im.sse.metrics;

import cn.gmlee.tools.im.conf.SseProperties;
import cn.gmlee.tools.im.sse.SseConnectionRegistry;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 基于 Micrometer 的 SSE 指标收集器实现.
 * <p>
 * 封装所有 Micrometer 交互，核心代码通过 {@link SseMetrics} 接口间接依赖。
 * 当 classpath 中存在 Micrometer 且 {@link MeterRegistry} 可用时，自动配置会选择此实现。
 * </p>
 *
 * @see SseMetrics
 * @see NoOpSseMetrics
 */
@Slf4j
public class MicrometerSseMetrics implements SseMetrics {

    private static final String PREFIX = "im.sse";

    /**
     * 错误计数器最大类型数.
     * <p>
     * 防止动态错误类型无限增长导致 MeterRegistry 膨胀。
     * 实际场景中错误类型来自固定集合（reaper_scan、subscribe_init 等），
     * 此限制仅为防御性保护。
     * </p>
     */
    private static final int MAX_ERROR_TYPES = 50;

    private final MeterRegistry registry;
    private final SseConnectionRegistry connectionRegistry;
    private final SseProperties properties;
    private final boolean enabled;

    // 预创建的计数器（避免每次创建）
    private final ConcurrentHashMap<String, Counter> subscribeCounters = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Counter> publishCounters = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Counter> noSubscriberCounters = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Counter> errorCounters = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Timer> subscribeDurationTimers = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Timer> publishDurationTimers = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Counter> directedPublishCounters = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Timer> directedPublishDurationTimers = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Counter> resumeCounters = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Counter> resumedMessageCounters = new ConcurrentHashMap<>();

    private Counter reaperScans;
    private Counter reaperZombies;
    private Counter topicCompacted;

    /**
     * 创建指标收集器.
     *
     * @param registry           Micrometer 注册表（可为 null 表示禁用）
     * @param connectionRegistry 连接注册表（用于 Gauge 回调）
     * @param properties         配置
     */
    public MicrometerSseMetrics(MeterRegistry registry, SseConnectionRegistry connectionRegistry, SseProperties properties) {
        this.connectionRegistry = connectionRegistry;
        this.properties = properties;
        this.enabled = registry != null && properties.getMetrics().isEnabled();
        this.registry = registry;

        if (this.enabled) {
            bindGauges();
            initReaperCounters();
        }
    }

    /**
     * 绑定 Gauge 指标.
     */
    private void bindGauges() {
        // 总连接数（通过 ConnectionCounter）
        registry.gauge(PREFIX + ".connections.total", connectionRegistry, r -> r.getCounter().getTotalConnections().get());

        // 活跃 Sink 数（广播通道）
        registry.gauge(PREFIX + ".sinks.active", connectionRegistry, SseConnectionRegistry::getActiveSinkCount);

        // 活跃定向 Sink 数（定向通道）
        registry.gauge(PREFIX + ".directed-sinks.active", connectionRegistry, SseConnectionRegistry::getDirectedSinkCount);

        // 全局最大连接数上限
        registry.gauge(PREFIX + ".connections.max-total", properties, p -> (long) p.getMaxTotalConnections());
    }

    /**
     * 初始化 Reaper 计数器.
     */
    private void initReaperCounters() {
        reaperScans = Counter.builder(PREFIX + ".reaper.scans")
                .description("SSE 连接收割器扫描次数")
                .register(registry);
        reaperZombies = Counter.builder(PREFIX + ".reaper.zombies")
                .description("SSE 收割器清理的僵尸连接数")
                .register(registry);
        topicCompacted = Counter.builder(PREFIX + ".topics.compacted")
                .description("Topic 计数器压缩次数（空 Topic 计数器条目移除）")
                .register(registry);
    }

    @Override
    public void recordSubscribe(String topic, String result) {
        if (!enabled) return;
        subscribeCounters.computeIfAbsent(topic + ":" + result, k ->
                Counter.builder(PREFIX + ".subscribe.rate")
                        .tag("topic", topic)
                        .tag("result", result)
                        .description("SSE 订阅尝试次数")
                        .register(registry)
        ).increment();
    }

    @Override
    public void recordPublish(String topic, String result) {
        if (!enabled) return;
        if ("NO_SUBSCRIBERS".equals(result)) {
            noSubscriberCounters.computeIfAbsent(topic, k ->
                    Counter.builder(PREFIX + ".publish.no-subscribers")
                            .tag("topic", topic)
                            .description("SSE 无订阅者的消息数")
                            .register(registry)
            ).increment();
        }
        publishCounters.computeIfAbsent(topic + ":" + result, k ->
                Counter.builder(PREFIX + ".publish.rate")
                        .tag("topic", topic)
                        .tag("result", result)
                        .description("SSE 发布尝试次数")
                        .register(registry)
        ).increment();
    }

    @Override
    public void recordReaperScan() {
        if (!enabled) return;
        reaperScans.increment();
    }

    @Override
    public void recordZombieReaped(int count) {
        if (!enabled) return;
        reaperZombies.increment(count);
    }

    @Override
    public void recordTopicCompaction(int count) {
        if (!enabled) return;
        topicCompacted.increment(count);
    }

    @Override
    public void recordError(String type) {
        if (!enabled) return;
        if (errorCounters.size() >= MAX_ERROR_TYPES && !errorCounters.containsKey(type)) {
            log.warn("SSE 错误计数器类型已达上限({}), 跳过注册新类型: {}", MAX_ERROR_TYPES, type);
            return;
        }
        errorCounters.computeIfAbsent(type, k ->
                Counter.builder(PREFIX + ".errors")
                        .tag("type", k)
                        .description("SSE 错误计数")
                        .register(registry)
        ).increment();
    }

    @Override
    public void recordSubscribeDuration(String topic, long durationMs) {
        if (!enabled) return;
        subscribeDurationTimers.computeIfAbsent(topic, k ->
                Timer.builder(PREFIX + ".subscribe.duration")
                        .tag("topic", topic)
                        .description("SSE 订阅延迟")
                        .register(registry)
        ).record(durationMs, TimeUnit.MILLISECONDS);
    }

    @Override
    public void recordPublishDuration(String topic, long durationMs) {
        if (!enabled) return;
        publishDurationTimers.computeIfAbsent(topic, k ->
                Timer.builder(PREFIX + ".publish.duration")
                        .tag("topic", topic)
                        .description("SSE 发布延迟")
                        .register(registry)
        ).record(durationMs, TimeUnit.MILLISECONDS);
    }

    @Override
    public void recordDirectedPublish(String topic, String result) {
        if (!enabled) return;
        directedPublishCounters.computeIfAbsent(topic + ":" + result, k ->
                Counter.builder(PREFIX + ".directed-publish.rate")
                        .tag("topic", topic)
                        .tag("result", result)
                        .description("SSE 定向投递尝试次数")
                        .register(registry)
        ).increment();
    }

    @Override
    public void recordDirectedPublishDuration(String topic, long durationMs) {
        if (!enabled) return;
        directedPublishDurationTimers.computeIfAbsent(topic, k ->
                Timer.builder(PREFIX + ".directed-publish.duration")
                        .tag("topic", topic)
                        .description("SSE 定向投递延迟")
                        .register(registry)
        ).record(durationMs, TimeUnit.MILLISECONDS);
    }

    @Override
    public void recordResume(String topic, String result) {
        if (!enabled) return;
        resumeCounters.computeIfAbsent(topic + ":" + result, k ->
                Counter.builder(PREFIX + ".resume.rate")
                        .tag("topic", topic)
                        .tag("result", result)
                        .description("SSE 断点续传会话结果")
                        .register(registry)
        ).increment();
    }

    @Override
    public void recordResumedMessages(String topic, long count) {
        if (!enabled || count <= 0) return;
        resumedMessageCounters.computeIfAbsent(topic, k ->
                Counter.builder(PREFIX + ".resume.messages")
                        .tag("topic", topic)
                        .description("回放给客户端的历史消息数")
                        .register(registry)
        ).increment(count);
    }

    @Override
    public void cleanupTopic(String topic) {
        if (!enabled) return;

        // 清理 subscribe counters (key = "topic:result")
        subscribeCounters.entrySet().removeIf(entry -> {
            if (entry.getKey().startsWith(topic + ":")) {
                registry.remove(entry.getValue());
                return true;
            }
            return false;
        });

        // 清理 publish counters
        publishCounters.entrySet().removeIf(entry -> {
            if (entry.getKey().startsWith(topic + ":")) {
                registry.remove(entry.getValue());
                return true;
            }
            return false;
        });

        // 清理 no-subscriber counters (key = topic)
        Counter noSub = noSubscriberCounters.remove(topic);
        if (noSub != null) {
            registry.remove(noSub);
        }

        // 清理 duration timers (key = topic)
        Timer subTimer = subscribeDurationTimers.remove(topic);
        if (subTimer != null) {
            registry.remove(subTimer);
        }
        Timer pubTimer = publishDurationTimers.remove(topic);
        if (pubTimer != null) {
            registry.remove(pubTimer);
        }

        // 清理 directed publish counters (key = "topic:result")
        directedPublishCounters.entrySet().removeIf(entry -> {
            if (entry.getKey().startsWith(topic + ":")) {
                registry.remove(entry.getValue());
                return true;
            }
            return false;
        });

        // 清理 directed publish duration timers (key = topic)
        Timer directedTimer = directedPublishDurationTimers.remove(topic);
        if (directedTimer != null) {
            registry.remove(directedTimer);
        }

        // 清理 resume counters (key = "topic:result")
        resumeCounters.entrySet().removeIf(entry -> {
            if (entry.getKey().startsWith(topic + ":")) {
                registry.remove(entry.getValue());
                return true;
            }
            return false;
        });

        // 清理 resumed message counters (key = topic)
        Counter resumedCounter = resumedMessageCounters.remove(topic);
        if (resumedCounter != null) {
            registry.remove(resumedCounter);
        }
    }

    @Override
    public void shutdownCleanup() {
        if (!enabled) return;
        subscribeCounters.clear();
        publishCounters.clear();
        noSubscriberCounters.clear();
        errorCounters.clear();
        subscribeDurationTimers.clear();
        publishDurationTimers.clear();
        directedPublishCounters.clear();
        directedPublishDurationTimers.clear();
        resumeCounters.clear();
        resumedMessageCounters.clear();
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
