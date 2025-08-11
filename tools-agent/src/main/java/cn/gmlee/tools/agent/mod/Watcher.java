package cn.gmlee.tools.agent.mod;

import cn.gmlee.tools.base.util.ProxyUtil;
import lombok.Data;

import java.lang.reflect.Method;
import java.util.Objects;

/**
 * 观察者.
 */
@Data
public class Watcher {
    private Thread thread;
    private Method originalMethod;
    private long startTime;
    private Object originalObj;
    private Object[] args;
    private Object obj;
    private Method method;
    private Object ret;
    private Throwable throwable;
    private long endTime = System.currentTimeMillis();

    /**
     * Of watcher.
     *
     * @param obj    the obj
     * @param method the method
     * @param args   the args
     * @return the watcher
     */
    public static Watcher of(Object obj, Method method, Object[] args) {
        Watcher watcher = new Watcher();
        watcher.obj = obj;
        watcher.method = method;
        watcher.args = args;
        watcher.thread = Thread.currentThread();
        watcher.startTime = System.currentTimeMillis();
        watcher.originalObj = ProxyUtil.getOriginalObject(obj);
        watcher.originalMethod = ProxyUtil.getOriginalMethod(obj, method);
        return watcher;
    }

    /**
     * Ret watcher.
     *
     * @param watcher   the watcher
     * @param ret       the ret
     * @param throwable the throwable
     * @return the watcher
     */
    public static Watcher ret(Watcher watcher, Object ret, Throwable throwable){
        watcher.ret = ret;
        watcher.throwable = throwable;
        return watcher;
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
        Watcher watcher = (Watcher) o;
        return Objects.equals(thread, watcher.thread) && Objects.equals(method, watcher.method) && Objects.equals(obj, watcher.obj);
    }

    @Override
    public int hashCode() {
        return Objects.hash(thread, method, obj);
    }
}
