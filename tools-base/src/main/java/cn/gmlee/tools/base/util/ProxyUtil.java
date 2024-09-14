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
     * 代理对象追加接口.
     *
     * @param <T>        the type parameter
     * @param t          the t
     * @param interfaces the interfaces
     * @return the t
     */
    public static <T> T JdkProxy(T t, Class<?>... interfaces) {
        AssertUtil.notNull(t, "代理源对象是空");
        Class<?> clazz = t.getClass();
        return (T) Proxy.newProxyInstance(
                clazz.getClassLoader(),
                CollectionUtil.merge(clazz.getInterfaces(), interfaces),
                (Object proxy, Method method, Object[] args) -> method.invoke(t, args)
        );
    }

    /**
     * 代理对象追加接口.
     *
     * @param <T>        the type parameter
     * @param t          the t
     * @param ih         the ih
     * @param interfaces the interfaces
     * @return the t
     */
    public static <T> T JdkProxy(T t, InvocationHandler ih, Class<?>... interfaces) {
        AssertUtil.notNull(t, "代理源对象是空");
        Class<?> clazz = t.getClass();
        return (T) Proxy.newProxyInstance(
                clazz.getClassLoader(),
                CollectionUtil.merge(clazz.getInterfaces(), interfaces), ih
        );
    }

    /**
     * 代理对象追加接口.
     *
     * @param <T>        the type parameter
     * @param t          the t
     * @param interfaces the interfaces
     * @return the t
     */
    public static <T> T CglibProxy(T t, Class<?>... interfaces) {
        AssertUtil.notNull(t, "代理源对象是空");
        Class<?> clazz = t.getClass();
        // 代理
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(clazz);
        enhancer.setInterfaces(interfaces);
        // 拦截
        MethodInterceptor interceptor = (proxy, method, args, methodProxy) -> method.invoke(t, args);
        enhancer.setCallback(interceptor);
        // 生成
        return (T) enhancer.create();
    }

    /**
     * 代理对象追加接口.
     *
     * @param <T>        the type parameter
     * @param t          the t
     * @param ih         the ih
     * @param interfaces the interfaces
     * @return the t
     */
    public static <T> T CglibProxy(T t, org.springframework.cglib.proxy.InvocationHandler ih, Class<?>... interfaces) {
        AssertUtil.notNull(t, "代理源对象是空");
        Class<?> clazz = t.getClass();
        // 代理
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(clazz);
        enhancer.setInterfaces(interfaces);
        // 拦截
        enhancer.setCallback(ih);
        // 生成
        return (T) enhancer.create();
    }
}
