package cn.gmlee.tools.base.util;

import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

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
}
