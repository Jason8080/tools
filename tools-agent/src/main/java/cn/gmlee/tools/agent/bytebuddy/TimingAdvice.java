package cn.gmlee.tools.agent.bytebuddy;

import cn.gmlee.tools.agent.mod.Monitor;
import net.bytebuddy.asm.Advice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

public class TimingAdvice {

    public static Logger log = LoggerFactory.getLogger(TimingAdvice.class);

    @Advice.OnMethodEnter
    public static Monitor onEnter(@Advice.This Object obj, @Advice.Origin Method method, @Advice.AllArguments Object[] args) {
        return MethodMonitorRegistry.enter(new Monitor(obj, method, args));
    }

    @Advice.OnMethodExit(onThrowable = Throwable.class)
    public static void onExit(@Advice.Enter Monitor mm) {
        MethodMonitorRegistry.exit(mm);
    }
}