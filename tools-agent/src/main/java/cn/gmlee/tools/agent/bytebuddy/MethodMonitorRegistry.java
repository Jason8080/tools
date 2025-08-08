package cn.gmlee.tools.agent.bytebuddy;

import cn.gmlee.tools.agent.mod.Monitor;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.*;

@Slf4j
public class MethodMonitorRegistry {

    private static final Set<Monitor> allRunningMethods = ConcurrentHashMap.newKeySet();

    public static Collection<Monitor> all() {
        return allRunningMethods;
    }

    public static Monitor enter(Monitor mm) {
        log.error("[Tools enter] {}#{} ({}ms)", mm.getObj().getClass().getName(), mm.getMethod().getName(), mm.getStartTime());
        allRunningMethods.add(mm);
        return mm;
    }

    public static void exit(Monitor mm) {
        log.error("[Tools exit] {}#{} ({}ms)", mm.getObj().getClass().getName(), mm.getMethod().getName(), mm.elapsedMillis());
        boolean remove = allRunningMethods.remove(mm);
        System.out.println(remove);
    }
}
