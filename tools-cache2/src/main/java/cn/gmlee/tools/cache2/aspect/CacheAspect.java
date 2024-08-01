package cn.gmlee.tools.cache2.aspect;

import cn.gmlee.tools.base.enums.Int;
import cn.gmlee.tools.base.mod.Kv;
import cn.gmlee.tools.base.util.BoolUtil;
import cn.gmlee.tools.base.util.ExceptionUtil;
import cn.gmlee.tools.base.util.QuickUtil;
import cn.gmlee.tools.cache2.anno.Cache;
import cn.gmlee.tools.cache2.anno.Cache2;
import cn.gmlee.tools.cache2.config.Cache2Conf;
import cn.gmlee.tools.cache2.handler.CacheHandler;
import cn.gmlee.tools.cache2.kit.ClassKit;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

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

    @Autowired
    private CacheHandler cacheHandler;

    @Autowired(required = false)
    private Cache2Conf conf;

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

        // 全局关闭
        if(Boolean.FALSE.equals(conf.getEnable())){
            return;
        }

        try {

            // -------------------------------------------------------------------------------------------------------------
            long start = System.currentTimeMillis();
            List<Kv<Field, Object>> fields = getFields(conf, result, conf != null ? conf.getDepth() : Int.THREE);
            long end = System.currentTimeMillis();
            long elapsedTime1 = end - start;
            QuickUtil.isTrue(conf.isLog(), () -> log.info("收集对象字段耗时：{}(ms)", elapsedTime1));
            // -------------------------------------------------------------------------------------------------------------
            start = System.currentTimeMillis();
            cacheHandler.handler(conf, fields);
            end = System.currentTimeMillis();
            long elapsedTime2 = end - start;
            QuickUtil.isTrue(conf.isLog(), () -> log.info("缓存字段填充耗时：{}(ms)", elapsedTime2));
            // -------------------------------------------------------------------------------------------------------------

        } catch (Throwable e) {

            log.warn("字典翻译异常", e);

        } finally {

            ClassKit.clear();

        }
    }

    private List<Kv<Field, Object>> getFields(Cache2Conf conf, Object result, int depth) {
        List<Kv<Field, Object>> kvs = new ArrayList();
        // 判断是否对象
        if (!BoolUtil.isBean(result, String.class)) {
            return kvs;
        }
        Map<String, Field> fieldsMap = ClassKit.getFieldsMapUseCache(result);
        if (BoolUtil.isEmpty(fieldsMap)) {
            return kvs;
        }
        for (Field field : fieldsMap.values()) {
            boolean old = field.isAccessible();
            QuickUtil.isFalse(old, () -> field.setAccessible(true));
            try {
                Object ret = ExceptionUtil.suppress(() -> field.get(result));
                if (ret instanceof Collection) {
                    for (Object obj : (Collection) ret) {
                        kvs.addAll(getFields(conf, obj, depth - 1));
                    }
                }
                // 带有注解
                if (field.isAnnotationPresent(Cache.class) || (conf != null && field.isAnnotationPresent(Cache2.class))) {
                    kvs.add(new Kv(field, result));
                    continue;
                }
                // 是JAVA类
                if (BoolUtil.isJavaClass(ret)) {
                    continue;
                }
                // 是派生类
                if (ret == null || result.getClass().isAssignableFrom(ret.getClass())) {
                    continue;
                }
                if (depth > 0) {
                    kvs.addAll(getFields(conf, ret, depth - 1));
                }
            } finally {
                QuickUtil.isFalse(old, () -> field.setAccessible(false));
            }
        }
        return kvs;
    }
}
