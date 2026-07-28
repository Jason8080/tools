package cn.gmlee.tools.im.sse;

import cn.gmlee.tools.im.conf.SseProperties;
import cn.gmlee.tools.im.core.Msg;
import cn.gmlee.tools.im.core.TopicMessage;
import cn.gmlee.tools.im.ex.SseConnectionLimitExceededException;
import cn.gmlee.tools.im.ex.SseConnectionLimitExceededException.Scope;
import cn.gmlee.tools.im.ex.SseShutdownException;
import cn.gmlee.tools.im.sse.backpressure.BackpressureStrategyResolver;
import cn.gmlee.tools.im.sse.cleanup.ConnectionReaper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.SmartLifecycle;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
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
 *   <li><b>心跳支持</b>：定期发送 SSE 注释保持连接活性</li>
 *   <li><b>优雅关闭</b>：实现 SmartLifecycle，按阶段排空连接</li>
 *   <li><b>可观测性</b>：通过 SseMetrics 暴露 Micrometer 指标</li>
 * </ul>
 *
 * <h3>并发安全</h3>
 * <p>
 * 所有计数器操作使用 CAS（compareAndSet），避免锁竞争：
 * <ul>
 *   <li>subscribe() 使用 CAS 循环原子检查并递增连接数</li>
 *   <li>cleanup 使用 AtomicBoolean 确保仅执行一次</li>
 *   <li>Sink 清理使用 ConcurrentHashMap.compute() 原子操作</li>
 * </ul>
 * </p>
 */
@Slf4j
public class SseConnectionManager implements SmartLifecycle {

    private final SseProperties properties;
    private final SseConnectionRegistry registry;
    private final BackpressureStrategyResolver strategyResolver;
    private final SseMetrics metrics;
    private final ConnectionReaper reaper;

    /**
     * 生命周期调度器（daemon 线程）.
     * <p>
     * 用于异步执行优雅关闭的等待和强制清理阶段，
     * 避免阻塞 Spring lifecycle 线程。
     * 使用 volatile 支持重启时重建。
     * </p>
     */
    private volatile ScheduledExecutorService lifecycleScheduler;

    /**
     * 是否正在接受新连接
     */
    private final AtomicBoolean accepting = new AtomicBoolean(true);

    /**
     * 是否已完成关闭（closeAll 已执行）.
     * <p>
     * 与 {@link #running} 的区别：
     * <ul>
     *   <li>{@code running} 在 stop() 开始时即设为 false，但 closeAll 在异步 drain 任务中执行</li>
     *   <li>{@code closed} 在 closeAll() 执行时设为 true，确保关闭后不再有新连接创建</li>
     *   <li>start() 时重置为 false</li>
     * </ul>
     * </p>
     */
    private volatile boolean closed = false;

    /**
     * SmartLifecycle 运行状态
     */
    @Getter
    private volatile boolean running = false;

    /**
     * 生命周期代数计数器.
     * <p>
     * 每次 start() 递增。异步 drain 任务通过比较代数来判断自身是否已过期
     * （组件已被重启），过期任务跳过破坏性清理（如 closeAll），
     * 避免影响新生命周期的状态。
     * </p>
     */
    private final AtomicInteger drainGeneration = new AtomicInteger(0);

    /**
     * 创建 SSE 连接管理器.
     *
     * @param properties       配置
     * @param registry         连接注册表
     * @param strategyResolver 背压策略解析器
     * @param metrics          指标收集器
     * @param reaper           连接收割器
     */
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

