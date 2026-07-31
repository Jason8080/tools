package cn.gmlee.tools.im.sse.cleanup;

import cn.gmlee.tools.im.conf.SseProperties;
import cn.gmlee.tools.im.sse.SseConnection;
import cn.gmlee.tools.im.sse.SseConnectionRegistry;
import cn.gmlee.tools.im.sse.internal.SseExecutorFactory;
import cn.gmlee.tools.im.sse.metrics.SseMetrics;
import cn.gmlee.tools.im.topic.TopicLifecycleManager;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * SSE 连接收割器.
 * <p>
 * 定时扫描所有连接，检测并清理僵尸连接（空闲超过阈值的连接）。
 * 这是三层清理机制的第二层，作为 doFinally（第一层）的兜底。
 * </p>
 *
 * <h3>工作流程</h3>
 * <ol>
 *   <li>定期（默认 30s）扫描所有连接</li>
 *   <li>检查每个连接的 lastActivityAt 时间戳</li>
 *   <li>空闲超过 idleTimeout 的连接标记为 DRAINING</li>
 *   <li>等待 gracePeriod 后强制关闭</li>
 *   <li>清理空 Topic 和空闲 Topic（通过 {@link TopicLifecycleManager}）</li>
 * </ol>
 *
 * <h3>线程模型</h3>
 * <p>
 * 使用两个独立的线程池（通过 {@link SseExecutorFactory} 创建）：
 * <ul>
 *   <li><b>scheduler</b>（单线程）：负责定期扫描和调度延迟关闭任务</li>
 *   <li><b>forceCloseExecutor</b>（固定 2 线程）：执行实际的强制关闭操作</li>
 * </ul>
 * 分离的目的是防止大量僵尸连接同时触发关闭时阻塞扫描调度。
 * </p>
 */
@Slf4j
public class ConnectionReaper {

    /**
     * 强制关闭线程池大小
     */
    private static final int FORCE_CLOSE_THREADS = 2;

    private final SseConnectionRegistry registry;
    private final SseMetrics metrics;
    private final SseProperties properties;
    private final AtomicBoolean running = new AtomicBoolean(false);

    /**
     * Topic 生命周期管理器（可选依赖，用于清理空闲 Topic）
     */
    private volatile TopicLifecycleManager topicLifecycleManager;

    /**
     * 收割调度器
     */
    private volatile ScheduledExecutorService scheduler;

    /**
     * 强制关闭执行器
     */
    private volatile ExecutorService forceCloseExecutor;

    /**
     * 创建连接收割器.
     *
     * @param registry   连接注册表
     * @param metrics    指标收集器
     * @param properties SSE 配置
     */
    public ConnectionReaper(SseConnectionRegistry registry, SseMetrics metrics, SseProperties properties) {
        this.registry = registry;
        this.metrics = metrics;
        this.properties = properties;
    }

    /**
     * 设置 Topic 生命周期管理器.
     * <p>
     * 延迟注入，避免循环依赖。由自动配置类在初始化完成后调用。
     * </p>
     *
     * @param topicLifecycleManager Topic 生命周期管理器
     */
    public void setTopicLifecycleManager(TopicLifecycleManager topicLifecycleManager) {
        this.topicLifecycleManager = topicLifecycleManager;
    }

    /**
     * 启动收割器.
     */
    public void start() {
        if (!properties.getReaper().isEnabled()) {
            log.info("[Reaper] 已禁用");
            return;
        }
        if (!running.compareAndSet(false, true)) {
            log.warn("[Reaper] 已在运行");
            return;
        }

        scheduler = SseExecutorFactory.createSingleThreadScheduler("sse-reaper-scheduler");
        forceCloseExecutor = SseExecutorFactory.createFixedThreadPool(FORCE_CLOSE_THREADS, "sse-reaper-force");

        long intervalMs = properties.getReaper().getInterval().toMillis();
        scheduler.scheduleAtFixedRate(this::scan, intervalMs, intervalMs, TimeUnit.MILLISECONDS);
        log.info("[Reaper] 启动完成: intervalMs={}", intervalMs);
    }

