package cn.gmlee.tools.agent.trigger;

import cn.gmlee.tools.agent.mod.Watcher;
import cn.gmlee.tools.base.anno.Monitor;
import cn.gmlee.tools.base.util.NullUtil;

import java.lang.reflect.Method;

/**
 * 超时触发器.
 */
public interface TimeoutTrigger {
    /**
     * 超时时间(ms).
     *
     * @param watcher the watcher
     * @return the long
     */
    default Long timout(Watcher watcher) {
        Method method = watcher.getOriginalMethod();
        Monitor monitor = method.getAnnotation(Monitor.class);
        if (monitor != null) {
            return monitor.timeout();
        }
        return null;
    }

    /**
     * 超时处理器.
     *
     * @param watcher the watcher
     */
    default void handler(Watcher watcher) {
        long elapsed = watcher.elapsedMillis();
        long timout = NullUtil.get(timout(watcher), 3000L);
        if (timout > 0 && elapsed > timout) {
            handle(watcher, elapsed, timout);
        }
    }

    /**
     * Handle.
     *
     * @param watcher the watcher
     * @param elapsed the elapsed
     * @param timout  the timout
     */
    void handle(Watcher watcher, long elapsed, long timout);
}