    /**
     * 订阅 Topic.
     * <p>
     * 创建 SSE 连接并返回消息流。客户端通过此流接收消息。
     * </p>
     *
     * <h4>并发安全</h4>
     * <p>
     * 使用 CAS 循环原子检查并递增连接数，避免竞态条件。
     * 如果超过限制，返回 Flux.error() 而非抛异常（兼容响应式 API）。
     * </p>
     *
     * <h4>延迟执行</h4>
     * <p>
     * 使用 {@link Flux#defer} 将计数器递增、Sink 创建、连接注册延迟到实际订阅时执行。
     * 避免返回的 Flux 未被订阅时导致计数器泄漏（{@code doFinally} 仅在实际订阅后才会触发）。
     * </p>
     *
     * @param topic Topic 名称
     * @return 消息流
     */
    public Flux<TopicMessage<Msg>> subscribe(String topic) {
        return Flux.defer(() -> {
            long startTime = System.currentTimeMillis();

            // 0. 检查是否正在接受连接
            if (closed || !accepting.get()) {
                metrics.recordSubscribe(topic, "REJECTED_SHUTDOWN");
                return Flux.error(SseShutdownException.INSTANCE);
            }

            // 1. CAS 循环：原子检查并递增全局连接数
            long maxTotal = properties.getMaxTotalConnections();
            long currentTotal;
            do {
                currentTotal = registry.getTotalConnections().get();
                if (currentTotal >= maxTotal) {
                    metrics.recordSubscribe(topic, "REJECTED_GLOBAL");
                    return Flux.error(new SseConnectionLimitExceededException(
                            topic, (int) currentTotal, (int) maxTotal, Scope.GLOBAL));
                }
            } while (!registry.getTotalConnections().compareAndSet(currentTotal, currentTotal + 1));

            // 2. CAS 循环：原子检查并递增 Topic 连接数
            int maxPerTopic = properties.getMaxConnectionsPerTopic();
            AtomicInteger topicCount = registry.getOrCreateTopicCount(topic);
            int currentTopic;
            do {
                currentTopic = topicCount.get();
                if (currentTopic >= maxPerTopic) {
                    // 回滚全局计数
                    registry.getTotalConnections().decrementAndGet();
                    metrics.recordSubscribe(topic, "REJECTED_TOPIC");
                    return Flux.error(new SseConnectionLimitExceededException(
                            topic, currentTopic, maxPerTopic, Scope.PER_TOPIC));
                }
            } while (!topicCount.compareAndSet(currentTopic, currentTopic + 1));

            // 1.5 二次检查关闭状态（防止 CAS 期间进入关闭流程）
            if (closed || !accepting.get()) {
                topicCount.decrementAndGet();
                registry.getTotalConnections().decrementAndGet();
                metrics.recordSubscribe(topic, "REJECTED_SHUTDOWN");
                return Flux.error(SseShutdownException.INSTANCE);
            }

            // 3-5. 获取 Sink、创建连接、构建数据流
            // 异常时回滚计数器，防止 doFinally 未注册导致计数器泄漏
            try {
                // 3. 获取或创建 Sink（原子操作）
                Sinks.Many<TopicMessage<Msg>> sink = registry.getOrCreateSink(topic);

                // 3.5 Sink 创建失败（Registry 正在关闭）
                if (sink == null) {
                    topicCount.decrementAndGet();
                    registry.getTotalConnections().decrementAndGet();
                    metrics.recordSubscribe(topic, "REJECTED_SHUTDOWN");
                    return Flux.error(SseShutdownException.INSTANCE);
                }

                // 4. 创建连接记录
                SseConnection conn = new SseConnection(topic);
                registry.register(conn);
                metrics.recordSubscribe(topic, "SUCCESS");

                long elapsed = System.currentTimeMillis() - startTime;
                metrics.recordSubscribeDuration(topic, elapsed);

                log.debug("SSE 连接已建立: topic={}, connectionId={}", topic, conn.getConnectionId());

                // 5. 构建数据流，附加生命周期钩子

                return sink.asFlux()
                        // 首次订阅时保存 Subscription 引用并转换为 ACTIVE
                        .doOnSubscribe(sub -> {
                            conn.setSubscription(sub);
                            // 检查是否在 doOnSubscribe 之前已被 Reaper/forceClose 标记关闭：
                            // 此时 cancel() 因 subscription 为 null 而丢失，需在此处补偿取消
                            if (conn.isClosed()) {
                                sub.cancel();
                                return;
                            }
                            conn.transition(ConnectionState.CREATED, ConnectionState.ACTIVE);
                            conn.touch();
                        })
                        // 每次接收数据更新活跃时间
                        .doOnNext(msg -> conn.touch())
                        // 终止时清理（仅执行一次）
                        .doFinally(signal -> {
                            if (conn.markClosed()) {
                                // doFinally 赢得清理权：递减计数器并移除连接
                                if (conn.getCountersDecrementGuard().compareAndSet(false, true)) {
                                    topicCount.decrementAndGet();
                                    registry.getTotalConnections().decrementAndGet();
                                    registry.unregister(conn);
                                }
                                if (registry.cleanupIfEmpty(topic)) {
                                    metrics.cleanupTopic(topic);
                                }
                                conn.transition(conn.getState().get(), ConnectionState.CLOSED);
                                log.debug("SSE 连接已关闭: topic={}, connectionId={}, signal={}",
                                        topic, conn.getConnectionId(), signal);
                            } else {
                                // markClosed 返回 false：Reaper 或 Shutdown 已先清理
                                // 检查 doFinally 是否仍需要递减计数器（Reaper 未完成递减的边界情况）
                                if (registry.isRegistered(conn)
                                        && conn.getCountersDecrementGuard().compareAndSet(false, true)) {
                                    topicCount.decrementAndGet();
                                    registry.getTotalConnections().decrementAndGet();
                                    registry.unregister(conn);
                                    if (registry.cleanupIfEmpty(topic)) {
                                        metrics.cleanupTopic(topic);
                                    }
                                }
                            }
                        });
            } catch (Exception e) {
                // 异常路径：doFinally 尚未注册，必须手动回滚计数器
                topicCount.decrementAndGet();
                registry.getTotalConnections().decrementAndGet();
                metrics.recordError("subscribe_init");
                log.error("SSE 订阅初始化失败: topic={}", topic, e);
                return Flux.error(e);
            }
        });
    }