    /**
     * 停止收割器.
     */
    public void stop() {
        if (!running.compareAndSet(true, false)) {
            return;
        }

        // 停止调度器
        ScheduledExecutorService s = this.scheduler;
        this.scheduler = null;
        if (s != null) {
            s.shutdown();
            try {
                if (!s.awaitTermination(5, TimeUnit.SECONDS)) {
                    s.shutdownNow();
                }
            } catch (InterruptedException e) {
                s.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        // 停止强制关闭执行器
        ExecutorService exec = this.forceCloseExecutor;
        this.forceCloseExecutor = null;
        if (exec != null) {
            exec.shutdown();
            try {
                if (!exec.awaitTermination(5, TimeUnit.SECONDS)) {
                    exec.shutdownNow();
                }
            } catch (InterruptedException e) {
                exec.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        log.info("[Reaper] 已停止");
    }

    /**
     * 执行一次扫描.
     */
    public void scan() {
        try {
            metrics.recordReaperScan();

            long idleTimeoutMs = properties.getReaper().getIdleTimeout().toMillis();
            long gracePeriodMs = properties.getReaper().getGracePeriod().toMillis();

            // 捕获本地引用，避免 stop() 并发置 null 后 NPE
            ScheduledExecutorService s = this.scheduler;
            ExecutorService exec = this.forceCloseExecutor;
            if (s == null || s.isShutdown() || exec == null || exec.isShutdown()) {
                return;
            }

            // 获取所有连接快照
            List<SseConnection> connections = registry.snapshotConnections();
            int zombieCount = 0;

            for (SseConnection conn : connections) {
                if (conn.isIdle(idleTimeoutMs)) {
                    if (conn.tryDrain()) {
                        long idleMs = System.currentTimeMillis() - conn.getLastActivityAt().get();
                        log.debug("[Reaper] 检测到僵尸连接: topic={}, connectionId={}, idleMs={}",
                                conn.getTopic(), conn.getConnectionId(), idleMs);

                        // 调度延迟后提交到强制关闭执行器
                        s.schedule(() -> exec.submit(() -> forceClose(conn)),
                                gracePeriodMs, TimeUnit.MILLISECONDS);
                        zombieCount++;
                    }
                }
            }

            // 清理空 Topic（基于 TTL，仅遍历当前空 Topic 集合）
            List<String> cleanedTopics = registry.cleanupEmptyTopicsByTtl();

            // 清理已销毁 Topic 的指标对象
            for (String topic : cleanedTopics) {
                metrics.cleanupTopic(topic);
            }

            // 压缩长期为空的 Topic 计数器条目
            long compactTtlMs = properties.getCleanup().getCompactTtl().toMillis();
            int compacted = registry.compactTopicCounts(compactTtlMs);

            // 清理空闲 Topic（通过 TopicLifecycleManager）
            int topicCleaned = cleanupIdleTopics();

            if (zombieCount > 0 || !cleanedTopics.isEmpty() || compacted > 0 || topicCleaned > 0) {
                metrics.recordZombieReaped(zombieCount);
                log.info("[Reaper] 扫描完成: zombies={}, cleanedTopics={}, compacted={}, topicsCleaned={}",
                        zombieCount, cleanedTopics.size(), compacted, topicCleaned);
            }
        } catch (Exception e) {
            log.error("[Reaper] 扫描异常", e);
            metrics.recordError("reaper_scan");
        }
    }

    /**
     * 清理空闲 Topic（引用计数为 0 且超过 TTL）.
     *
     * @return 被清理的 Topic 数量
     */
    private int cleanupIdleTopics() {
        TopicLifecycleManager manager = this.topicLifecycleManager;
        if (manager == null) {
            return 0;
        }

        try {
            return manager.cleanup();
        } catch (Exception e) {
            log.error("[Reaper] Topic 清理异常", e);
            return 0;
        }
    }

    /**
     * 强制关闭连接.
     */
    private void forceClose(SseConnection conn) {
        if (conn.markClosed()) {
            log.debug("[Reaper] 强制关闭僵尸连接: topic={}, connectionId={}",
                    conn.getTopic(), conn.getConnectionId());
            try {
                registry.cleanupConnection(conn, metrics);
            } finally {
                // 确保即使 cleanupConnection 抛出异常，订阅也被取消
                conn.cancel();
                conn.completeClose();
            }
        }
    }

    /**
     * 检查是否正在运行.
     */
    public boolean isRunning() {
        return running.get();
    }
}
