package cn.gmlee.tools.cache2.server.cache;

import cn.gmlee.tools.cache2.anno.Cache;
import cn.gmlee.tools.cache2.server.Ds;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

/**
 * 缓存服务
 */
public interface CacheServer extends Ds {
    /**
     * 缓存所有字典.
     *
     * @param cache  the cache
     * @param result the result
     * @param field  the field
     * @param list   the list
     */
    void save(Cache cache, Object result, Field field, List<Map<String, Object>> list);
}
