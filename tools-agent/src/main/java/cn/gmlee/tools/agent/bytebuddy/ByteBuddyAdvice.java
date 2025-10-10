package cn.gmlee.tools.agent.bytebuddy;

import net.bytebuddy.asm.Advice;
import net.bytebuddy.implementation.bytecode.assign.Assigner;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class ByteBuddyAdvice {

    @Advice.OnMethodEnter
    public static Object onEnter(@Advice.This Object obj, @Advice.Origin Method method, @Advice.AllArguments Object[] args) {
        Method enterMethod = null;
        Method removeMethod = null;
        try {
            Class<?> clazz = Class.forName("cn.gmlee.tools.agent.bytebuddy.ByteBuddyRegistry");
            Method[] methods = clazz.getDeclaredMethods();
            for (Method clazzMethod : methods) {
                if (clazzMethod.getName().equals("enter") && Modifier.isPublic(clazzMethod.getModifiers())) {
                    enterMethod = clazzMethod;
                }
                if (clazzMethod.getName().equals("remove") && clazzMethod.isVarArgs()) {
                    removeMethod = clazzMethod;
                }
            }
            if (enterMethod != null) return enterMethod.invoke(null, obj, method, args);
        } catch (Exception e) {
            try {
                if (removeMethod != null) removeMethod.invoke(null, Thread.currentThread());
            } catch (Exception ignored) {
            }
        }
        try {
            if (removeMethod != null) removeMethod.invoke(null, Thread.currentThread());
        } catch (Exception ignored) {
        }
        return null;
    }

    @Advice.OnMethodExit(onThrowable = Throwable.class)
    public static void onExit(@Advice.Enter Object watcher, @Advice.Return(typing = Assigner.Typing.DYNAMIC) Object ret, @Advice.Thrown Throwable throwable) {
        Method exitMethod = null;
        Method removeMethod = null;
        try {
            Class<?> clazz = Class.forName("cn.gmlee.tools.agent.bytebuddy.ByteBuddyRegistry");
            Method[] methods = clazz.getDeclaredMethods();
            for (Method clazzMethod : methods) {
                if (clazzMethod.getName().equals("exit") && Modifier.isPublic(clazzMethod.getModifiers())) {
                    exitMethod = clazzMethod;
                }
                if (clazzMethod.getName().equals("remove") && clazzMethod.isVarArgs()) {
                    removeMethod = clazzMethod;
                }
            }
            if (exitMethod != null) exitMethod.invoke(null, watcher, ret, throwable);
        } catch (Exception e) {
            try {
                if (removeMethod != null) removeMethod.invoke(null, Thread.currentThread());
            } catch (Exception ignored) {
            }
        }
    }
}