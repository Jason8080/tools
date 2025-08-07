package cn.gmlee.tools.agent.bytebuddy;

import cn.gmlee.tools.agent.mod.MethodClock;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.*;

public class MethodMonitorRegistry {

    private static final ThreadLocal<Deque<MethodClock>> executionStack = ThreadLocal.withInitial(ArrayDeque::new);
    private static final Set<MethodClock> allRunningMethods = ConcurrentHashMap.newKeySet();

    public static void start(Method method) {
        MethodClock info = new MethodClock(method);
        executionStack.get().push(info);
        allRunningMethods.add(info);
    }

    public static void end() {
        Deque<MethodClock> stack = executionStack.get();
        if (!stack.isEmpty()) {
            MethodClock info = stack.pop();
            allRunningMethods.remove(info);
        }
        if (stack.isEmpty()) {
            executionStack.remove();
        }
    }

    public static Collection<MethodClock> all() {
        return allRunningMethods;
    }
}
