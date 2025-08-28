package cn.gmlee.tools.agent.bytebuddy;

import cn.gmlee.tools.agent.assist.TriggerAssist;
import cn.gmlee.tools.agent.mod.Watcher;
import cn.gmlee.tools.agent.trigger.ByteBuddyTrigger;
import cn.gmlee.tools.base.util.QuickUtil;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class ByteBuddyRegistry {

    private static final Set<Watcher> WATCHERS = ConcurrentHashMap.newKeySet();

    public static Collection<Watcher> all() {
        return WATCHERS;
    }

    public static Object enter(Object obj, Method method, Object[] args) {
        return enter(Watcher.of(obj, method, args));
    }

    private static Watcher enter(Watcher watcher) {
        WATCHERS.add(watcher);
        TriggerAssist.register(watcher, ByteBuddyTrigger::enter);
        return watcher;
    }

    public static void exit(Object watcher, Object ret, Throwable throwable) {
        if(watcher instanceof Watcher){
            exit(Watcher.ret((Watcher) watcher, ret, throwable));
        }
    }

    private static void exit(Watcher watcher) {
        boolean remove = WATCHERS.remove(watcher);
        QuickUtil.isFalse(remove, () -> log.error("监控方法删除失败: {}", watcher));
        TriggerAssist.register(watcher, ByteBuddyTrigger::exit);
    }
}
