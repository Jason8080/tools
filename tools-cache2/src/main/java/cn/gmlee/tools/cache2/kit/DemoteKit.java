package cn.gmlee.tools.cache2.kit;

import cn.gmlee.tools.base.enums.Int;
import cn.gmlee.tools.cache2.anno.Cache;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 缓存降级工具.
 */
public class DemoteKit {
    private static final Map<String, Integer> DEMOTE_MAP = new ConcurrentHashMap<>();

    /**
     * 是否存在降级.
     *
     * @param cache  the cache
     * @param result the result
     * @param field  the field
     * @return 是否存在降级 boolean
     */
    public static boolean hasDemote(Cache cache, Object result, Field field) {
        String key = CacheKit.generateKey(cache, result, field);
        Integer count = DEMOTE_MAP.get(key);
        if(count == null){
            DEMOTE_MAP.put(key, 0);
            return false;
        }
        if(Int.ZERO.equals(count)){
            return false;
        }
        if(count++ > 100){
            DEMOTE_MAP.put(key, 0);
            return false;
        }
        DEMOTE_MAP.put(key, count);
        return true;
    }

    /**
     * 降级.
     *
     * @param cache  the cache
     * @param result the result
     * @param field  the field
     */
    public static void demote(Cache cache, Object result, Field field) {
        String key = CacheKit.generateKey(cache, result, field);
        DEMOTE_MAP.put(key, DEMOTE_MAP.get(key) + 1);
    }
}
