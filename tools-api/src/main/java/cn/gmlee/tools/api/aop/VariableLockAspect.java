package cn.gmlee.tools.api.aop;

import cn.gmlee.tools.api.anno.VariableLock;
import cn.gmlee.tools.api.lock.VariableLockServer;
import cn.gmlee.tools.base.util.BoolUtil;
import cn.gmlee.tools.base.util.ClassUtil;
import cn.gmlee.tools.base.util.WebUtil;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Aspect
@RequiredArgsConstructor
public class VariableLockAspect {

    private static final Logger log = LoggerFactory.getLogger(VariableLockAspect.class);

    private final VariableLockServer variableLockServer;

    private static final ThreadLocal<List<String>> VALUE_LOCAL = new InheritableThreadLocal();

    @Pointcut("@annotation(cn.gmlee.tools.api.anno.VariableLock)")
    public void pointcut() {
    }

    @Before("pointcut()")
    public void before(JoinPoint point) {
        // 请求对象
        HttpServletRequest request = WebUtil.getRequest();
        if (request == null) {
            return;
        }
        // 加锁数据
        VariableLock vl = getVariableLock(point);
        String[] names = vl.value();
        List<String> sb = new ArrayList<>(names.length);
        for (int i = 0; i< names.length; i++){
            getValue(request, vl, point, names[i], sb);
        }
        if (sb.isEmpty()) {
            return;
        }
        // 请求加锁
        VALUE_LOCAL.set(sb);
        variableLockServer.lock(vl, sb.toArray(new String[0]));
    }

    private VariableLock getVariableLock(JoinPoint point) {
        MethodSignature methodSignature = (MethodSignature) point.getSignature();
        Method methodObj = methodSignature.getMethod();
        return methodObj.getAnnotation(VariableLock.class);
    }

    /**
     * 需要保证每个参数都有值(值可以是null)
     *
     * @param request
     * @param vl
     * @param point
     * @param name
     * @param sb
     */
    private void getValue(HttpServletRequest request, VariableLock vl, JoinPoint point, String name, List<String> sb) {
        // 全部范围
        if (BoolUtil.isEmpty(vl.origin())) {
            // HEAD + URL + FORM + COOKIE
            String parameter = WebUtil.getParameter(name, request);
            if (!BoolUtil.isEmpty(parameter)) {
                sb.add(parameter);
                return;
            }
            //  ARGS
            Object arg = getArg(point, name);
            if (arg != null) {
                sb.add(arg.toString());
                return;
            }
        }
        // 优先请求头: 与全部范围时getParameter的优先级保持一致
        if (BoolUtil.containOne(vl.origin(), VariableLock.Origin.HEAD)) {
            String header = WebUtil.getCurrentHeader(name);
            if (!BoolUtil.isEmpty(header)) {
                sb.add(header);
                return;
            }
        }
        if (BoolUtil.containOne(vl.origin(), VariableLock.Origin.QUERY, VariableLock.Origin.FORM)) {
            String parameter = request.getParameter(name);
            if (!BoolUtil.isEmpty(parameter)) {
                sb.add(parameter);
                return;
            }
        }
        if (BoolUtil.containOne(vl.origin(), VariableLock.Origin.ARGS)) {
            Object arg = getArg(point, name);
            if (arg != null) {
                sb.add(arg.toString());
                return;
            }
        }
        if (BoolUtil.containOne(vl.origin(), VariableLock.Origin.COOKIE)) {
            String cookie = WebUtil.getCookie(name, request);
            if (!BoolUtil.isEmpty(cookie)) {
                sb.add(cookie);
                return;
            }
        }
        sb.add(null);
    }

    private Object getArg(JoinPoint point, String name) {
        MethodSignature methodSignature = (MethodSignature) point.getSignature();
        Object[] args = point.getArgs();
        String[] names = methodSignature.getParameterNames();
        for (int i = 0; i < names.length; i++) {
            Object argVal = args[i];
            String argKey = names[i];
            // 检查是否符合要求
            Object check = check(name, argKey, argVal);
            if (check instanceof Boolean && (Boolean) check) {
                return argVal;
            }
            if (!(check instanceof Boolean) && check != null) {
                return check;
            }
        }
        return null;
    }

    private synchronized Object check(String name, String argKey, Object argVal) {
        if (argVal == null) {
            return false;
        }
        if (BoolUtil.eq(name, argKey)) {
            return true;
        }
        // 如果是对象
        if (BoolUtil.isBean(argVal, String.class, BigDecimal.class, Date.class, LocalDateTime.class)) {
            Map<String, Field> fieldMap = ClassUtil.getFieldsMapUseCache(argVal);
            Iterator<Map.Entry<String, Field>> it = fieldMap.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, Field> next = it.next();
                String key = next.getKey();
                Field field = next.getValue();
                Object newValue = ClassUtil.getValue(argVal, field);
                // 检查是否符合要求
                Object check = check(name, key, newValue);
                if (check instanceof Boolean && (Boolean) check) {
                    return newValue;
                }
                if (!(check instanceof Boolean) && check != null) {
                    return check;
                }
            }
            return false;
        }
        return false;
    }


    @After("pointcut()")
    public void after(JoinPoint point) {
        // 解锁数据
        VariableLock vl = getVariableLock(point);
        List<String> sb = VALUE_LOCAL.get();
        if (BoolUtil.isEmpty(sb)) {
            return;
        }
        // 请求解锁
        VALUE_LOCAL.remove();
        variableLockServer.unlock(vl, sb.toArray(new String[0]));
    }

}
