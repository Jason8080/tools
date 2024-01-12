package cn.gmlee.tools.log.aop;

import cn.gmlee.tools.base.anno.ApiPrint;
import cn.gmlee.tools.base.assist.ApiAssist;
import cn.gmlee.tools.base.mod.JsonLog;
import cn.gmlee.tools.base.util.ExceptionUtil;
import cn.gmlee.tools.base.util.NullUtil;
import cn.gmlee.tools.base.util.TimeUtil;
import cn.gmlee.tools.base.util.WebUtil;
import cn.gmlee.tools.log.cof.ApiPrintTrigger;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 日志打印.
 * <p>ApiPrint切面方法 获取api描述信息.</p>
 *
 * @author - Jas°
 */
@Slf4j
@Aspect
public class ApiPrintAspect {

    private final ConcurrentHashMap<String, Long> map = new ConcurrentHashMap<>();

    @Value("${tools.log.maxlength:-1}")
    private Integer maxlength;

    @Resource
    private ApiPrintTrigger apiPrintTrigger;

    @Pointcut("@annotation(cn.gmlee.tools.base.anno.ApiPrint)")
    public void pointcut() {
    }


    /**
     * 输出内容如下:
     * 1. 请求地址
     * 2. 请求说明 [ApiPrint - value(version)]
     * 3. 请求时间
     *
     * @param point the point
     */
    @Before("pointcut()")
    public void before(JoinPoint point) {
        MethodSignature methodSignature = (MethodSignature) point.getSignature();
        String className = methodSignature.getDeclaringTypeName();
        Method methodObj = methodSignature.getMethod();
        map.put(ApiAssist.getSite(className, methodObj), System.currentTimeMillis());
    }


    /**
     * 输出内容如下:
     * 1. 请求地址
     * 2. 请求说明 [ApiPrint - value(version)]
     * 3. 请求参数
     * 4. 响应参数
     * 5. 响应时间
     *
     * @param point  the point
     * @param result the result
     */
    @SuppressWarnings("all")
    @AfterReturning(value = "pointcut()", returning = "result")
    public void afterReturning(JoinPoint point, Object result) {
        MethodSignature methodSignature = (MethodSignature) point.getSignature();
        String[] names = methodSignature.getParameterNames();
        Object[] args = point.getArgs();
        String className = methodSignature.getDeclaringTypeName();
        Method methodObj = methodSignature.getMethod();
        String site = ApiAssist.getSite(className, methodObj);
        Long startMs = map.remove(site);
        Long elapsedTime = System.currentTimeMillis() - startMs;
        ApiPrint ap = methodObj.getAnnotation(ApiPrint.class);
        JsonLog jsonLog = JsonLog.log()
                .setUrl(WebUtil.getCurrentPath())
                .setPrint(NullUtil.get(ap.value(), methodObj.getName()))
                .setType(ap.type())
                .setRequestTime(String.valueOf(startMs))
                .setRequestIp(WebUtil.getCurrentIp())
                .setRequestHeaders(WebUtil.getCurrentHeaderMap())
                .setRequestParams(args)
                .setResponseParams(ApiAssist.getResponseParams(methodObj.getReturnType(), result))
                .setResponseTime(TimeUtil.getCurrentDatetime())
                .setElapsedTime(elapsedTime)
                .setSite(site);
        log.info(jsonLog.builder(ap.format(), getMaxlength(ap)));
        ExceptionUtil.sandbox(() -> apiPrintTrigger.log(jsonLog, result, null));
    }

    /**
     * 输出内容如下:
     * 1. 请求地址
     * 2. 请求说明 [ApiPrint - value(version)]
     * 3. 请求参数
     * 4. 异常信息
     * 5. 响应时间
     *
     * @param point the point
     * @param e     the e
     */
    @SuppressWarnings("all")
    @AfterThrowing(value = "pointcut()", throwing = "e")
    public void afterThrowing(JoinPoint point, Exception e) {
        MethodSignature methodSignature = (MethodSignature) point.getSignature();
        String[] names = methodSignature.getParameterNames();
        Object[] args = point.getArgs();
        String className = methodSignature.getDeclaringTypeName();
        Method methodObj = methodSignature.getMethod();
        String site = ApiAssist.getSite(className, methodObj);
        Long startMs = map.remove(site);
        Long elapsedTime = System.currentTimeMillis() - startMs;
        ApiPrint ap = methodObj.getAnnotation(ApiPrint.class);
        JsonLog jsonLog = JsonLog.log()
                .setUrl(WebUtil.getCurrentPath())
                .setPrint(NullUtil.get(ap.value(), methodObj.getName()))
                .setType(ap.type())
                .setRequestTime(String.valueOf(startMs))
                .setRequestIp(WebUtil.getCurrentIp())
                .setRequestHeaders(WebUtil.getCurrentHeaderMap())
                .setRequestParams(args)
                .setResponseTime(TimeUtil.getCurrentDatetime())
                .setElapsedTime(elapsedTime)
                .setEx(ExceptionUtil.getOriginMsg(e))
                .setSite(site);
        log.info(jsonLog.builder(ap.format(), getMaxlength(ap)));
        ExceptionUtil.sandbox(() -> apiPrintTrigger.log(jsonLog, null, e));
    }


    /**
     * Get maxlength long.
     *
     * @param apiPrint the api print
     * @return the long
     */
    public int getMaxlength(ApiPrint apiPrint) {
        int length = apiPrint.length();
        if (length != -1) {
            return length;
        }
        return maxlength;
    }
}
