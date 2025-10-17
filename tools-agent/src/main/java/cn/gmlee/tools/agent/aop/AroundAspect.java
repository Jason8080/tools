package cn.gmlee.tools.agent.aop;

import cn.gmlee.tools.agent.bytebuddy.ByteBuddyRegistry;
import cn.gmlee.tools.agent.conf.MonitorMethodProperties;
import cn.gmlee.tools.base.util.BoolUtil;
import cn.gmlee.tools.base.util.ExceptionUtil;
import cn.gmlee.tools.spring.util.IocUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;
import java.util.List;

/**
 * The type Aop aspect.
 */
@Slf4j
@Aspect
@RequiredArgsConstructor
public class AroundAspect {

    private final MonitorMethodProperties monitorMethodProperties;

    /**
     * All methods.
     */
    @Pointcut("execution(* *(..)) && !within(cn.gmlee.tools.agent..*)")
    public void allMethods() {
    }


    /**
     * Around object.
     *
     * @param pjp the pjp
     * @return the object
     * @throws Throwable the throwable
     */
    @Around("allMethods()")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        Object obj = pjp.getTarget();
        Method method = ExceptionUtil.sandbox(() -> (MethodSignature) pjp.getSignature()).getMethod();
        Object[] args = pjp.getArgs();
        Boolean check = ExceptionUtil.sandbox(() -> check(obj, method, args));
        if (!BoolUtil.isTrue(check)) {
            return pjp.proceed(pjp.getArgs());
        }
        Object watcher = ExceptionUtil.sandbox(() -> ByteBuddyRegistry.enter(obj, method, args));
        Object ret = null;
        Throwable throwable = null;
        try {
            return ret = pjp.proceed();
        } catch (Throwable e) {
            throwable = e;
            throw e;
        } finally {
            ByteBuddyRegistry.exit(watcher, ret, throwable);
        }
    }

    /**
     * 检查是否监控
     *
     * @param obj    对象
     * @param method 方法
     * @param args   参数
     * @return true表示监控 false表示不监控
     */
    private boolean check(Object obj, Method method, Object[] args) {
        if (IocUtil.getInstanceProvider() == null) {
            return false;
        }
        if (monitorMethodProperties == null || !BoolUtil.isTrue(monitorMethodProperties.getEnable())) {
            return false;
        }
        String clazz = obj.getClass().getName();
        String name = method.getName();
        List<String> ignores = monitorMethodProperties.getIgnore();
        for (String ignore : ignores) {
            if (ignore.contains("#")) {
                String className = ignore.substring(0, ignore.indexOf('#'));
                String methodName = ignore.substring(ignore.indexOf('#') + 1);
                if (BoolUtil.equalsIgnoreCase(clazz, className) && BoolUtil.equalsIgnoreCase(name, methodName)) {
                    return false; // 表示直接放行（不监控）
                }
                continue;
            }
            if (clazz.startsWith(ignore)) {
                return false; // 表示直接放行（不监控）
            }
        }
        List<String> types = monitorMethodProperties.getType();
        for (String type : types) {
            if (type.contains("#")) {
                continue;
            }
            if (clazz.startsWith(type)) {
                return true; // 表示要监控
            }
        }
        return types.isEmpty(); // == types.isEmpty() ? true : false;
    }
}
