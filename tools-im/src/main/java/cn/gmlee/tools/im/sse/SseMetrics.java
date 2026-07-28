package cn.gmlee.tools.im.sse;

import cn.gmlee.tools.im.conf.SseProperties;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * SSE 指标收集器.
 * <p>
 * 封装所有 Micrometer 交互，核心代码不直接依赖 io.micrometer。
 * 如果 MeterRegistry 不可用，则使用 NoOp 实现。
 * </p>
 *
 * <h3>暴露的指标</h3>
 * <ul>
 *   <li>im.sse.connections.total - 总连接数（Gauge）</li>
 *   <li>im.sse.connections.active - 每 Topic 连接数（Gauge）</li>
 *   <li>im.sse.subscribe.rate - 订阅尝试（Counter，标签：topic, result）</li>
 *   <li>im.sse.publish.rate - 发布尝试（Counter，标签：topic, result）</li>
 *   <li>im.sse.publish.no-subscribers - 无订阅者消息（Counter，标签：topic）</li>
 *   <li>im.sse.reaper.scans - 收割扫描次数（Counter）</li>
 *   <li>im.sse.reaper.zombies - 收割的僵尸连接（Counter）</li>
 *   <li>im.sse.sinks.active - 活跃 Sink 数（Gauge）</li>
 *   <li>im.sse.errors - 错误计数（Counter，标签：type）</li>
 *   <li>im.sse.subscribe.duration - 订阅延迟（Timer，标签：topic）</li>
 *   <li>im.sse.publish.duration - 发布延迟（Timer，标签：topic）</li>
 * </ul>
 */
@Slf4j
public class SseMetrics {

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

    private Counter reaperScans;
    private Counter reaperZombies;

    /**
     * 创建指标收集器.
     *
     * @param registry          Micrometer 注册表（可为 null 表示禁用）
     * @param connectionRegistry 连接注册表（用于 Gauge 回调）
     * @param properties        配置
     */
    public SseMetrics(MeterRegistry registry, SseConnectionRegistry connectionRegistry, SseProperties properties) {
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
        // 总连接数
        registry.gauge(PREFIX + ".connections.total", connectionRegistry, r -> r.getTotalConnections().get());

        // 活跃 Sink 数
        registry.gauge(PREFIX + ".sinks.active", connectionRegistry, SseConnectionRegistry::getActiveSinkCount);

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
    }

    /**
     * 记录订阅结果.
     *
     * @param topic  Topic
     * @param result 结果（SUCCESS / REJECTED_GLOBAL / REJECTED_TOPIC）
     */
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

    /**
     * 记录发布结果.
     *
     * @param topic  Topic
     * @param result 结果（SUCCESS / NO_SUBSCRIBERS / EMIT_FAILURE）
     */
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

    /**
     * 记录收割扫描.
     */
    public void recordReaperScan() {
        if (!enabled) return;
        reaperScans.increment();
    }

    /**
     * 记录收割的僵尸连接.
     *
     * @param count 数量
     */
    public void recordZombieReaped(int count) {
        if (!enabled) return;
        reaperZombies.increment(count);
    }

    /**
     * 记录错误.
     * <p>
     * 错误类型来自固定集合（如 reaper_scan、subscribe_init），数量有限。
     * 当类型数超过 {@link #MAX_ERROR_TYPES} 时，新类型不再注册，防止 MeterRegistry 无限膨胀。
     * 所有错误计数器在 {@link #shutdownCleanup()} 中统一清理。
     * </p>
     *
     * @param type 错误类型
     */
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

    /**
     * 记录订阅延迟.
     *
     * @param topic      Topic
     * @param durationMs 延迟（毫秒）
     */
    public void recordSubscribeDuration(String topic, long durationMs) {
        if (!enabled) return;
        subscribeDurationTimers.computeIfAbsent(topic, k ->
                Timer.builder(PREFIX + ".subscribe.duration")
                        .tag("topic", topic)
                        .description("SSE 订阅延迟")
                        .register(registry)
        ).record(durationMs, TimeUnit.MILLISECONDS);
    }

    /**
     * 记录发布延迟.
     *
     * @param topic      Topic
     * @param durationMs 延迟（毫秒）
     */
    public void recordPublishDuration(String topic, long durationMs) {
        if (!enabled) return;
        publishDurationTimers.computeIfAbsent(topic, k ->
                Timer.builder(PREFIX + ".publish.duration")
                        .tag("topic", topic)
                        .description("SSE 发布延迟")
                        .register(registry)
        ).record(durationMs, TimeUnit.MILLISECONDS);
    }

    /**
     * 清理指定 Topic 的所有指标.
     * <p>
     * 当 Topic 被销毁时调用，从本地缓存和 MeterRegistry 中移除该 Topic 的指标对象，
     * 防止动态 Topic 场景下指标对象无限增长。
     * </p>
     * <p>
     * 注意：errorCounters 按错误类型（而非 Topic）索引，类型数量有限且有上限保护
     * （见 {@link #MAX_ERROR_TYPES}），不在此处清理。全量清理由 {@link #shutdownCleanup()} 负责。
     * </p>
     *
     * @param topic 要清理的 Topic
     */
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
    }

    /**
     * 关闭时清理所有本地指标缓存.
     * <p>
     * 清理所有 Counter/Timer 的本地缓存引用，防止组件重启后旧指标对象残留。
     * 在 {@link cn.gmlee.tools.im.sse.SseConnectionManager#stop} 的异步关闭任务中调用。
     * </p>
     */
    public void shutdownCleanup() {
        if (!enabled) return;
        subscribeCounters.clear();
        publishCounters.clear();
        noSubscriberCounters.clear();
        errorCounters.clear();
        subscribeDurationTimers.clear();
        publishDurationTimers.clear();
    }

    /**
     * 检查是否启用指标.
     *
     * @return 启用返回 true
     */
    public boolean isEnabled() {
        return enabled;
    }
}
