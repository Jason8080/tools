package cn.gmlee.tools.im.sse;

import cn.gmlee.tools.im.conf.SseProperties;
import cn.gmlee.tools.im.model.ConnectionMetadata;
import cn.gmlee.tools.im.model.Msg;
import cn.gmlee.tools.im.model.TopicMessage;
import cn.gmlee.tools.im.ex.SseConnectionLimitExceededException;
import cn.gmlee.tools.im.ex.SseShutdownException;

import cn.gmlee.tools.im.sse.cleanup.ConnectionReaper;
import cn.gmlee.tools.im.sse.internal.ConnectionCounter;
import cn.gmlee.tools.im.sse.internal.SseExecutorFactory;
import cn.gmlee.tools.im.sse.metrics.SseMetrics;
import cn.gmlee.tools.im.spi.listener.SseConnectionListener;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.SmartLifecycle;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * SSE 连接管理器（门面）.
 * <p>
 * 这是 SSE 连接管理的唯一公共入口，保持与旧版本相同的 API 签名以兼容下游代码。
 * 内部委托给 {@link SseConnectionRegistry}、{@link ConnectionReaper}、{@link SseMetrics}、
 * {@link SseConnectionFluxBuilder} 等组件。
 * </p>
 *
 * <h3>核心特性</h3>
 * <ul>
 *   <li><b>无锁并发</b>：使用 CAS 循环保证连接数限制的原子性</li>
 *   <li><b>三层清理</b>：doFinally（响应式） + Reaper（定时清理） + maxConnectionLifetime（强制清理）</li>
 *   <li><b>优雅关闭</b>：实现 SmartLifecycle，按阶段排空连接</li>
 *   <li><b>可观测性</b>：通过 SseMetrics 暴露 Micrometer 指标，
 *       通过 {@link SseConnectionListener} 暴露生命周期事件</li>
 * </ul>
 *
 * <h3>职责分离</h3>
 * <p>
 * 管理器专注于协调：预检查、计数器管理、生命周期控制。
 * Flux 构建细节（Sink 创建、连接注册、生命周期钩子）委托给 {@link SseConnectionFluxBuilder}。
 * </p>
 */
@Slf4j
public class SseConnectionManager implements SmartLifecycle {

    private final SseProperties properties;
    private final SseConnectionRegistry registry;
    private final SseMetrics metrics;
    private final ConnectionReaper reaper;
    private final List<SseConnectionListener> listeners;

    /**
     * 生命周期调度器（daemon 线程）
     */
    private volatile ScheduledExecutorService lifecycleScheduler;

    /**
     * 是否正在接受新连接
     */
    private final AtomicBoolean accepting = new AtomicBoolean(true);

    /**
     * 是否已完成关闭
     */
    @Getter
    private volatile boolean closed = false;

    /**
     * SmartLifecycle 运行状态
     */
    @Getter
    private volatile boolean running = false;

    /**
     * 异步 drain 任务.
     * <p>
     * 使用 CompletableFuture 管理异步关闭任务，支持取消操作。
     * 每次 {@link #start()} 时取消旧任务，避免影响新生命周期。
     * </p>
     */
    private volatile CompletableFuture<Void> currentDrainTask;

    /**
     * 创建连接管理器.
     *
     * @param properties       SSE 配置
     * @param registry         连接注册表
     * @param metrics          指标收集器
     * @param reaper           连接收割器
     * @param listeners        连接生命周期监听器列表（可为空）
     */
    public SseConnectionManager(SseProperties properties,
                                SseConnectionRegistry registry,
                                SseMetrics metrics,
                                ConnectionReaper reaper,
                                List<SseConnectionListener> listeners) {
        this.properties = properties;
        this.registry = registry;
        this.metrics = metrics;
        this.reaper = reaper;
        this.listeners = listeners != null ? listeners : Collections.emptyList();
    }

    // ==================== 公共 API ====================

