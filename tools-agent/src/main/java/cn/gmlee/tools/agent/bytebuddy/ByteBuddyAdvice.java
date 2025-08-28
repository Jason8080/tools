package cn.gmlee.tools.agent.bytebuddy;

import net.bytebuddy.asm.Advice;
import net.bytebuddy.implementation.bytecode.assign.Assigner;

import java.lang.reflect.Method;

public class ByteBuddyAdvice {

    @Advice.OnMethodEnter
    public static Object onEnter(@Advice.This Object obj, @Advice.Origin Method method, @Advice.AllArguments Object[] args) {
        return ByteBuddyRegistry.enter(obj, method, args);
    }

    @Advice.OnMethodExit(onThrowable = Throwable.class)
    public static void onExit(@Advice.Enter Object watcher, @Advice.Return(typing = Assigner.Typing.DYNAMIC) Object ret, @Advice.Thrown Throwable throwable) {
        ByteBuddyRegistry.exit(watcher, ret, throwable);
    }
}