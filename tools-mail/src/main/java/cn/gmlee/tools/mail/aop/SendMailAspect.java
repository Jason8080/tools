package cn.gmlee.tools.mail.aop;

import cn.gmlee.tools.base.assist.ApiAssist;
import cn.gmlee.tools.base.enums.Advice;
import cn.gmlee.tools.base.mod.JsonLog;
import cn.gmlee.tools.base.util.BoolUtil;
import cn.gmlee.tools.base.util.TimeUtil;
import cn.gmlee.tools.base.util.WebUtil;
import cn.gmlee.tools.mail.anno.SendMail;
import cn.gmlee.tools.mail.server.MailServer;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * @author - Jas°
 * @Description SendMail切面方法 获取api描述信息
 */
@Aspect
public class SendMailAspect {
    private Logger logger = LoggerFactory.getLogger(SendMailAspect.class);

    @Resource
    private MailServer mailServer;

    private static final String MAIL_TITLE = "【%s】您有新载消息请注意查收!";

    @Pointcut("@annotation(cn.gmlee.tools.mail.anno.SendMail)")
    public void pointcut() {
    }

    @Around(value = "pointcut()")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        MethodSignature methodSignature = (MethodSignature) pjp.getSignature();
        String[] names = methodSignature.getParameterNames();
        Object[] args = pjp.getArgs();
        String className = methodSignature.getDeclaringTypeName();
        Method methodObj = methodSignature.getMethod();
        String key = getKey(className, methodObj);
        return proceed(pjp, names, args, methodObj, key);
    }

    private Object proceed(ProceedingJoinPoint pjp, String[] names, Object[] args, Method methodObj, String key) throws Throwable {
        SendMail sm = methodObj.getAnnotation(SendMail.class);
        JsonLog jsonLog = getJsonLog(names, args, key);
        String author = sm.value();
        Advice[] advices = sm.advice();
        LocalDateTime beforeTime = LocalDateTime.now();
        Object result = null;
        Throwable ex = null;
        try {
            if (BoolUtil.containOne(advices, Advice.Before)) {
                String log = build(jsonLog, methodObj, Advice.Before,
                        TimeUtil.format(beforeTime), null, null, null, ex.getMessage());
                mailServer.log(String.format(MAIL_TITLE, author), log, sm.recipients());
            }
            result = pjp.proceed(args);
            LocalDateTime afterTime = LocalDateTime.now();
            if (BoolUtil.containOne(advices, Advice.Returning)) {
                Duration duration = Duration.between(beforeTime, afterTime);
                String log = build(jsonLog, methodObj, Advice.Returning,
                        TimeUtil.format(beforeTime), result, TimeUtil.format(afterTime), duration.toMillis(), ex.getMessage());
                mailServer.log(String.format(MAIL_TITLE, author), log, sm.recipients());
            }
            return result;
        } catch (Throwable throwable) {
            ex = throwable;
            LocalDateTime afterTime = LocalDateTime.now();
            if (BoolUtil.containOne(advices, Advice.Throwing)) {
                Duration duration = Duration.between(beforeTime, afterTime);
                String log = build(jsonLog, methodObj, Advice.Throwing,
                        TimeUtil.format(beforeTime), result, TimeUtil.format(afterTime), duration.toMillis(), ex.getMessage());
                mailServer.log(String.format(MAIL_TITLE, author), log, sm.recipients());
            }
            throw throwable;
        } finally {
            LocalDateTime afterTime = LocalDateTime.now();
            Duration duration = Duration.between(beforeTime, afterTime);
            String log = build(jsonLog, methodObj, Advice.After,
                    TimeUtil.format(beforeTime), result, TimeUtil.format(afterTime), duration.toMillis(), ex.getMessage());
            logger.debug(log);
            if (BoolUtil.containOne(advices, Advice.After)) {
                mailServer.log(String.format(MAIL_TITLE, author), log, sm.recipients());
            }
        }
    }

    private JsonLog getJsonLog(String[] names, Object[] args, String key) {
        JsonLog jsonLog = JsonLog.log()
                .setUrl(WebUtil.getPath(WebUtil.getRequest()))
                .setRequestParams(args)
                .setSite(key);
        return jsonLog;
    }

    private String build(JsonLog jsonLog, Method methodObj, Advice advice,
                         String requestTime, Object result, String responseTime, Long elapsedTime, String ex) {
        return jsonLog.setPrint(advice.name())
                .setRequestTime(requestTime)
                .setResponseParams(ApiAssist.getResponseParams(methodObj.getReturnType(), result))
                .setResponseTime(responseTime)
                .setElapsedTime(elapsedTime)
                .setEx(ex)
                .builder(false, true, -1);
    }


    private String getKey(String className, Method methodObj) {
        Long threadId = Thread.currentThread().getId();
        return threadId + ":" + className + "." + methodObj.getName();
    }
}