    /**
     * 订阅 Topic.
     * <p>
     * 返回延迟执行的 Flux，在实际订阅时执行预检查和连接创建。
     * 完整流程：
     * <ol>
     *   <li>预检查：是否接受新连接</li>
     *   <li>计数器递增：CAS 循环检查全局/Topic 限制</li>
     *   <li>二次检查：防止 CAS 期间进入关闭流程</li>
     *   <li>Flux 构建：委托给 {@link SseConnectionFluxBuilder}</li>
     * </ol>
     * </p>
     * <p>
     * 返回的 Flux 通过 Reactor Context 携带连接引用，键为 {@link #CONTEXT_KEY_CONNECTION}。
     * 调用方可通过 {@code Flux.deferContextual} 获取连接并执行操作（如心跳时调用 {@code touch()}）。
     * </p>
     * <p>
     * <b>注意</b>：返回的 Flux 必须被订阅。如果 Flux 创建后未被订阅（如客户端在订阅前断开），
     * 连接会在 {@code reaper.idleTimeout}（默认 3600s）后被 Reaper 作为空闲连接清理。
     * </p>
     *
     * @param topic    Topic 名称
     * @param metadata 连接元数据（身份标识等）
     * @return 消息流（Context 中携带连接引用）
     */
    public Flux<TopicMessage> subscribe(String topic, ConnectionMetadata metadata) {
        return Flux.defer(() -> {
            // 1. 预检查
            if (!checkAccepting(topic)) {
                return Flux.error(SseShutdownException.INSTANCE);
            }

            // 2. 获取连接许可
            ConnectionCounter counter = registry.getCounter();
            ConnectionCounter.AcquireResult acquireResult = counter.tryAcquire(
                    topic, properties.getMaxTotalConnections(), properties.getMaxConnectionsPerTopic());

            if (!acquireResult.isSuccess()) {
                recordRejection(topic, acquireResult.getException());
                return Flux.error(acquireResult.getException());
            }

            // 3. 二次检查（防止 CAS 期间进入关闭流程）
            if (!checkAccepting(topic)) {
                counter.rollback(topic);
                metrics.recordSubscribe(topic, "REJECTED_SHUTDOWN");
                return Flux.error(SseShutdownException.INSTANCE);
            }

            // 4. 构建连接流（委托给 FluxBuilder）
            SseSubscription sub = SseConnectionFluxBuilder.build(topic, metadata, registry, metrics, properties, listeners);
            // 将连接引用写入 Reactor Context
            return sub.getFlux().contextWrite(ctx -> ctx.put(CONTEXT_KEY_CONNECTION, sub.getConnection()));
        });
    }

    /**
     * Reactor Context 中存储连接引用的键.
     * <p>
     * 调用方可通过此键从 Context 中获取 {@link SseConnection} 引用：
     * <pre>{@code
     * flux.deferContextual(ctx -> {
     *     SseConnection conn = ctx.getOrDefault(SseConnectionManager.CONTEXT_KEY_CONNECTION, null);
     *     // 使用 conn 执行操作
     * })
     * }</pre>
     * </p>
     */
    public static final String CONTEXT_KEY_CONNECTION = "sse.connection";

    /**
     * 发布消息到 Topic.
     * <p>
     * 双通道投递：
     * <ul>
     *   <li>广播（routingKeys 为空）→ topicSink → 所有连接</li>
     *   <li>定向（routingKeys 非空）→ 反向索引查找 → 目标连接的 directedSink</li>
     * </ul>
     * 两条通道互斥，无重复投递。
     * </p>
     *
     * @param message 消息
     */
    public void publish(TopicMessage message) {
        if (message == null || message.getTopic() == null) {
            return;
        }

        long startTime = System.currentTimeMillis();
        String topic = message.getTopic();
        Set<String> routingKeys = message.getRoutingKeys();

        if (routingKeys == null || routingKeys.isEmpty()) {
            publishBroadcast(message, topic, startTime);
        } else {
            publishDirected(message, topic, routingKeys, startTime);
        }
    }

    /**
     * 广播投递：通过共享 topicSink 发送到 Topic 下所有连接.
     * <p>
     * 行为与原 publish() 完全一致。
     * </p>
     */
    private void publishBroadcast(TopicMessage message, String topic, long startTime) {
        Sinks.Many<TopicMessage> sink = registry.getSink(topic);

        if (sink == null) {
            metrics.recordPublish(topic, "NO_SUBSCRIBERS");
            log.debug("[Publish] 无订阅者(广播): topic={}", topic);
            return;
        }

        Sinks.EmitResult result = sink.tryEmitNext(message);

        if (result.isFailure()) {
            metrics.recordPublish(topic, "EMIT_FAILURE");
            SseConnectionFluxBuilder.firePublishError(topic, result, listeners);
            log.warn("[Publish] 广播失败: topic={}, result={}", topic, result);
        } else {
            metrics.recordPublish(topic, "SUCCESS");
            registry.updateTopicActivity(topic, System.currentTimeMillis());
        }

        long elapsed = System.currentTimeMillis() - startTime;
        metrics.recordPublishDuration(topic, elapsed);
    }

