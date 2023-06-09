package cn.gmlee.tools.cache2.aspect;

import cn.gmlee.tools.base.enums.Int;
import cn.gmlee.tools.base.mod.Kv;
import cn.gmlee.tools.base.util.BoolUtil;
import cn.gmlee.tools.base.util.ClassUtil;
import cn.gmlee.tools.base.util.ExceptionUtil;
import cn.gmlee.tools.base.util.QuickUtil;
import cn.gmlee.tools.cache2.anno.Cache;
import cn.gmlee.tools.cache2.handler.CacheHandler;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 缓存切面
 *
 * @author - Jas°
 */
@Aspect
public class CacheAspect {

    private static final Logger log = LoggerFactory.getLogger(CacheAspect.class);

    @Resource
    private CacheHandler cacheHandler;

    /**
     * 切入点.
     */
    @Pointcut("execution (* *..controller..*Controller..*(..)) || execution (* *..api..*Api..*(..))")
    public void pointcut() {
    }
    /**
     * 接口后置处理器
     *
     * @param point
     * @param result
     */
    @AfterReturning(value = "pointcut()", returning = "result")
    public void processor(JoinPoint point, Object result) {
        // -------------------------------------------------------------------------------------------------------------
        long start = System.currentTimeMillis();
        List<Kv<Field, Object>> fields = getFields(result, Int.THREE);
        long end = System.currentTimeMillis();
        log.debug("收集对象字段耗时：{}(ms)", end - start);
        // -------------------------------------------------------------------------------------------------------------
        start = System.currentTimeMillis();
        cacheHandler.handler(fields);
        end = System.currentTimeMillis();
        log.debug("缓存字段填充耗时：{}(ms)", end - start);
        // -------------------------------------------------------------------------------------------------------------
    }

    private List<Kv<Field, Object>> getFields(Object result, int depth) {
        List<Kv<Field, Object>> kvs = new ArrayList();
        // 判断是否对象
        if (!BoolUtil.isBean(result, String.class)) {
            return kvs;
        }
        Map<String, Field> fieldsMap = ClassUtil.getFieldsMapUseCache(result);
        if (BoolUtil.isEmpty(fieldsMap)) {
            return kvs;
        }
        for (Field field : fieldsMap.values()) {
            boolean old = field.isAccessible();
            QuickUtil.isFalse(old, () -> field.setAccessible(true));
            try {
                Object ret = ExceptionUtil.suppress(() -> field.get(result));
                if(ret instanceof Collection){
                    for (Object obj : (Collection) ret){
                        kvs.addAll(getFields(obj, depth));
                    }
                }
                // 带有注解
                if (field.isAnnotationPresent(Cache.class)) {
                    kvs.add(new Kv(field, result));
                    continue;
                }
                // 是JAVA类
                if(BoolUtil.isJavaClass(ret)){
                    continue;
                }
                // 是派生类
                if(ret==null || result.getClass().isAssignableFrom(ret.getClass())) {
                    continue;
                }
                if(depth-- > 0){
                    kvs.addAll(getFields(ret, depth));
                }
            } finally {
                QuickUtil.isFalse(old, () -> field.setAccessible(false));
            }
        }
        return kvs;
    }
}
