package cn.gmlee.tools.cache2.adapter;

import cn.gmlee.tools.base.util.BoolUtil;
import cn.gmlee.tools.cache2.anno.Cache;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 字段类型适配器.
 */
public class BaseFieldAdapter implements FieldAdapter {

    @Override
    public boolean support(Cache cache, Object result, Field field) {
        return BoolUtil.isBaseClass(field.getType(), String.class, BigDecimal.class);
    }

    @Override
    public Object getObject(Field field, List<Map<String, Object>> list, Map<String, Object> map, Object value) {
        // 目标类型
        Class<?> target = field.getType();
        // 值类型
        Class<?> source = value != null ? value.getClass() : null;
        if (BoolUtil.allSubclass(target, source)) {
            return value;
        }
        // 如果是金额类型
        if(target.equals(BigDecimal.class) && BoolUtil.allSubclass(Number.class, source)) {
            return value instanceof BigDecimal ? value : new BigDecimal(value.toString());
        }
        return null;
    }
}
