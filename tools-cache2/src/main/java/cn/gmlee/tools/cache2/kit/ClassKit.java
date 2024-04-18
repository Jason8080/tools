package cn.gmlee.tools.cache2.kit;

import cn.gmlee.tools.base.util.BoolUtil;
import cn.gmlee.tools.base.util.ClassUtil;
import cn.gmlee.tools.base.util.ExceptionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 通用字节码工具类
 *
 * @author Jas °
 * @date 2020 /9/21 (周一)
 */
public class ClassKit extends TimerTask {

    private static final Logger log = LoggerFactory.getLogger(ClassKit.class);

    private static final Map<Object, Map> classMapCache = new ConcurrentHashMap();
    private static final Map<Object, Map> fieldMapCache = new ConcurrentHashMap();

    static {
        // 定时清理缓存
        new Timer().schedule(new ClassKit(), 0, 60 * 1000);
    }

    @Override
    public void run() {
        ExceptionUtil.sandbox(ClassKit::clear);
    }

    /**
     * 缓存清理
     */
    public static void clear() {
        classMapCache.clear();
        fieldMapCache.clear();
    }

    /**
     * 生成Map并缓存 (请注意在 finally 中清理).
     *
     * @param <T>    the type parameter
     * @param <V>    the type parameter
     * @param source the source
     * @return the map
     */
    public static <T, V> Map<String, V> generateMapUseCache(T source) {
        Map<String, V> map = classMapCache.get(source);
        if (BoolUtil.notEmpty(map)) {
            return map;
        }
        map = ClassUtil.generateMap(source);
        classMapCache.put(source, map);
        return map;
    }


    /**
     * 获取Map并缓存 (请注意在 finally 中清理).
     *
     * @param <T>    the type parameter
     * @param source the source
     * @return the fields map use cache
     */
    public static <T> Map<String, Field> getFieldsMapUseCache(T source) {
        Map<String, Field> map = fieldMapCache.get(source);
        if (BoolUtil.notEmpty(map)) {
            return map;
        }
        map = ClassUtil.getFieldsMap(source);
        fieldMapCache.put(source, map);
        return map;
    }
}
