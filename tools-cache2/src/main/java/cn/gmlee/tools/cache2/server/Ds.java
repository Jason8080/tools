package cn.gmlee.tools.cache2.server;

import cn.gmlee.tools.cache2.anno.Cache;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

/**
 * 数据源.
 */
public interface Ds {
    /**
     * 获取所有数据.
     *
     * @param cache  the cache
     * @param result the result
     * @param field  the field
     * @return the list
     */
    List<Map<String, Object>> get(Cache cache, Object result, Field field);
}
