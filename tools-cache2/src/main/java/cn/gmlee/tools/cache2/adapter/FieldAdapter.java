package cn.gmlee.tools.cache2.adapter;


import cn.gmlee.tools.base.util.NullUtil;
import cn.gmlee.tools.cache2.anno.Cache;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

/**
 * 字段类型适配器.
 */
public interface FieldAdapter {

    /**
     * 是否适配.
     *
     * @param cache  the cache
     * @param result the result
     * @param field  the field
     * @return the boolean
     */
    boolean support(Cache cache, Object result, Field field);

    /**
     * 获取字段值.
     *
     * @param cache  the cache
     * @param result the result
     * @param field  the field
     * @param list   the list
     * @return the object
     */
    default Object get(Cache cache, Object result, Field field, List<Map<String, Object>> list){
        Map<String, Object> map = list.get(0);
        String get = NullUtil.get(cache.get(), field.getName());
        Object value = map.get(get);
        return getObject(field, list, map, value);
    }

    /**
     * Gets object.
     *
     * @param field the field
     * @param list  the list
     * @param map   the map
     * @param value the value
     * @return the object
     */
    Object getObject(Field field, List<Map<String, Object>> list, Map<String, Object> map, Object value);
}
