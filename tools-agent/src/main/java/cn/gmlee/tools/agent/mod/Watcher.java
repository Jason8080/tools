package cn.gmlee.tools.agent.mod;

import cn.gmlee.tools.agent.assist.OriginalAssist;
import cn.gmlee.tools.base.util.ProxyUtil;
import lombok.Data;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 观察者.
 */
@Data
public class Watcher {

    /**
     * 默认值键.
     */
    public static final String DEFAULT_KEY = "DEFAULT";

    private long startTime;
    private Thread thread;
    private Method originalMethod;
    private Object originalObj;
    private Object[] originalArgs;
    private Object[] args;
    private Object obj;
    private Method method;
    private Object ret;
    private Throwable throwable;
    private long endTime = System.currentTimeMillis();
    private Map<String, Object> infoMap = new ConcurrentHashMap<>();

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
        OriginalAssist.parserFastClassOriginalObjectAndMethod(watcher); // 支持FastClass代理类
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
     * Put watcher.
     *
     * @param watcher the watcher
     * @param key     the key
     * @param val     the val
     * @return the watcher
     */
    public static <V> Watcher put(Watcher watcher, String key, V val){
        watcher.infoMap.put(key, val);
        return watcher;
    }

    /**
     * Get object.
     *
     * @param watcher the watcher
     * @param key     the key
     * @return the object
     */
    public static <V> V get(Watcher watcher, String key){
        return (V) watcher.infoMap.get(key);
    }

    /**
     * Add watcher.
     *
     * @param watcher the watcher
     * @param val     the val
     * @return the watcher
     */
    public static <V> Watcher add(Watcher watcher, V val){
        watcher.infoMap.put(DEFAULT_KEY, val);
        return watcher;
    }

    /**
     * Get object.
     *
     * @param watcher the watcher
     * @return the object
     */
    public static <V> V get(Watcher watcher){
        return (V) watcher.infoMap.get(DEFAULT_KEY);
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
        return this == o;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
