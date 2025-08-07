package cn.gmlee.tools.agent.mod;

import java.lang.reflect.Method;

/**
 * 方法时钟.
 */
public class MethodClock {
    /**
     * The Thread.
     */
    public final Thread thread;
    /**
     * The Method.
     */
    public final Method method;
    /**
     * The Start time.
     */
    public final long startTime;
    /**
     * The End time.
     */
    public long endTime = System.currentTimeMillis();

    /**
     * Instantiates a new Method execution info.
     *
     * @param method the method
     */
    public MethodClock(Method method) {
        this.method = method;
        this.thread = Thread.currentThread();
        this.startTime = System.currentTimeMillis();
    }

    /**
     * Instantiates a new Method execution info.
     *
     * @param method the method
     * @param thread the thread
     */
    public MethodClock(Method method, Thread thread) {
        this.method = method;
        this.thread = thread;
        this.startTime = System.currentTimeMillis();
    }

    /**
     * Instantiates a new Method execution info.
     *
     * @param method    the method
     * @param thread    the thread
     * @param startTime the start time
     */
    public MethodClock(Method method, Thread thread, long startTime) {
        this.method = method;
        this.thread = thread;
        this.startTime = startTime;
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
}
