package cn.gmlee.tools.sign.aspect;

import cn.gmlee.tools.base.enums.XCode;
import cn.gmlee.tools.base.ex.SkillException;
import cn.gmlee.tools.base.util.BoolUtil;
import cn.gmlee.tools.base.util.QuickUtil;
import cn.gmlee.tools.base.util.SignUtil;
import cn.gmlee.tools.base.util.WebUtil;
import cn.gmlee.tools.sign.anno.Sign;
import cn.gmlee.tools.sign.conf.LdwSignProperties;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.web.bind.annotation.RequestBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The type Sign aspect.
 */
@Aspect
@RequiredArgsConstructor
public class SignAspect {

    private final LdwSignProperties ldwSignProperties;

    /**
     * 签名
     *
     * @param sign the sign
     */
    @Pointcut("@annotation(sign)")
    public void pointcut(Sign sign) {

    }


    /**
     * Join point object.
     *
     * @param point the join point
     * @param sign  the sign
     * @return the object
     * @throws Throwable the throwable
     */
    @Around("pointcut(sign)")
    public Object signature(ProceedingJoinPoint point, Sign sign) throws Throwable {
        HttpServletRequest request = WebUtil.getRequest();
        HttpServletResponse response = WebUtil.getResponse();
        // 获取
        Object[] args = point.getArgs();
        Object body = getArgByJsonBody(args);
        // 验签
        String oldSignature = SignUtil.sign(body, ldwSignProperties.getApp().get(sign.appId()));
        String currentHeader = request.getHeader(SignUtil.getSignature());
        QuickUtil.isFalse(BoolUtil.eq(oldSignature, currentHeader), () -> new SkillException(XCode.API_SIGN));
        // 放行
        Object proceed = point.proceed(args);
        // 加签
        String newSignature = SignUtil.sign(proceed, ldwSignProperties.getApp().get(sign.appId()));
        response.addHeader(SignUtil.getSignature(), newSignature);
        return proceed;
    }

    private Object getArgByJsonBody(Object... args) {
        if (BoolUtil.isEmpty(args)) {
            return null;
        }
        for (Object arg : args) {
            Class<?> clazz = arg.getClass();
            if (clazz != null && clazz.isAnnotationPresent(RequestBody.class)) {
                return arg;
            }
        }
        return null;
    }
}
