package cn.gmlee.tools.datalog.aop;

import cn.gmlee.tools.base.anno.ApiPrint;
import cn.gmlee.tools.base.assist.ApiAssist;
import cn.gmlee.tools.base.util.JsonUtil;
import cn.gmlee.tools.base.util.TimeUtil;
import cn.gmlee.tools.base.util.WebUtil;
import cn.gmlee.tools.datalog.model.LogApi;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Date;

/**
 * @author - Jas°
 * @Description ApiPrint切面方法 获取api描述信息
 */
@Aspect
public class DatalogAspect {

    private static final ThreadLocal<LogApi> threadLocalDatalog = new ThreadLocal();

    private Logger logger = LoggerFactory.getLogger(DatalogAspect.class);

    /**
     * 避免别人内存泄露
     *
     * @return
     */
    public static LogApi get() {
        return threadLocalDatalog.get();
    }

    @Pointcut("@annotation(cn.gmlee.tools.base.anno.ApiPrint)")
    public void pointcut() {
    }


    /**
     * 输出内容如下:
     * 1. 请求地址
     * 2. 请求说明 [ApiPrint - value(version)]
     * 3. 请求时间
     *
     * @param point
     */
    @Before("pointcut()")
    public void before(JoinPoint point) {
        MethodSignature methodSignature = (MethodSignature) point.getSignature();
        String[] names = methodSignature.getParameterNames();
        Object[] args = point.getArgs();
        String className = methodSignature.getDeclaringTypeName();
        Method methodObj = methodSignature.getMethod();
        ApiPrint ap = methodObj.getAnnotation(ApiPrint.class);
        LogApi logApi = new LogApi();
        logApi.setApi(ap.value());
        logApi.setSite(TimeUtil.getCurrentMs() + ApiAssist.getSite(className, methodObj));
        logApi.setParams(JsonUtil.toJson(args));
        logApi.setRequestIp(WebUtil.getCurrentIp());
        logApi.setRequestUrl(WebUtil.getCurrentPath());
        logApi.setRequestTime(new Date());
        threadLocalDatalog.set(logApi);
    }


    @After("pointcut()")
    public void after(JoinPoint point) {
        threadLocalDatalog.remove();
    }

}
