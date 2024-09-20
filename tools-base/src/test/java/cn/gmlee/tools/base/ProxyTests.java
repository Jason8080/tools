package cn.gmlee.tools.base;

import cn.gmlee.tools.base.mod.JsonLog;
import cn.gmlee.tools.base.util.ProxyUtil;
import org.junit.Test;
import org.springframework.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

public class ProxyTests {

    @Test
    public void testCglib(){
        JsonLog log = ProxyUtil.CglibProxy(JsonLog.log()/*, (Object obj, Method method, Object[] args, MethodProxy proxy) -> proxy.invokeSuper(obj, args)*/);
        log.setEx("异常");
        System.out.println(log);
    }
}
