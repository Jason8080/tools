package cn.gmlee.tools.agent.bytebuddy;

import cn.gmlee.tools.agent.assist.TriggerAssist;
import cn.gmlee.tools.agent.mod.Watcher;
import cn.gmlee.tools.agent.trigger.ByteBuddyTrigger;
import cn.gmlee.tools.base.util.BoolUtil;
import cn.gmlee.tools.base.util.QuickUtil;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The type Byte buddy registry.
 */
@Slf4j
public class ByteBuddyRegistry {

    private static final Map<Thread, Set<Watcher>> WATCHERS = new ConcurrentHashMap<>();

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
        return enter(Watcher.of(obj, method, args));
    }

    private static Watcher enter(Watcher watcher) {
        ByteBuddyRegistry.save(watcher);
        TriggerAssist.register(watcher, ByteBuddyTrigger::enter);
        return watcher;
    }

    /**
     * Exit.
     *
     * @param watcher   the watcher
     * @param ret       the ret
     * @param throwable the throwable
     */
    public static void exit(Object watcher, Object ret, Throwable throwable) {
        if(watcher instanceof Watcher){
            exit(Watcher.ret((Watcher) watcher, ret, throwable));
        }
    }

    private static void exit(Watcher watcher) {
        boolean remove = ByteBuddyRegistry.remove(watcher);
        QuickUtil.isFalse(remove, () -> log.error("监控方法删除失败: {}", watcher));
        TriggerAssist.register(watcher, ByteBuddyTrigger::exit);
    }

    private static void save(Watcher watcher) {
        Thread thread = Thread.currentThread();
        Set<Watcher> set = WATCHERS.computeIfAbsent(thread, k -> ConcurrentHashMap.newKeySet());
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
    public static void remove(Thread... threads){
        if(BoolUtil.isEmpty(threads)){
            return;
        }
        for (Thread thread : threads){
            WATCHERS.remove(thread);
        }
    }
}
