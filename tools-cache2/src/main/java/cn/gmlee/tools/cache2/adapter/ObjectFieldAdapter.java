package cn.gmlee.tools.cache2.adapter;

import cn.gmlee.tools.base.util.BeanUtil;
import cn.gmlee.tools.base.util.BoolUtil;
import cn.gmlee.tools.cache2.anno.Cache;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 字段类型适配器.
 */
public class ObjectFieldAdapter implements FieldAdapter {

    @Override
    public boolean support(Cache cache, Object result, Field field) {
        return BoolUtil.isBean(field.getType(), String.class, BigDecimal.class);
    }

    @Override
    public Object getObject(Field field, List<Map<String, Object>> list, Map<String, Object> map, Object value) {
        return BeanUtil.convert(map, field.getType(), false);
    }
}
