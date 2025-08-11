package cn.gmlee.tools.base.util;

import org.springframework.aop.framework.Advised;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

/**
 * 代理工具类.
 */
public class ProxyUtil {

    /**
     * 代理对象追加接口(仅继承接口不可调用).
     * <p>
     * 注意: 必须要有相同的函数才能被调用.
     * </p>
     *
     * @param <T>        the type parameter
     * @param o          the o
     * @param interfaces the interfaces
     * @return the o
     */
    @Deprecated
    public static <T> T JdkProxy(Object o, Class<?>... interfaces) {
        AssertUtil.notNull(o, "代理源对象是空");
        return (T) Proxy.newProxyInstance(
                o.getClass().getClassLoader(),
                CollectionUtil.merge(o.getClass().getInterfaces(), interfaces),
                (Object proxy, Method method, Object[] args) -> method.invoke(o, args)
        );
    }

    /**
     * 代理对象追加接口.
     * <p>
     * 注意: 必须要有相同的函数才能被调用.
     * </p>
     *
     * @param <T>        the type parameter
     * @param target     the target
     * @param ih         the ih
     * @param interfaces the interfaces
     * @return the target
     */
    public static <T> T JdkProxy(Class<?> target, InvocationHandler ih, Class<?>... interfaces) {
        return (T) Proxy.newProxyInstance(
                target.getClassLoader(),
                CollectionUtil.merge(target.getInterfaces(), interfaces), ih
        );
    }

    /**
     * 代理对象追加接口.
     *
     * @param <T>        the type parameter
     * @param o          the o
     * @param interfaces the interfaces
     * @return the target
     */
    public static <T> T CglibProxy(Object o, Class<?>... interfaces) {
        AssertUtil.notNull(o, "代理源对象是空");
        // 代理
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(o.getClass());
        enhancer.setInterfaces(interfaces);
        // 拦截
        MethodInterceptor interceptor = (proxy, method, args, methodProxy) -> method.invoke(o, args);
        enhancer.setCallback(interceptor);
        // 生成
        return (T) enhancer.create();
    }

    /**
     * 代理对象追加接口.
     *
     * @param <T>        the type parameter
     * @param target     the target
     * @param mi         the mi
     * @param interfaces the interfaces
     * @return the target
     */
    public static <T> T CglibProxy(Class<?> target, MethodInterceptor mi, Class<?>... interfaces) {
        AssertUtil.notNull(target, "代理目标类是空");
        // 代理
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(target);
        enhancer.setInterfaces(interfaces);
        // 拦截
        enhancer.setCallback(mi);
        // 生成
        return (T) enhancer.create();
    }

    /**
     * 获取原始对象（解除代理，带死循环保护）
     *
     * @param candidate the candidate
     * @return the original object
     */
    public static Object getOriginalObject(Object candidate) {
        return getOriginalObject(candidate, Collections.newSetFromMap(new IdentityHashMap<>()));
    }

    private static Object getOriginalObject(Object candidate, Set<Object> visited) {
        if (candidate == null) {
            return null;
        }
        if (visited.contains(candidate)) {
            return candidate; // 防止死循环
        }
        visited.add(candidate);

        try {
            // 1. Spring AOP 代理对象
            if (candidate instanceof Advised) {
                Object target = ((Advised) candidate).getTargetSource().getTarget();
                if (target != null) {
                    return getOriginalObject(target, visited);
                }
                return candidate;
            }

            // 2. JDK 动态代理
            if (Proxy.isProxyClass(candidate.getClass())) {
                try {
                    Object h = Proxy.getInvocationHandler(candidate);
                    Method getTarget = null;
                    try {
                        getTarget = h.getClass().getMethod("getTarget");
                    } catch (NoSuchMethodException ignored) {
                    }
                    if (getTarget != null) {
                        getTarget.setAccessible(true);
                        Object target = getTarget.invoke(h);
                        if (target != null) {
                            return getOriginalObject(target, visited);
                        }
                    }
                } catch (Exception ignored) {
                }
                return candidate;
            }

            // 3. CGLIB 代理类（类名中包含 $$ 且有非 Object 父类）
            if (candidate.getClass().getName().contains("$$")) {
                Class<?> superClass = candidate.getClass().getSuperclass();
                if (superClass != null && superClass != Object.class) {
                    try {
                        Object superObj = superClass.cast(candidate);
                        return getOriginalObject(superObj, visited);
                    } catch (ClassCastException ignored) {
                    }
                }
            }

        } catch (Exception ignored) {
            // 出现异常直接返回原对象
            return candidate;
        }

        // 普通对象
        return candidate;
    }


    /**
     * 获取原始方法（根据原始对象类重新解析）
     *
     * @param instance the instance
     * @param method   the method
     * @return the original method
     */
    public static Method getOriginalMethod(Object instance, Method method) {
        Object original = getOriginalObject(instance);
        if (original == null) {
            return method;
        }

        Class<?> clazz = original.getClass();
        try {
            return clazz.getMethod(method.getName(), method.getParameterTypes());
        } catch (NoSuchMethodException e) {
            try {
                return clazz.getDeclaredMethod(method.getName(), method.getParameterTypes());
            } catch (NoSuchMethodException ex) {
                return method; // 找不到就返回原方法
            }
        }
    }
}
