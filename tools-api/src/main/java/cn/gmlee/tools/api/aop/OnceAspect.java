package cn.gmlee.tools.api.aop;

import cn.gmlee.tools.api.anno.Once;
import cn.gmlee.tools.api.assist.OnceAssist;
import cn.gmlee.tools.api.once.OnceHandler;
import cn.gmlee.tools.base.util.WebUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.lang.reflect.Method;

/**
 * @author - Jas°
 * @Description Api切面方法 获取api描述信息
 */
@Aspect
@Component
public class OnceAspect {
    @Resource
    private OnceHandler onceHandler;

    @Pointcut("@annotation(cn.gmlee.tools.api.anno.Once)")
    public void pointcut() {
    }

    /**
     * 环绕通知
     *
     * <p>
     * 注意不要将之加载Service当中, 否则会影响事务。
     * </p>
     *
     * @param point
     */
    @Around(value = "pointcut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        MethodSignature methodSignature = (MethodSignature) point.getSignature();
        Method methodObj = methodSignature.getMethod();
        Once once = methodObj.getAnnotation(Once.class);
        Object[] args = point.getArgs();
        if (OnceAssist.requestOk(once, args)) {
            return point.proceed(args);
        }
        return onceHandler.handler(WebUtil.getRequest(), WebUtil.getResponse(), args);
    }
}
