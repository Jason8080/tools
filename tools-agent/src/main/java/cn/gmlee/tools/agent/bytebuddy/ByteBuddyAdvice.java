package cn.gmlee.tools.agent.bytebuddy;

import net.bytebuddy.asm.Advice;
import net.bytebuddy.implementation.bytecode.assign.Assigner;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class ByteBuddyAdvice {

    @Advice.OnMethodEnter
    public static Object onEnter(@Advice.This Object obj, @Advice.Origin Method method, @Advice.AllArguments Object[] args) {
        Method clearMethod = null;
        try {
            Class<?> clazz = Class.forName("cn.gmlee.tools.agent.bytebuddy.ByteBuddyRegistry");
            Method[] methods = clazz.getDeclaredMethods();
            for (Method clazzMethod : methods) {
                if (clazzMethod.getName().equals("enter") && Modifier.isPublic(clazzMethod.getModifiers())) {
                    return clazzMethod.invoke(null, obj, method, args);
                }
                if(clazzMethod.getName().equals("remove") && clazzMethod.isVarArgs()){
                    clearMethod = clazzMethod;
                }
            }
        } catch (Exception e) {
            try {
                if(clearMethod!=null) clearMethod.invoke(null, Thread.currentThread());
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    @Advice.OnMethodExit(onThrowable = Throwable.class)
    public static void onExit(@Advice.Enter Object watcher, @Advice.Return(typing = Assigner.Typing.DYNAMIC) Object ret, @Advice.Thrown Throwable throwable) {
        Method clearMethod = null;
        try {
            Class<?> clazz = Class.forName("cn.gmlee.tools.agent.bytebuddy.ByteBuddyRegistry");
            Method[] methods = clazz.getDeclaredMethods();
            for (Method clazzMethod : methods) {
                if (clazzMethod.getName().equals("exit") && Modifier.isPublic(clazzMethod.getModifiers())) {
                    clazzMethod.invoke(null, watcher, ret, throwable);
                }
                if(clazzMethod.getName().equals("remove") && clazzMethod.isVarArgs()){
                    clearMethod = clazzMethod;
                }
            }
        } catch (Exception e) {
            try {
                if(clearMethod!=null) clearMethod.invoke(null, Thread.currentThread());
            } catch (Exception ignored) {
            }
        }
    }
}