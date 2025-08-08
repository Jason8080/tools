package cn.gmlee.tools.agent.mod;

import lombok.Data;

import java.lang.reflect.Method;
import java.util.Objects;

/**
 * 观察者.
 */
@Data
public class Watcher {
    private final Thread thread;
    private final Method method;
    private final long startTime;
    private Object obj;
    private Object[] args;
    private long endTime = System.currentTimeMillis();

    /**
     * Instantiates a new Monitor method.
     *
     * @param obj    the obj
     * @param method the method
     * @param args   the args
     */
    public Watcher(Object obj, Method method, Object[] args) {
        this.obj = obj;
        this.method = method;
        this.args = args;
        this.thread = Thread.currentThread();
        this.startTime = System.currentTimeMillis();
    }

    /**
     * Elapsed millis long.
     *
     * @return the long
     */
    public long elapsedMillis() {
        return elapsedMillis(System.currentTimeMillis());
    }

    /**
     * Elapsed millis long.
     *
     * @param endTime the end time
     * @return the long
     */
    public long elapsedMillis(long endTime) {
        this.endTime = endTime;
        return this.endTime - this.startTime;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Watcher)) return false;
        Watcher that = (Watcher) o;
        return Objects.equals(thread, that.thread) && Objects.equals(method, that.method);
    }

    @Override
    public int hashCode() {
        return Objects.hash(thread, method);
    }
}
