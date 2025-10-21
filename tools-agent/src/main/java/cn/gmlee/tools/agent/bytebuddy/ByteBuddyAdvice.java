package cn.gmlee.tools.agent.bytebuddy;

import net.bytebuddy.asm.Advice;
import net.bytebuddy.implementation.bytecode.assign.Assigner;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.concurrent.atomic.AtomicReference;

/**
 * ✅ 最终版 ByteBuddyAdvice
 * - 所有静态方法、字段均设为 public
 * - 避免任何 IllegalAccessError
 */
public class ByteBuddyAdvice {

    // 🔧 必须为 public static，否则目标类字节码无法访问
    public static final AtomicReference<Method> ENTER_METHOD = new AtomicReference<>();
    public static final AtomicReference<Method> EXIT_METHOD = new AtomicReference<>();
    public static final AtomicReference<Method> REMOVE_METHOD = new AtomicReference<>();

    // 🔧 同样必须为 public
    public static void initRegistryMethods() {
        if (ENTER_METHOD.get() == null || EXIT_METHOD.get() == null || REMOVE_METHOD.get() == null) {
            synchronized (ByteBuddyAdvice.class) {
                if (ENTER_METHOD.get() == null || EXIT_METHOD.get() == null || REMOVE_METHOD.get() == null) {
                    try {
                        Class<?> clazz = Class.forName("cn.gmlee.tools.agent.bytebuddy.ByteBuddyRegistry");
                        for (Method m : clazz.getDeclaredMethods()) {
                            if ("enter".equals(m.getName()) && Modifier.isPublic(m.getModifiers())) {
                                ENTER_METHOD.set(m);
                            } else if ("exit".equals(m.getName()) && Modifier.isPublic(m.getModifiers())) {
                                EXIT_METHOD.set(m);
                            } else if ("remove".equals(m.getName()) && m.isVarArgs()) {
                                REMOVE_METHOD.set(m);
                            }
                        }
                    } catch (Throwable ignored) {
                        // swallow to prevent blocking classload
                    }
                }
            }
        }
    }

    @Advice.OnMethodEnter
    public static Object onEnter(@Advice.This Object obj,
                                 @Advice.Origin Method method,
                                 @Advice.AllArguments Object[] args) {
        initRegistryMethods();
        Method enter = ENTER_METHOD.get();
        Method remove = REMOVE_METHOD.get();
        try {
            if (enter != null) return enter.invoke(null, obj, method, args);
        } catch (Throwable e) {
            safeRemove(remove);
        }
        safeRemove(remove);
        return null;
    }

    @Advice.OnMethodExit(onThrowable = Throwable.class)
    public static void onExit(@Advice.Enter Object watcher,
                              @Advice.Return(typing = Assigner.Typing.DYNAMIC) Object ret,
                              @Advice.Thrown Throwable throwable) {
        initRegistryMethods();
        Method exit = EXIT_METHOD.get();
        Method remove = REMOVE_METHOD.get();
        try {
            if (exit != null) exit.invoke(null, watcher, ret, throwable);
        } catch (Throwable ignored) {
        } finally {
            safeRemove(remove);
        }
    }

    // 🚩 核心：ByteBuddy 会生成直接调用字节码，必须是 public static
    public static void safeRemove(Method remove) {
        if (remove != null) {
            try {
                remove.invoke(null, Thread.currentThread());
            } catch (Throwable ignored) {
                // ignore silently
            }
        }
    }
}
