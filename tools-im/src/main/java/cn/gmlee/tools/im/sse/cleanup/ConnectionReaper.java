package cn.gmlee.tools.im.sse.cleanup;

import cn.gmlee.tools.im.sse.ConnectionState;
import cn.gmlee.tools.im.sse.SseConnection;
import cn.gmlee.tools.im.sse.SseConnectionRegistry;
import cn.gmlee.tools.im.sse.SseMetrics;
import cn.gmlee.tools.im.conf.SseProperties;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
 * </ol>
 *
 * <h3>线程模型</h3>
 * <p>
 * 使用两个独立的线程池：
 * <ul>
 *   <li><b>scheduler</b>（单线程）：负责定期扫描和调度延迟关闭任务</li>
 *   <li><b>forceCloseExecutor</b>（固定 2 线程）：执行实际的强制关闭操作</li>
 * </ul>
 * 分离的目的是防止大量僵尸连接同时触发关闭时阻塞扫描调度。
 * 100K 连接扫描约 3ms，CPU 占用可忽略（30s 间隔下约 0.01%）。
 * </p>
 */
@Slf4j
public class ConnectionReaper {

    /**
     * 强制关闭线程池大小.
     * <p>
     * 2 个线程足以处理极端场景（10K+ 僵尸连接同时关闭），
     * 因为 forceClose 本身是轻量操作（CAS + cancel）。
     * </p>
     */
    private static final int FORCE_CLOSE_THREADS = 2;

    private final SseConnectionRegistry registry;
    private final SseMetrics metrics;
    private final SseProperties properties;
    private final AtomicBoolean running = new AtomicBoolean(false);

    /**
     * 收割调度器（非 final，支持 stop/start 重建）.
     * <p>
     * 单线程，负责定期扫描和调度延迟关闭任务。
     * stop() 会销毁调度器，start() 重新创建，避免在已终止的调度器上提交任务。
     * volatile 保证 stop/start 跨线程可见性。
     * </p>
     */
    private volatile ScheduledExecutorService scheduler;

    /**
     * 强制关闭执行器（非 final，支持 stop/start 重建）.
     * <p>
     * 固定线程池，执行实际的强制关闭操作。
     * 与调度器分离，防止大量延迟关闭任务阻塞扫描调度。
     * </p>
     */
    private volatile ExecutorService forceCloseExecutor;

    /**
     * 创建连接收割器.
     *
     * @param registry   连接注册表
     * @param metrics    指标收集器
     * @param properties 配置
     */
    public ConnectionReaper(SseConnectionRegistry registry, SseMetrics metrics, SseProperties properties) {
        this.registry = registry;
        this.metrics = metrics;
        this.properties = properties;
    }

    /**
     * 启动收割器.
     * <p>
     * 按配置的间隔定期执行扫描。
     * 每次启动创建新的调度器和执行器，避免在已终止的线程池上提交任务。
     * </p>
     */
    public void start() {
        if (!properties.getReaper().isEnabled()) {
            log.info("SSE 连接收割器已禁用");
            return;
        }
        if (!running.compareAndSet(false, true)) {
            log.warn("SSE 连接收割器已在运行");
            return;
        }

        scheduler = createScheduler();
        forceCloseExecutor = createForceCloseExecutor();
        long intervalMs = properties.getReaper().getInterval().toMillis();
        scheduler.scheduleAtFixedRate(this::scan, intervalMs, intervalMs, TimeUnit.MILLISECONDS);
        log.info("SSE 连接收割器已启动，扫描间隔: {}ms", intervalMs);
    }

    /**
     * 创建收割调度器.
     *
     * @return 新的 daemon 单线程调度器
     */
    private static ScheduledExecutorService createScheduler() {
        return Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "sse-reaper-scheduler");
            t.setDaemon(true);
            return t;
        });
    }

    /**
     * 创建强制关闭执行器.
     *
     * @return 新的 daemon 固定线程池
     */
    private static ExecutorService createForceCloseExecutor() {
        return Executors.newFixedThreadPool(FORCE_CLOSE_THREADS, r -> {
            Thread t = new Thread(r, "sse-reaper-force-close");
            t.setDaemon(true);
            return t;
        });
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

        log.info("SSE 连接收割器已停止");
    }

    /**
     * 执行一次扫描.
     * <p>
     * 扫描所有连接，检测僵尸并清理。
     * </p>
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
                // 检查是否空闲超时
                if (conn.isIdle(idleTimeoutMs)) {
                    // 尝试转换为 DRAINING 状态
                    if (conn.transition(ConnectionState.ACTIVE, ConnectionState.DRAINING) ||
                            conn.transition(ConnectionState.CREATED, ConnectionState.DRAINING)) {
                        log.debug("检测到僵尸连接: {}, 空闲 {}ms", conn,
                                System.currentTimeMillis() - conn.getLastActivityAt().get());

                        // 调度延迟后提交到强制关闭执行器
                        s.schedule(() -> exec.submit(() -> forceClose(conn)),
                                gracePeriodMs, TimeUnit.MILLISECONDS);
                        zombieCount++;
                    }
                }
            }

            // 清理空 Topic（基于 TTL）
            long emptyTopicTtlMs = properties.getCleanup().getEmptyTopicTtl().toMillis();
            List<String> cleanedTopics = registry.cleanupEmptyTopicsByTtl(emptyTopicTtlMs);

            // 清理已销毁 Topic 的指标对象
            for (String topic : cleanedTopics) {
                metrics.cleanupTopic(topic);
            }

            // 压缩长期为空的 Topic 计数器条目（回收内存）
            long compactTtlMs = properties.getCleanup().getCompactTtl().toMillis();
            int compacted = registry.compactTopicCounts(compactTtlMs);

            if (zombieCount > 0 || !cleanedTopics.isEmpty() || compacted > 0) {
                metrics.recordZombieReaped(zombieCount);
                log.info("收割扫描完成: 僵尸连接={}, 清理 Topic={}, 压缩计数器={}",
                        zombieCount, cleanedTopics.size(), compacted);
            }
        } catch (Exception e) {
            log.error("收割扫描异常", e);
            metrics.recordError("reaper_scan");
        }
    }

    /**
     * 强制关闭连接.
     * <p>
     * 通过 {@link SseConnectionRegistry#forceDecrementCounters} 原子递减计数器，
     * 确保与 doFinally 之间不会双重递减。
     * </p>
     *
     * @param conn 连接记录
     */
    private void forceClose(SseConnection conn) {
        if (conn.markClosed()) {
            // 标记成功，执行完整清理（包括计数器递减）
            log.debug("强制关闭僵尸连接: {}", conn);
            if (registry.forceDecrementCounters(conn)) {
                if (registry.cleanupIfEmpty(conn.getTopic())) {
                    metrics.cleanupTopic(conn.getTopic());
                }
            }
            // 主动取消 Flux 订阅，立即终止连接（而非等待客户端自行断开）
            conn.cancel();
        }
    }

    /**
     * 检查是否正在运行.
     *
     * @return 运行中返回 true
     */
    public boolean isRunning() {
        return running.get();
    }
}