    /**
     * 发布消息到 Topic.
     * <p>
     * 将消息发送到指定 Topic 的所有活跃订阅者。
     * </p>
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
            // 无订阅者
            metrics.recordPublish(topic, "NO_SUBSCRIBERS");
            log.debug("发布消息时无订阅者: topic={}", topic);
            return;
        }

        Sinks.EmitResult result = sink.tryEmitNext(message);

        if (result.isFailure()) {
            metrics.recordPublish(topic, "EMIT_FAILURE");
            log.warn("发布消息到 Topic [{}] 失败: {}", topic, result);
        } else {
            metrics.recordPublish(topic, "SUCCESS");
            // 更新该 Topic 所有连接的活跃时间
            registry.updateTopicActivity(topic, System.currentTimeMillis());
        }

        long elapsed = System.currentTimeMillis() - startTime;
        metrics.recordPublishDuration(topic, elapsed);
    }

    /**
     * 获取 Topic 连接数.
     *
     * @param topic Topic 名称
     * @return 连接数
     */
    public int getConnectionCount(String topic) {
        return registry.getTopicCount(topic);
    }

    /**
     * 获取总连接数.
     *
     * @return 总连接数
     */
    public int getTotalConnections() {
        return (int) registry.getTotalConnections().get();
    }

    /**
     * 获取所有 Topic.
     *
     * @return Topic 集合
     */
    public Set<String> getAllTopics() {
        return registry.getAllTopics();
    }

    /**
     * 检查 Topic 是否有连接.
     *
     * @param topic Topic 名称
     * @return 有连接返回 true
     */
    public boolean hasConnections(String topic) {
        return registry.hasConnections(topic);
    }

    /**
     * 强制关闭指定连接.
     * <p>
     * 通过 {@link SseConnectionRegistry#forceDecrementCounters} 原子递减计数器，
     * 确保与 doFinally 之间不会双重递减。
     * </p>
     *
     * @param connectionId 连接 ID
     * @return 成功关闭返回 true
     */
    public boolean forceClose(String connectionId) {
        SseConnection conn = registry.getConnection(connectionId);
        if (conn != null && conn.markClosed()) {
            if (registry.forceDecrementCounters(conn)) {
                if (registry.cleanupIfEmpty(conn.getTopic())) {
                    metrics.cleanupTopic(conn.getTopic());
                }
            }
            // 主动取消 Flux 订阅，立即终止连接
            conn.cancel();
            log.info("强制关闭连接: {}", connectionId);
            return true;
        }
        return false;
    }

    /**
     * 获取指定 Topic 的连接详情.
     *
     * @param topic Topic 名称
     * @return 连接列表
     */
    public List<SseConnection> getConnectionDetails(String topic) {
        return registry.getConnectionsByTopic(topic);
    }

    /**
     * 检查是否正在接受新连接.
     *
     * @return 接受返回 true
     */
    public boolean isAccepting() {
        return accepting.get();
    }

    /**
     * 检查管理器是否已完成关闭.
     * <p>
     * 与 {@link #isRunning()} 的区别：{@code isRunning()} 在 stop() 开始时返回 false，
     * 而 {@code isClosed()} 在 closeAll() 执行后才返回 true。
     * </p>
     *
     * @return 已关闭返回 true
     */
    public boolean isClosed() {
        return closed;
    }

