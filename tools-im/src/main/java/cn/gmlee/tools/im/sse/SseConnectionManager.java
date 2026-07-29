package cn.gmlee.tools.im.sse;

import cn.gmlee.tools.im.conf.SseProperties;
import cn.gmlee.tools.im.core.Msg;
import cn.gmlee.tools.im.core.TopicMessage;
import cn.gmlee.tools.im.ex.SseConnectionLimitExceededException;
import cn.gmlee.tools.im.ex.SseShutdownException;
import cn.gmlee.tools.im.sse.backpressure.BackpressureStrategyResolver;
import cn.gmlee.tools.im.sse.cleanup.ConnectionReaper;
import cn.gmlee.tools.im.sse.internal.ConnectionCounter;
import cn.gmlee.tools.im.sse.internal.SseExecutorFactory;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.SmartLifecycle;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * SSE 连接管理器（门面）.
 * <p>
 * 这是 SSE 连接管理的唯一公共入口，保持与旧版本相同的 API 签名以兼容下游代码。
 * 内部委托给 {@link SseConnectionRegistry}、{@link ConnectionReaper}、{@link SseMetrics} 等组件。
 * </p>
 *
 * <h3>核心特性</h3>
 * <ul>
 *   <li><b>无锁并发</b>：使用 CAS 循环保证连接数限制的原子性</li>
 *   <li><b>三层清理</b>：doFinally（响应式） + Reaper（定时） + Shutdown（强制）</li>
 *   <li><b>优雅关闭</b>：实现 SmartLifecycle，按阶段排空连接</li>
 *   <li><b>可观测性</b>：通过 SseMetrics 暴露 Micrometer 指标</li>
 * </ul>
 */
@Slf4j
public class SseConnectionManager implements SmartLifecycle {

    private final SseProperties properties;
    private final SseConnectionRegistry registry;
    private final BackpressureStrategyResolver strategyResolver;
    private final SseMetrics metrics;
    private final ConnectionReaper reaper;

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
     * 生命周期代数计数器
     */
    private final AtomicInteger drainGeneration = new AtomicInteger(0);

    public SseConnectionManager(SseProperties properties,
                                 SseConnectionRegistry registry,
                                 BackpressureStrategyResolver strategyResolver,
                                 SseMetrics metrics,
                                 ConnectionReaper reaper) {
        this.properties = properties;
        this.registry = registry;
        this.strategyResolver = strategyResolver;
        this.metrics = metrics;
        this.reaper = reaper;
    }

    // ==================== 公共 API ====================

    /**
     * 订阅 Topic.
     *
     * @param topic Topic 名称
     * @return 消息流
     */
    public Flux<TopicMessage<Msg>> subscribe(String topic) {
        return Flux.defer(() -> {
            long startTime = System.currentTimeMillis();

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

            // 4. 构建连接流
            return buildConnectionFlux(topic, acquireResult.getTopicCount(), startTime);
        });
    }

