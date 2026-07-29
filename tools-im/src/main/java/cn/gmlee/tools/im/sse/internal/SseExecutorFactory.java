package cn.gmlee.tools.im.sse.internal;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * SSE 线程池创建工厂.
 * <p>
 * 统一创建 daemon 线程池，保证线程命名规范一致，避免线程泄露。
 * 所有创建的线程池均为 daemon 线程，不会阻止 JVM 退出。
 * </p>
 *
 * <h3>使用场景</h3>
 * <ul>
 *   <li>{@link cn.gmlee.tools.im.sse.SseConnectionManager} - 生命周期调度器</li>
 *   <li>{@link cn.gmlee.tools.im.sse.cleanup.ConnectionReaper} - 收割调度器、强制关闭执行器</li>
 * </ul>
 */
public final class SseExecutorFactory {

    private SseExecutorFactory() {
        // 工具类禁止实例化
    }

    /**
     * 创建单线程调度器.
     * <p>
     * 线程为 daemon 线程，命名格式：{@code <name>-0}、{@code <name>-1}（重建时递增）。
     * </p>
     *
     * @param name 线程名称前缀
     * @return 新的 daemon 单线程调度器
     */
    public static ScheduledExecutorService createSingleThreadScheduler(String name) {
        return Executors.newSingleThreadScheduledExecutor(createDaemonThreadFactory(name));
    }

    /**
     * 创建固定大小线程池.
     * <p>
     * 线程为 daemon 线程，命名格式：{@code <namePrefix>-0}、{@code <namePrefix>-1}、...
     * </p>
     *
     * @param threads    线程数量
     * @param namePrefix 线程名称前缀
     * @return 新的 daemon 固定线程池
     */
    public static ExecutorService createFixedThreadPool(int threads, String namePrefix) {
        return Executors.newFixedThreadPool(threads, createDaemonThreadFactory(namePrefix));
    }

    /**
     * 创建 daemon 线程工厂.
     *
     * @param namePrefix 线程名称前缀
     * @return 线程工厂
     */
    private static ThreadFactory createDaemonThreadFactory(String namePrefix) {
        AtomicInteger counter = new AtomicInteger(0);
        return r -> {
            Thread t = new Thread(r, namePrefix + "-" + counter.getAndIncrement());
            t.setDaemon(true);
            return t;
        };
    }
}
