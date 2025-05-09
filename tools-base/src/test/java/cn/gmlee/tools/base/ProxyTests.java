package cn.gmlee.tools.base;

import cn.gmlee.tools.base.define.Payload;
import cn.gmlee.tools.base.define.Reflect;
import cn.gmlee.tools.base.mod.JsonLog;
import cn.gmlee.tools.base.mod.Login;
import cn.gmlee.tools.base.util.ProxyUtil;
import org.junit.Test;
import org.springframework.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

public class ProxyTests {

    @Test
    public void test(){
        Object obj = ProxyUtil.JdkProxy(JsonLog.log(), Payload.class);
        System.out.println(obj);
    }

    @Test
    public void test2(){
        Login o = new Login();
        Payload obj = ProxyUtil.JdkProxy(Payload.class, (Object proxy, Method method, Object[] args) -> {
            System.out.println("代理类型：" + proxy.getClass());
            System.out.println("代理方法：" + method.getName());
            return method.invoke(o, args);
        }, Payload.class);
        obj.getUid();
        System.out.println(obj);
    }

    @Test
    public void testJdk(){
        Login log = ProxyUtil.JdkProxy(new Login<>(), Payload.class);
        log.setToken("123");
        System.out.println(log);
    }

    @Test
    public void testJdk2(){
        Login log = ProxyUtil.JdkProxy(Login.class, (Object proxy, Method method, Object[] args) -> {
                return method.invoke(proxy, args);
        });
        log.setToken("123");
        System.out.println(log);
    }

    @Test
    public void testCglib(){
        JsonLog log = ProxyUtil.CglibProxy(JsonLog.log(), Reflect.class);
        log.setEx("异常");
        System.out.println(log);
    }

    @Test
    public void testCglib2(){
        JsonLog log = ProxyUtil.CglibProxy(JsonLog.class, (Object obj, Method method, Object[] args, MethodProxy proxy) -> {
            return proxy.invokeSuper(obj, args);
        });
        log.setEx("异常");
        System.out.println(log);
    }
}
