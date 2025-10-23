package cn.gmlee.tools.agent.bytebuddy;

import cn.gmlee.tools.agent.assist.TriggerAssist;
import cn.gmlee.tools.agent.conf.MonitorMethodProperties;
import cn.gmlee.tools.agent.mod.Watcher;
import cn.gmlee.tools.agent.trigger.AgentTrigger;
import cn.gmlee.tools.base.util.BoolUtil;
import cn.gmlee.tools.base.util.QuickUtil;
import cn.gmlee.tools.spring.util.IocUtil;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * The type Byte buddy registry.
 */
@Slf4j
public class ByteBuddyRegistry {

    private static MonitorMethodProperties props;

    private static final Map<Thread, Set<Watcher>> WATCHERS = new WeakHashMap<>();

    /**
     * All map.
     *
     * @return the map
     */
    public static Map<Thread, Set<Watcher>> all() {
        return WATCHERS;
    }

    /**
     * Enter object.
     *
     * @param obj    the obj
     * @param method the method
     * @param args   the args
     * @return the object
     */
    public static Object enter(Object obj, Method method, Object[] args) {
        try {
            return enter(Watcher.of(obj, method, args));
        } catch (Exception e) {
            ByteBuddyRegistry.remove(Thread.currentThread());
            log.error("[Tools Agent]进入 {} 方法异常", method.getName(), e);
        }
        return null;
    }

    private static Watcher enter(Watcher watcher) {
        if (close()) {
            return watcher;
        }
        if (ignoreThread(watcher)) {
            return watcher;
        }
        ByteBuddyRegistry.save(watcher);
        TriggerAssist.register(watcher, AgentTrigger::enter);
        return watcher;
    }

    private static boolean close() {
        if (props == null) {
            props = IocUtil.contain(MonitorMethodProperties.class) ? IocUtil.getBean(MonitorMethodProperties.class) : null;
        }
        // 配置不存在视为关闭 || 不开启就是关闭
        return props == null || !props.getEnable();
    }

    private static boolean ignoreThread(Watcher watcher) {
        String name = watcher.getThread().getName();
        if (props == null) {
            props = IocUtil.contain(MonitorMethodProperties.class) ? IocUtil.getBean(MonitorMethodProperties.class) : null;
        }
        if (props == null || props.getIgnore().isEmpty()) {
            return false;
        }
        Optional<String> any = props.getIgnore().stream()
                .filter(x -> x.startsWith("$"))
                .map(x -> x.substring(1))
                .filter(name::startsWith).findAny();
        return any.isPresent();
    }

    /**
     * Exit.
     *
     * @param watcher   the watcher
     * @param ret       the ret
     * @param throwable the throwable
     */
    public static void exit(Object watcher, Object ret, Throwable throwable) {
        if (watcher instanceof Watcher) {
            try {
                exit(Watcher.ret((Watcher) watcher, ret, throwable));
            } catch (Exception e) {
                log.error("[Tools Agent]退出 {} 方法异常", ((Watcher) watcher).getMethod(), e);
                ByteBuddyRegistry.remove(Thread.currentThread());
            }
            return;
        }
        ByteBuddyRegistry.remove(Thread.currentThread());
    }

    private static void exit(Watcher watcher) {
        if (close()) {
            return;
        }
        if (ignoreThread(watcher)) {
            return;
        }
        boolean remove = ByteBuddyRegistry.remove(watcher);
//        QuickUtil.isFalse(remove, () -> log.warn("监控方法删除失败: {}", watcher.getMethod()));
        TriggerAssist.register(watcher, AgentTrigger::exit);
    }

    private static void save(Watcher watcher) {
        Set<Watcher> set = WATCHERS.computeIfAbsent(watcher.getThread(), k -> Collections.synchronizedSet(new LinkedHashSet<>()));
        set.add(watcher);
    }

    private static boolean remove(Watcher watcher) {
        Thread thread = watcher.getThread();
        Set<Watcher> set = WATCHERS.get(thread);
        if (set == null) {
            return false;
        }
        return set.remove(watcher);
    }

    /**
     * Remove.
     *
     * @param threads the threads
     */
    public static void remove(Thread... threads) {
        if (BoolUtil.isEmpty(threads)) {
            WATCHERS.clear();
            log.warn("监控方法内存清理完成...");
            return;
        }
        for (Thread thread : threads) {
            WATCHERS.remove(thread);
        }
    }
}
