package cn.gmlee.tools.cache2.adapter;

import cn.gmlee.tools.cache2.anno.Cache;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 字段类型适配器.
 */
@Component
public class CollectionFieldAdapter implements FieldAdapter {

    @Override
    public boolean support(Cache cache, Object result, Field field) {
        return Collection.class.isAssignableFrom(field.getType());
    }

    @Override
    public Object getObject(Field field, List<Map<String, Object>> list, Map<String, Object> map, Object value) {
        return list;
    }
}
