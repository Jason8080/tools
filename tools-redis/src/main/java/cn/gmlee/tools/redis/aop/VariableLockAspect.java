package cn.gmlee.tools.redis.aop;

import cn.gmlee.tools.base.util.*;
import cn.gmlee.tools.redis.anno.VariableLock;
import cn.gmlee.tools.redis.lock.VariableLockServer;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * The type Variable lock aspect.
 */
@Aspect
@RequiredArgsConstructor
public class VariableLockAspect {

    private static final Logger log = LoggerFactory.getLogger(VariableLockAspect.class);

    private final VariableLockServer variableLockServer;

    private static final ThreadLocal<List<String>> VALUE_LOCAL = new InheritableThreadLocal();

    /**
     * Pointcut.
     */
    @Pointcut("@annotation(cn.gmlee.tools.redis.anno.VariableLock)")
    public void pointcut() {
    }

    /**
     * 请求开始根据注解配置加锁.
     *
     * @param point the point
     */
    @Before("pointcut()")
    public void before(JoinPoint point) {
        // 加锁数据
        VariableLock vl = getVariableLock(point);
        String[] names = vl.value();
        List<String> sb = new ArrayList<>(names.length);
        for (int i = 0; i < names.length; i++) {
            getValue(vl, point, names[i], sb);
        }
        if (sb.isEmpty()) {
            return;
        }
        VALUE_LOCAL.set(sb);
        // 变量加锁
        variableLockServer.lock(vl, sb.toArray(new String[0]));
    }

    private VariableLock getVariableLock(JoinPoint point) {
        MethodSignature methodSignature = (MethodSignature) point.getSignature();
        Method methodObj = methodSignature.getMethod();
        VariableLock variableLock = methodObj.getAnnotation(VariableLock.class);
        
        // 如果 value 为空数组，则使用方法的全限定路径名
        if (variableLock.value().length == 0) {
            String methodName = methodSignature.getDeclaringTypeName() + "." + methodSignature.getName();
            return new VariableLock() {
                @Override
                public String[] value() {
                    return new String[]{methodName};
                }

                @Override
                public String biz() {
                    return variableLock.biz();
                }

                @Override
                public VariableLock.Origin[] origin() {
                    return variableLock.origin();
                }

                @Override
                public boolean spin() {
                    return variableLock.spin();
                }

                @Override
                public boolean lock() {
                    return variableLock.lock();
                }

                @Override
                public boolean check() {
                    return variableLock.check();
                }

                @Override
                public boolean unlock() {
                    return variableLock.unlock();
                }

                @Override
                public long timeout() {
                    return variableLock.timeout();
                }

                @Override
                public String message() {
                    return variableLock.message();
                }

                @Override
                public Class<VariableLock> annotationType() {
                    return VariableLock.class;
                }
            };
        }
        
        return variableLock;
    }

    /**
     * 需要保证每个参数都有值(值可以是null)
     *
     * @param vl
     * @param point
     * @param name
     * @param sb
     */
    private void getValue(VariableLock vl, JoinPoint point, String name, List<String> sb) {
        HttpServletRequest request = WebUtil.getRequest();
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
            if (arg instanceof MultipartFile) {
                byte[] bytes = ExceptionUtil.sandbox(() -> ((MultipartFile) arg).getBytes(), e -> {
                    log.warn("变量锁读取文件异常", e);
                    return ExceptionUtil.getOriginMsg(e).getBytes();
                });
                sb.add(Md5Util.encode(bytes));
            } else if (arg != null) {
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
            String parameter = request != null ? request.getParameter(name) : null;
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
        if (BoolUtil.isBean(argVal, String.class, BigDecimal.class, Date.class, LocalDateTime.class, Collection.class)) {
            Map<String, Field> fieldMap = ClassUtil.getFieldsMap(argVal);
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


    /**
     * 请求发生异常单独解锁.
     *
     * <p>
     * 不加锁 或 不检锁: 则异常也不解锁
     * </p>
     *
     * @param point the point
     */
    @AfterThrowing("pointcut()")
    public void afterThrowing(JoinPoint point) {
        List<String> sb = VALUE_LOCAL.get();
        if (BoolUtil.isEmpty(sb)) {
            // 没办法解锁
            return;
        }
        VariableLock vl = getVariableLock(point);
        // 不加锁 || 不检锁 : 异常也不解锁
        if (!vl.lock() || !vl.check()) {
            // 未加锁不解锁
            VALUE_LOCAL.remove();
            return;
        }
        // 变量解锁
        variableLockServer.unlock(vl, sb.toArray(new String[0]));
    }


    /**
     * 请求完成根据注解配置解锁.
     *
     * @param point the point
     */
    @After("pointcut()")
    public void after(JoinPoint point) {
        List<String> sb = VALUE_LOCAL.get();
        // 请求解锁
        VALUE_LOCAL.remove();
        if (BoolUtil.isEmpty(sb)) {
            // 没办法解锁
            return;
        }
        VariableLock vl = getVariableLock(point);
        if (!vl.unlock()) {
            // 不需要解锁
            return;
        }
        // 变量解锁
        variableLockServer.unlock(vl, sb.toArray(new String[0]));
    }

}
