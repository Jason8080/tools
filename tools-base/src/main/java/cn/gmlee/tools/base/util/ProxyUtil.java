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
        return (T) Proxy.newProxyInstance(
                t.getClass().getClassLoader(),
                CollectionUtil.merge(t.getClass().getInterfaces(), interfaces),
                (Object proxy, Method method, Object[] args) -> method.invoke(t, args)
        );
    }

    /**
     * 代理对象追加接口.
     *
     * @param <T>        the type parameter
     * @param target     the target
     * @param ih         the ih
     * @param interfaces the interfaces
     * @return the target
     */
    public static <T> T JdkProxy(Class<T> target, InvocationHandler ih, Class<?>... interfaces) {
        return (T) Proxy.newProxyInstance(
                target.getClassLoader(),
                CollectionUtil.merge(target.getInterfaces(), interfaces), ih
        );
    }

    /**
     * 代理对象追加接口.
     *
     * @param <T>        the type parameter
     * @param t          the t
     * @param interfaces the interfaces
     * @return the target
     */
    public static <T> T CglibProxy(T t, Class<?>... interfaces) {
        AssertUtil.notNull(t, "代理源对象是空");
        // 代理
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(t.getClass());
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
     * @param target     the target
     * @param ih         the ih
     * @param interfaces the interfaces
     * @return the target
     */
    public static <T> T CglibProxy(Class<T> target, org.springframework.cglib.proxy.InvocationHandler ih, Class<?>... interfaces) {
        AssertUtil.notNull(target, "代理目标类是空");
        // 代理
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(target);
        enhancer.setInterfaces(interfaces);
        // 拦截
        enhancer.setCallback(ih);
        // 生成
        return (T) enhancer.create();
    }
}
