package cn.gmlee.tools.agent.bytebuddy;

import cn.gmlee.tools.agent.assist.TriggerAssist;
import cn.gmlee.tools.agent.mod.Watcher;
import cn.gmlee.tools.base.util.QuickUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class ByteBuddyRegistry {

    private static final Set<Watcher> WATCHERS = ConcurrentHashMap.newKeySet();

    public static Collection<Watcher> all() {
        return WATCHERS;
    }

    public static Watcher enter(Watcher watcher) {
        WATCHERS.add(watcher);
        TriggerAssist.trigger(watcher, ByteBuddyTrigger::enter);
        return watcher;
    }

    public static void exit(Watcher watcher) {
        boolean remove = WATCHERS.remove(watcher);
        QuickUtil.isFalse(remove, () -> log.error("监控方法删除失败: {}", watcher));
        TriggerAssist.trigger(watcher, ByteBuddyTrigger::exit);
    }
}
