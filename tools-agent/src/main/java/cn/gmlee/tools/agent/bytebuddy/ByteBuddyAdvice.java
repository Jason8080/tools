package cn.gmlee.tools.agent.bytebuddy;

import cn.gmlee.tools.base.util.AssertUtil;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.implementation.bytecode.assign.Assigner;

import java.lang.reflect.Method;

public class ByteBuddyAdvice {

    private static Method enterMethod = null;
    private static Method exitMethod = null;

    static {
        try {
            Class<?> clazz = Class.forName("cn.gmlee.tools.agent.bytebuddy.ByteBuddyRegistry");
            Method[] methods = clazz.getMethods();
            for (Method method : methods) {
                if (method.getName().equals("enter")) {
                    enterMethod = method;
                } else if (method.getName().equals("exit")) {
                    exitMethod = method;
                }
            }
            AssertUtil.notNull(exitMethod, "Tools ByteBuddyRegistry enterMethod not found.");
            AssertUtil.notNull(exitMethod, "Tools ByteBuddyRegistry exitMethod not found.");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Tools ByteBuddyRegistry load fail.", e);
        }
    }

    @Advice.OnMethodEnter
    public static Object onEnter(@Advice.This Object obj, @Advice.Origin Method method, @Advice.AllArguments Object[] args) {
        try {
            return enterMethod.invoke(null, obj, method, args);
        } catch (Exception ignored) {
        }
        return null;
    }

    @Advice.OnMethodExit(onThrowable = Throwable.class)
    public static void onExit(@Advice.Enter Object watcher, @Advice.Return(typing = Assigner.Typing.DYNAMIC) Object ret, @Advice.Thrown Throwable throwable) {
        try {
            exitMethod.invoke(null, watcher, ret, throwable);
        } catch (Exception ignored) {
        }
    }
}