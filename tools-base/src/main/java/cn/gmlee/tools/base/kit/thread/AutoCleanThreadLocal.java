package cn.gmlee.tools.base.kit.thread;

import cn.gmlee.tools.base.kit.task.TimerTaskManager;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 自动清理的本地线程工具.
 *
 * @param <T> the type parameter
 */
public class AutoCleanThreadLocal<T> extends InheritableThreadLocal<T> {

    private final long cleanInterval;

    private final Map<Thread, Long> lastUseTime = new ConcurrentHashMap<>();

    /**
     * Instantiates a new Auto clean thread local.
     */
    public AutoCleanThreadLocal() {
        this(60 * 1000);
    }

    /**
     * Instantiates a new Auto clean thread local.
     *
     * @param cleanIntervalMillis 清理周期
     */
    public AutoCleanThreadLocal(long cleanIntervalMillis) {
        this.cleanInterval = cleanIntervalMillis;
        TimerTaskManager.run(this::cleanStaleEntries);
    }

    @Override
    public T get() {
        T value = super.get();
        lastUseTime.put(Thread.currentThread(), System.currentTimeMillis());
        return value;
    }

    @Override
    public void set(T value) {
        super.set(value);
        lastUseTime.put(Thread.currentThread(), System.currentTimeMillis());
    }

    /**
     * Clean stale entries.
     */
    public void cleanStaleEntries() {
        long currentTime = System.currentTimeMillis();
        Set<Thread> threads = lastUseTime.keySet();
        for (Thread thread : threads) {
            Long ms = lastUseTime.get(thread);
            if (currentTime - ms > cleanInterval) {
                this.remove();
                lastUseTime.remove(thread);
            }
        }
    }
}
