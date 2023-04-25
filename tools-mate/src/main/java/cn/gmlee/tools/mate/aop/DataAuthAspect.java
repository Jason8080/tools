package cn.gmlee.tools.mate.aop;

import cn.gmlee.tools.base.anno.DataScope;
import cn.gmlee.tools.base.util.DataScopeUtil;
import lombok.SneakyThrows;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

/**
 * 数据权限控制.
 * <p>
 * 包含行、列的数据权限都会依赖这个切面
 * </p>
 *
 * @author Jas°
 * @date 2021/10/29 (周五)
 */
@Aspect
public class DataAuthAspect {

    private Logger logger = LoggerFactory.getLogger(DataAuthAspect.class);

    @Pointcut("@annotation(cn.gmlee.tools.base.anno.DataScope)")
    public void pointcut() {
    }

    @Before(value = "pointcut()")
    public void before(JoinPoint point) {
        DataScopeUtil.set(getApiAuth(point));
    }

    @After(value = "pointcut()")
    public void after(JoinPoint point) {
        DataScopeUtil.remove();
    }

    @SneakyThrows
    private DataScope getApiAuth(JoinPoint point) {
        Signature signature = point.getSignature();
        if (!(signature instanceof MethodSignature)) {
            return null;
        }
        Method method = ((MethodSignature) signature).getMethod();
        return method.getAnnotation(DataScope.class);
    }

}
