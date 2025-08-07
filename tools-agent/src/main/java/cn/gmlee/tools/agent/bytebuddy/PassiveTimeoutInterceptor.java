package cn.gmlee.tools.agent.bytebuddy;

import net.bytebuddy.implementation.bind.annotation.*;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;

public class PassiveTimeoutInterceptor {

    @RuntimeType
    public Object intercept(@SuperCall Callable<Object> original, @Origin Method method) throws Exception {
        try {
            MethodMonitorRegistry.start(method);
            return original.call();  // 原线程执行，不包一层线程
        } finally {
            MethodMonitorRegistry.end();
        }
    }
}
