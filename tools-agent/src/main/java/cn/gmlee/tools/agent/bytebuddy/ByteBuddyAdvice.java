package cn.gmlee.tools.agent.bytebuddy;

import cn.gmlee.tools.agent.mod.Watcher;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.implementation.bytecode.assign.Assigner;

import java.lang.reflect.Method;

public class ByteBuddyAdvice {

    @Advice.OnMethodEnter
    public static Watcher onEnter(@Advice.This Object obj, @Advice.Origin Method method, @Advice.AllArguments Object[] args) {
        return ByteBuddyRegistry.enter(Watcher.of(obj, method, args));
    }

    @Advice.OnMethodExit(onThrowable = Throwable.class)
    public static void onExit(@Advice.Enter Watcher watcher, @Advice.Return(typing = Assigner.Typing.DYNAMIC) Object ret, @Advice.Thrown Throwable throwable) {
        ByteBuddyRegistry.exit(Watcher.ret(watcher, ret, throwable));
    }
}