    /**
     * 定向投递：通过反向索引查找目标连接，逐个投递到 directedSink.
     * <p>
     * 复杂度 O(K)，K 为目标连接数。远优于广播+过滤的 O(N)。
     * </p>
     */
    private void publishDirected(TopicMessage message, String topic,
                                  Set<String> routingKeys, long startTime) {
        int targetCount = 0;
        int successCount = 0;
        int failCount = 0;

        for (String routingKey : routingKeys) {
            Set<String> connIds = registry.getConnectionIdsByRoutingKey(topic, routingKey);
            for (String connId : connIds) {
                targetCount++;
                Sinks.Many<TopicMessage> directedSink = registry.getDirectedSink(connId);
                if (directedSink == null) {
                    failCount++;
                    continue;
                }
                Sinks.EmitResult result = directedSink.tryEmitNext(message);
                if (result.isFailure()) {
                    failCount++;
                    log.debug("[Publish] 定向投递失败: topic={}, connectionId={}, result={}",
                            topic, connId, result);
                } else {
                    successCount++;
                }
            }
        }

        // 记录指标
        String result;
        if (targetCount == 0) {
            result = "NO_TARGETS";
        } else if (failCount > 0) {
            result = "PARTIAL_FAILURE";
        } else {
            result = "SUCCESS";
        }
        metrics.recordDirectedPublish(topic, result);

        if (targetCount == 0) {
            log.debug("[Publish] 无定向目标: topic={}, routingKeys={}", topic, routingKeys);
        } else if (failCount > 0) {
            log.warn("[Publish] 定向投递部分失败: topic={}, success={}, fail={}",
                    topic, successCount, failCount);
        }

        if (successCount > 0) {
            registry.updateTopicActivity(topic, System.currentTimeMillis());
        }

        long elapsed = System.currentTimeMillis() - startTime;
        metrics.recordDirectedPublishDuration(topic, elapsed);
    }

    /**
     * 获取 Topic 连接数.
     */
    public int getConnectionCount(String topic) {
        return registry.getCounter().getTopicCount(topic);
    }

    /**
     * 获取总连接数.
     */
    public int getTotalConnections() {
        return (int) registry.getCounter().getTotalConnections().get();
    }

    /**
     * 获取所有 Topic.
     */
    public Set<String> getAllTopics() {
        return registry.getAllTopics();
    }

    /**
     * 检查 Topic 是否有连接.
     */
    public boolean hasConnections(String topic) {
        return registry.getCounter().getTopicCount(topic) > 0;
    }

    /**
     * 强制关闭指定连接.
     */
    public boolean forceClose(String connectionId) {
        SseConnection conn = registry.getConnection(connectionId);
        if (conn != null && conn.markClosed()) {
            try {
                registry.cleanupConnection(conn, metrics);
            } finally {
                // 确保即使 cleanupConnection 抛出异常，订阅也被取消、状态也被置为 CLOSED
                conn.cancel();
                conn.completeClose();
            }
            log.info("[ForceClose] 完成: connectionId={}", connectionId);
            return true;
        }
        return false;
    }

    /**
     * 获取指定 Topic 的连接详情.
     */
    public List<SseConnection> getConnectionDetails(String topic) {
        return registry.getConnectionsByTopic(topic);
    }

    /**
     * 检查是否正在接受新连接.
     */
    public boolean isAccepting() {
        return accepting.get();
    }

    // ==================== SmartLifecycle 实现 ====================

    @Override
    public void start() {
        if (running) {
            return;
        }
        running = true;
        closed = false;
        accepting.set(true);

        // 取消旧的 drain 任务（如果有）
        CompletableFuture<Void> oldTask = currentDrainTask;
        if (oldTask != null && !oldTask.isDone()) {
            oldTask.cancel(false);
        }

        // 关闭旧调度器
        ScheduledExecutorService old = lifecycleScheduler;
        lifecycleScheduler = SseExecutorFactory.createSingleThreadScheduler("sse-lifecycle");
        if (old != null) {
            old.shutdownNow();
        }

        reaper.start();
        log.info("[Lifecycle] 启动完成");
    }

    @Override
    public void stop() {
        stop(() -> {
        });
    }

