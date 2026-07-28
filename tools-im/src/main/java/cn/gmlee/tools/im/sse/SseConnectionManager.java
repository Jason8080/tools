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
     * 是否正在接受新连接
     */
    private final AtomicBoolean accepting = new AtomicBoolean(true);

    /**
     * SmartLifecycle 运行状态
     */
    @Getter
    private volatile boolean running = false;

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
     * @param topic Topic 名称
     * @return 消息流
     */
    public Flux<TopicMessage<Msg>> subscribe(String topic) {
        long startTime = System.currentTimeMillis();

        // 0. 检查是否正在接受连接
        if (!accepting.get()) {
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

        // 3. 获取或创建 Sink（原子操作）
        Sinks.Many<TopicMessage<Msg>> sink = registry.getOrCreateSink(topic);

        // 4. 创建连接记录
        SseConnection conn = new SseConnection(topic);
        registry.register(conn);
        metrics.recordSubscribe(topic, "SUCCESS");

        long elapsed = System.currentTimeMillis() - startTime;
        metrics.recordSubscribeDuration(topic, elapsed);

        log.debug("SSE 连接已建立: topic={}, connectionId={}", topic, conn.getConnectionId());

        // 5. 构建返回 Flux，附加生命周期钩子
        return sink.asFlux()
                // 首次订阅时转换为 ACTIVE
                .doOnSubscribe(sub -> {
                    conn.transition(ConnectionState.CREATED, ConnectionState.ACTIVE);
                    conn.touch();
                })
                // 每次接收数据更新活跃时间
                .doOnNext(msg -> conn.touch())
                // 终止时清理（仅执行一次）
                .doFinally(signal -> {
                    if (conn.markClosed()) {
                        // 标记成功，执行清理
                        topicCount.decrementAndGet();
                        registry.getTotalConnections().decrementAndGet();
                        registry.unregister(conn);
                        registry.cleanupIfEmpty(topic);
                        conn.transition(conn.getState().get(), ConnectionState.CLOSED);
                        log.debug("SSE 连接已关闭: topic={}, connectionId={}, signal={}",
                                topic, conn.getConnectionId(), signal);
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
     *
     * @param connectionId 连接 ID
     * @return 成功关闭返回 true
     */
    public boolean forceClose(String connectionId) {
        SseConnection conn = registry.getConnection(connectionId);
        if (conn != null && conn.markClosed()) {
            registry.unregister(conn);
            registry.cleanupIfEmpty(conn.getTopic());
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

    // ============ SmartLifecycle 实现 ============

    @Override
    public void start() {
        if (running) {
            return;
        }
        running = true;
        accepting.set(true);
        reaper.start();
        log.info("SSE 连接管理器已启动");
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

        // 阶段 1: 停止接受新连接
        if (properties.getShutdown().isRejectNew()) {
            accepting.set(false);
            log.debug("已拒绝新连接");
        }

        // 阶段 2: 转换所有连接为 DRAINING
        int drainingCount = 0;
        for (SseConnection conn : registry.snapshotConnections()) {
            if (conn.transition(ConnectionState.ACTIVE, ConnectionState.DRAINING) ||
                    conn.transition(ConnectionState.CREATED, ConnectionState.DRAINING)) {
                drainingCount++;
            }
        }
        log.debug("已将 {} 个连接转换为 DRAINING 状态", drainingCount);

        // 阶段 3: 向所有 Sink 发送完成信号
        registry.getAllSinks().forEach(Sinks.Many::tryEmitComplete);
        log.debug("已向所有 Sink 发送完成信号");

        // 阶段 4: 等待排空超时
        Duration drainTimeout = properties.getShutdown().getDrainTimeout();
        try {
            Thread.sleep(Math.min(drainTimeout.toMillis(), 1000));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // 阶段 5: 强制关闭剩余连接
        int forceClosed = 0;
        for (SseConnection conn : registry.snapshotConnections()) {
            if (conn.markClosed()) {
                forceClosed++;
            }
        }
        if (forceClosed > 0) {
            log.debug("强制关闭 {} 个剩余连接", forceClosed);
        }

        // 阶段 6: 清理所有资源
        registry.closeAll();
        reaper.stop();

        running = false;
        log.info("SSE 连接管理器已关闭");

        callback.run();
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
