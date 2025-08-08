package cn.gmlee.tools.agent.bytebuddy;

import cn.gmlee.tools.agent.mod.Watcher;
import cn.gmlee.tools.base.util.QuickUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.*;

@Slf4j
public class MethodMonitorRegistry {

    private static final Set<Watcher> WATCHERS = ConcurrentHashMap.newKeySet();

    public static Collection<Watcher> all() {
        return WATCHERS;
    }

    public static Watcher enter(Watcher watcher) {
        WATCHERS.add(watcher);
        return watcher;
    }

    public static void exit(Watcher watcher) {
        boolean remove = WATCHERS.remove(watcher);
        QuickUtil.isFalse(remove, () -> log.error("监控方法删除失败: {}", watcher));
    }
}