    @Override
    public void stop(Runnable callback) {
        if (!running) {
            callback.run();
            return;
        }

        log.info("[Lifecycle] 开始优雅关闭...");
        running = false;

        // 阶段 1: 停止接受新连接
        if (properties.getShutdown().isRejectNew()) {
            accepting.set(false);
            log.debug("[Lifecycle] 已拒绝新连接");
        }

        // 阶段 2: 转换所有连接为 DRAINING
        int drainingCount = drainAllConnections();
        log.debug("[Lifecycle] 转换连接为 DRAINING: count={}", drainingCount);

        // 阶段 3: 向所有 Sink 发送完成信号（广播 + 定向双通道）
        registry.getAllSinks().forEach(Sinks.Many::tryEmitComplete);
        registry.getAllDirectedSinks().forEach(Sinks.Many::tryEmitComplete);
        log.debug("[Lifecycle] 已发送 Sink 完成信号");

        // 阶段 4-6: 异步执行
        ScheduledExecutorService scheduler = this.lifecycleScheduler;

        if (scheduler == null) {
            log.warn("[Lifecycle] scheduler 为 null，跳过异步 drain");
            callback.run();
            return;
        }

        // 创建异步 drain 任务
        currentDrainTask = CompletableFuture.runAsync(() -> {
            try {
                executeShutdownPhases(scheduler, callback);
            } catch (Exception e) {
                log.error("[Lifecycle] 异步关闭异常", e);
                callback.run();
            }
        }, scheduler);
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public int getPhase() {
        return Integer.MAX_VALUE - 100;
    }

    @Override
    public boolean isAutoStartup() {
        return true;
    }

    // ==================== 私有方法 ====================

    /**
     * 检查是否接受新连接.
     */
    private boolean checkAccepting(String topic) {
        if (closed || !accepting.get()) {
            metrics.recordSubscribe(topic, "REJECTED_SHUTDOWN");
            return false;
        }
        return true;
    }

    /**
     * 记录拒绝原因.
     */
    private void recordRejection(String topic, SseConnectionLimitExceededException ex) {
        String reason = ex.getScope() == SseConnectionLimitExceededException.Scope.GLOBAL
                ? "REJECTED_GLOBAL" : "REJECTED_TOPIC";
        metrics.recordSubscribe(topic, reason);
        log.info("[Subscribe] 拒绝({}): topic={}, current={}, max={}",
                reason, topic, ex.getCurrentCount(), ex.getMaxAllowed());
    }

    /**
     * 排空所有连接.
     */
    private int drainAllConnections() {
        int[] count = {0};
        registry.forEachConnection(conn -> {
            if (conn.tryDrain()) {
                count[0]++;
            }
        });
        return count[0];
    }

    /**
     * 执行关闭阶段.
     */
    private void executeShutdownPhases(ScheduledExecutorService scheduler, Runnable callback) {
        try {
            // 阶段 4: 等待排空超时
            Duration drainTimeout = properties.getShutdown().getDrainTimeout();
            try {
                Thread.sleep(drainTimeout.toMillis());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.debug("[Lifecycle] 排空等待被中断");
                return; // 被中断，跳过后续阶段
            }

            // 检查任务是否被取消（组件已重启）
            if (Thread.currentThread().isInterrupted()) {
                log.debug("[Lifecycle] 检测到任务取消，跳过旧 drain");
                return;
            }

            // 阶段 5: 强制关闭剩余连接
            int forceClosed = forceCloseAllConnections();
            if (forceClosed > 0) {
                log.debug("[Lifecycle] 强制关闭剩余连接: count={}", forceClosed);
            }

            // 阶段 6: 清理所有资源
            closed = true;
            shutdownComponents();

            log.info("[Lifecycle] 关闭完成");
        } finally {
            scheduler.shutdown();
            if (lifecycleScheduler == scheduler) {
                lifecycleScheduler = null;
            }
            callback.run();
        }
    }

    /**
     * 强制关闭所有连接.
     */
    private int forceCloseAllConnections() {
        int[] count = {0};
        registry.forEachConnection(conn -> {
            try {
                if (conn.markClosed()) {
                    try {
                        registry.cleanupConnection(conn, metrics);
                    } finally {
                        conn.cancel();
                        conn.completeClose();
                    }
                    count[0]++;
                }
            } catch (Exception e) {
                log.error("[Lifecycle] 强制关闭失败: connectionId={}", conn.getConnectionId(), e);
            }
        });
        return count[0];
    }

    /**
     * 关闭组件.
     */
    private void shutdownComponents() {
        try {
            registry.closeAll();
        } catch (Exception e) {
            log.error("[Lifecycle] Registry 关闭失败", e);
        }
        try {
            reaper.stop();
        } catch (Exception e) {
            log.error("[Lifecycle] Reaper 停止失败", e);
        }
        try {
            metrics.shutdownCleanup();
        } catch (Exception e) {
            log.error("[Lifecycle] Metrics 清理失败", e);
        }
    }
}