    /**
     * 发布消息到 Topic.
     *
     * @param message 消息
     */
    public void publish(TopicMessage<Msg> message) {
        if (message == null || message.getTopic() == null) {
            return;
        }

        long startTime = System.currentTimeMillis();
        String topic = message.getTopic();

        Sinks.Many<TopicMessage<Msg>> sink = registry.getSink(topic);

        if (sink == null) {
            metrics.recordPublish(topic, "NO_SUBSCRIBERS");
            log.debug("[Publish] 无订阅者: topic={}", topic);
            return;
        }

        Sinks.EmitResult result = sink.tryEmitNext(message);

        if (result.isFailure()) {
            metrics.recordPublish(topic, "EMIT_FAILURE");
            log.warn("[Publish] 失败: topic={}, result={}", topic, result);
        } else {
            metrics.recordPublish(topic, "SUCCESS");
            registry.updateTopicActivity(topic, System.currentTimeMillis());
        }

        long elapsed = System.currentTimeMillis() - startTime;
        metrics.recordPublishDuration(topic, elapsed);
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
            registry.cleanupConnection(conn, metrics);
            conn.cancel();
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
        drainGeneration.incrementAndGet();

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
        stop(() -> {});
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

        // 阶段 3: 向所有 Sink 发送完成信号
        registry.getAllSinks().forEach(Sinks.Many::tryEmitComplete);
        log.debug("[Lifecycle] 已发送 Sink 完成信号");

        // 阶段 4-6: 异步执行
        Duration drainTimeout = properties.getShutdown().getDrainTimeout();
        ScheduledExecutorService scheduler = this.lifecycleScheduler;
        int expectedGen = drainGeneration.get();

        if (scheduler == null) {
            log.warn("[Lifecycle] scheduler 为 null，跳过异步 drain");
            callback.run();
            return;
        }

        scheduler.schedule(() -> executeShutdownPhases(scheduler, expectedGen, callback),
                0, TimeUnit.MILLISECONDS);
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
     * 构建连接 Flux.
     */
    private Flux<TopicMessage<Msg>> buildConnectionFlux(String topic,
                                                         java.util.concurrent.atomic.AtomicInteger topicCount,
                                                         long startTime) {
        SseConnection conn = null;
        try {
            // 获取或创建 Sink
            Sinks.Many<TopicMessage<Msg>> sink = registry.getOrCreateSink(topic);
            if (sink == null) {
                registry.getCounter().rollback(topic);
                metrics.recordSubscribe(topic, "REJECTED_SHUTDOWN");
                return Flux.error(SseShutdownException.INSTANCE);
            }

            // 创建连接记录
            conn = new SseConnection(topic);
            registry.register(conn);
            metrics.recordSubscribe(topic, "SUCCESS");

            long elapsed = System.currentTimeMillis() - startTime;
            metrics.recordSubscribeDuration(topic, elapsed);

            log.info("[Subscribe] 成功: topic={}, connectionId={}", topic, conn.getConnectionId());

            // 构建数据流
            return buildReactorFlux(sink, conn, topic, topicCount);

        } catch (Exception e) {
            // 异常回滚
            registry.getCounter().rollback(topic);
            if (conn != null) {
                registry.unregister(conn);
                if (registry.cleanupIfEmpty(topic)) {
                    metrics.cleanupTopic(topic);
                }
            }
            metrics.recordError("subscribe_init");
            log.error("[Subscribe] 初始化失败: topic={}", topic, e);
            return Flux.error(e);
        }
    }

    /**
     * 构建 Reactor Flux 并附加生命周期钩子.
     */
    private Flux<TopicMessage<Msg>> buildReactorFlux(Sinks.Many<TopicMessage<Msg>> sink,
                                                      SseConnection conn,
                                                      String topic,
                                                      java.util.concurrent.atomic.AtomicInteger topicCount) {
        return sink.asFlux()
                .doOnSubscribe(sub -> handleOnSubscribe(conn, sub))
                .doOnNext(msg -> conn.touch())
                .doFinally(signal -> handleFinally(conn, topic, topicCount, signal));
    }

    /**
     * 处理 onSubscribe 回调.
     */
    private void handleOnSubscribe(SseConnection conn, org.reactivestreams.Subscription sub) {
        conn.setSubscription(sub);
        // 补偿检查：如果在 doOnSubscribe 前已被关闭
        if (conn.isClosed()) {
            sub.cancel();
            return;
        }
        conn.activate();
        conn.touch();
    }

    /**
     * 处理 finally 回调.
     */
    private void handleFinally(SseConnection conn, String topic,
                                java.util.concurrent.atomic.AtomicInteger topicCount,
                                reactor.core.publisher.SignalType signal) {
        if (conn.markClosed()) {
            // doFinally 赢得清理权
            registry.cleanupConnection(conn, metrics);
            conn.completeClose();
            log.debug("[Cleanup] 完成: topic={}, connectionId={}, signal={}",
                    topic, conn.getConnectionId(), signal);
        } else {
            // 其他层已清理，检查是否需要补偿递减
            if (registry.isRegistered(conn)) {
                registry.cleanupConnection(conn, metrics);
            }
        }
    }

    /**
     * 排空所有连接.
     */
    private int drainAllConnections() {
        int count = 0;
        for (SseConnection conn : registry.snapshotConnections()) {
            if (conn.tryDrain()) {
                count++;
            }
        }
        return count;
    }

    /**
     * 执行关闭阶段.
     */
    private void executeShutdownPhases(ScheduledExecutorService scheduler, int expectedGen, Runnable callback) {
        try {
            // 阶段 4: 等待排空超时
            Duration drainTimeout = properties.getShutdown().getDrainTimeout();
            try {
                Thread.sleep(drainTimeout.toMillis());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.debug("[Lifecycle] 排空等待被中断");
            }

            // 检查是否已被重启
            if (drainGeneration.get() != expectedGen) {
                log.debug("[Lifecycle] 检测到组件重启，跳过旧 drain");
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
        int count = 0;
        for (SseConnection conn : registry.snapshotConnections()) {
            try {
                if (conn.markClosed()) {
                    registry.cleanupConnection(conn, metrics);
                    conn.cancel();
                    count++;
                }
            } catch (Exception e) {
                log.error("[Lifecycle] 强制关闭失败: connectionId={}", conn.getConnectionId(), e);
            }
        }
        return count;
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
