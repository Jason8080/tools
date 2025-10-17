package cn.gmlee.tools.agent.trigger;

import cn.gmlee.tools.agent.mod.Watcher;
import cn.gmlee.tools.base.anno.Monitor;
import cn.gmlee.tools.base.util.BoolUtil;
import cn.gmlee.tools.base.util.NullUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 超时触发器.
 */
public interface TimeoutTrigger {
    /**
     * The constant log.
     */
    Logger log = LoggerFactory.getLogger(TimeoutTrigger.class);

    /**
     * 超时处理器.
     *
     * @param thread   the thread
     * @param watcher  线程中耗时最长的监听器
     * @param watchers 线程中全量的监听器
     */
    default void handler(Thread thread, Watcher watcher, Set<Watcher> watchers) {
        List<Watcher> list = new ArrayList<>(watchers);
        if (BoolUtil.isEmpty(watchers)) {
            return;
        }
        watcher = watcher!=null ? watcher : list.get(0);
        long elapsed = watcher.elapsedMillis();
        long timout = NullUtil.get(timout(watcher), 3000L);
        if (timout > 0 && elapsed > timout) {
            handle(thread, watchers, elapsed, timout);
        }
    }

    /**
     * Timout long.
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
     * Handle.
     *
     * @param thread   the thread
     * @param watchers the watchers
     * @param elapsed  the elapsed
     * @param timout   the timout
     */
    default void handle(Thread thread, Set<Watcher> watchers, long elapsed, long timout) {
        String chain = append(watchers);
        log.warn("\r\n-------------------- Timeout Watcher --------------------\r\n[{}] ({}/{})ms{}\r\n",
                thread != null ? thread.getName() : "unknown", elapsed, timout, chain
        );
    }

    /**
     * Append string.
     *
     * @param watchers the watchers
     * @return the string
     */
    default String append(Set<Watcher> watchers) {
        List<Watcher> list = new ArrayList<>(watchers);
        if (BoolUtil.isEmpty(watchers)) {
            return "监控链路丢失";
        }
        String format = "\r\n\t↓\r\n%s#%s %s(ms)";
        StringBuilder sb = new StringBuilder();
        for (Watcher watcher : list) {
            String method = String.format(format,
                    watcher.getOriginalObj().getClass().getName(),
                    watcher.getOriginalMethod().getName(),
                    watcher.elapsedMillis()
            );
            sb.append(method);
        }
        return sb.toString();
    }
}
