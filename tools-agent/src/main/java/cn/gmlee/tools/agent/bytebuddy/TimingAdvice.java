package cn.gmlee.tools.agent.bytebuddy;

import cn.gmlee.tools.agent.mod.Watcher;
import net.bytebuddy.asm.Advice;

import java.lang.reflect.Method;

public class TimingAdvice {

    @Advice.OnMethodEnter
    public static Watcher onEnter(@Advice.This Object obj, @Advice.Origin Method method, @Advice.AllArguments Object[] args) {
        return MethodMonitorRegistry.enter(new Watcher(obj, method, args));
    }

    @Advice.OnMethodExit(onThrowable = Throwable.class)
    public static void onExit(@Advice.Enter Watcher watcher) {
        MethodMonitorRegistry.exit(watcher);
    }
}