    // ============ SmartLifecycle 实现 ============

    @Override
    public void start() {
        if (running) {
            return;
        }
        running = true;
        closed = false;
        accepting.set(true);
        drainGeneration.incrementAndGet(); // 使旧的异步 drain 任务失效
        // 关闭旧调度器（若存在），防止线程泄漏
        ScheduledExecutorService old = lifecycleScheduler;
        lifecycleScheduler = createLifecycleScheduler();
        if (old != null) {
            old.shutdownNow();
        }
        reaper.start();
        log.info("SSE 连接管理器已启动");
    }

    /**
     * 创建生命周期调度器.
     *
     * @return 新的 daemon 单线程调度器
     */
    private static ScheduledExecutorService createLifecycleScheduler() {
        return Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "sse-lifecycle");
            t.setDaemon(true);
            return t;
        });
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

        log.info("SSE 连接管理器开始优雅关闭...");
        running = false; // 同步设置，防止重复调度

        // 阶段 1: 停止接受新连接（同步，立即生效）
        if (properties.getShutdown().isRejectNew()) {
            accepting.set(false);
            log.debug("已拒绝新连接");
        }

        // 阶段 2: 转换所有连接为 DRAINING（同步，O(n) CAS）
        int drainingCount = 0;
        for (SseConnection conn : registry.snapshotConnections()) {
            if (conn.transition(ConnectionState.ACTIVE, ConnectionState.DRAINING) ||
                    conn.transition(ConnectionState.CREATED, ConnectionState.DRAINING)) {
                drainingCount++;
            }
        }
        log.debug("已将 {} 个连接转换为 DRAINING 状态", drainingCount);

        // 阶段 3: 向所有 Sink 发送完成信号（同步，触发 doFinally 排空）
        registry.getAllSinks().forEach(Sinks.Many::tryEmitComplete);
        log.debug("已向所有 Sink 发送完成信号");

        // 阶段 4-6: 异步执行，不阻塞 lifecycle 线程
        Duration drainTimeout = properties.getShutdown().getDrainTimeout();
        ScheduledExecutorService scheduler = this.lifecycleScheduler;
        int expectedGen = drainGeneration.get(); // 捕获当前代数

        // 防御性检查：start() 中 running=true 与 lifecycleScheduler 赋值之间的极端竞态窗口
        if (scheduler == null) {
            log.warn("lifecycleScheduler 为 null，跳过异步 drain 任务");
            callback.run();
            return;
        }

        scheduler.schedule(() -> {
            try {
                // 阶段 4: 等待排空超时（响应中断以实现快速关闭）
                try {
                    Thread.sleep(drainTimeout.toMillis());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.debug("排空等待被中断，立即进入强制关闭阶段");
                }

                // 检查组件是否已被重启（start() 在等待期间被调用）
                if (drainGeneration.get() != expectedGen) {
                    log.debug("检测到组件重启（代数变更），跳过旧 drain 任务");
                    return; // 新生命周期的 stop() 会负责清理
                }

                // 阶段 5: 强制关闭剩余连接
                int forceClosed = 0;
                for (SseConnection conn : registry.snapshotConnections()) {
                    if (conn.markClosed()) {
                        if (registry.forceDecrementCounters(conn)) {
                            registry.cleanupIfEmpty(conn.getTopic());
                        }
                        // 主动取消 Flux 订阅，立即终止连接
                        conn.cancel();
                        forceClosed++;
                    }
                }
                if (forceClosed > 0) {
                    log.debug("强制关闭 {} 个剩余连接", forceClosed);
                }

                // 阶段 6: 清理所有资源
                closed = true; // 标记关闭完成，阻止后续 subscribe()
                registry.closeAll();
                reaper.stop();
                metrics.shutdownCleanup();

                log.info("SSE 连接管理器已关闭");
            } finally {
                // 关闭调度器线程（daemon 线程，不影响 JVM 退出）
                scheduler.shutdown();
                // 仅在调度器未被 start() 替换时置 null，避免覆盖新生命周期的调度器
                if (lifecycleScheduler == scheduler) {
                    lifecycleScheduler = null;
                }
                callback.run();
            }
        }, 0, TimeUnit.MILLISECONDS);
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public int getPhase() {
        // 在 HTTP 服务器之后停止（较晚的 phase）
        return Integer.MAX_VALUE - 100;
    }

    @Override
    public boolean isAutoStartup() {
        return true;
    }
}
