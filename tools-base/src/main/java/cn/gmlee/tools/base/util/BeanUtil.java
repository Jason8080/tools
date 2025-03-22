package cn.gmlee.tools.base.util;

import cn.gmlee.tools.base.enums.XCode;
import cn.gmlee.tools.base.ex.SkillException;
import org.springframework.beans.BeanUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 通用对象转换工具类
 *
 * @param <T> the type parameter
 * @author Jas °
 * @date 2020 /9/21 (周一)
 */
public class BeanUtil<T> {
    private T target;

    /**
     * Instantiates a new Bean util.
     *
     * @param source the source
     * @param clazz  the clazz
     */
    public BeanUtil(Object source, Class<T> clazz) {
        try {
            T target = clazz.getConstructor().newInstance();
            convert(source, target);
            this.target = target;
        } catch (Exception e) {
            throw new SkillException(XCode.THIRD_PARTY_FAIL.code, e);
        }
    }

    /**
     * Instantiates a new Bean util.
     *
     * @param source the source
     * @param target the target
     */
    public BeanUtil(Object source, T target) {
        convert(source, target);
        this.target = target;
    }

    /**
     * Set t.
     *
     * @param name  the name
     * @param value the value
     * @return the t
     */
    public boolean set(String name, Object value) {
        return set(target, name, value);
    }

    /**
     * Set t.
     *
     * @param target the target
     * @param name   the name
     * @param value  the value
     * @return the t
     */
    public static boolean set(Object target, String name, Object value) {
        if (target == null) {
            return false;
        }
        try {
            // 根据JavaBean对象获取对应的字节码对象
            Class clazz = target.getClass();
            // 根据字节码对象获取对应的Field对象
            Field f = clazz.getDeclaredField(name);
            // 重新赋值
            ClassUtil.setValue(target, f, value);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Convert t.
     *
     * @param <T>    the type parameter
     * @param source the source
     * @param clazz  the clazz
     * @return the t
     */
    public static <T> T convert(Map<String, Object> source, Class<T> clazz) {
        return convert(source, clazz, false);
    }

    /**
     * Convert list.
     *
     * @param <T>   the type parameter
     * @param clazz the clazz
     * @return the list
     */
    public static <C extends Collection, T> C convert(C c, Class<T> clazz) {
        if(BoolUtil.isEmpty(c)){
            return c;
        }
        return (C) c.stream().map(item -> convert(item, clazz)).collect(Collectors.toList());
    }

    /**
     * Convert t.
     *
     * @param <T>    the type parameter
     * @param source the source
     * @param clazz  the clazz
     * @param hump   the hump
     * @return the t
     */
    public static <T> T convert(Map<String, Object> source, Class<T> clazz, boolean hump) {
        try {
            Constructor<T> constructor = clazz.getConstructor();
            T t = constructor.newInstance();
            if (BoolUtil.isEmpty(source)) {
                return t;
            }
            return convert(source, t, hump);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Convert t.
     *
     * @param <T>    the type parameter
     * @param source the source
     * @param target the target
     * @param hump   the hump
     * @return the t
     */
    public static <T> T convert(Map<String, Object> source, T target, boolean hump) {
        return convert(source, target, hump, false);
    }

    /**
     * Convert target.
     *
     * @param <T>    the type parameter
     * @param source the source
     * @param target the target
     * @param hump   the hump
     * @param cover  是否覆盖目标对象的值
     * @return the target
     */
    public static <T> T convert(Map<String, Object> source, T target, boolean hump, boolean cover) {
        Map<String, Field> fields = ClassUtil.getFieldsMap(target);
        Iterator<Map.Entry<String, Field>> it = fields.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Field> next = it.next();
            String key = QuickUtil.is(hump, () -> next.getKey(), () -> HumpUtil.hump2underline(next.getKey()));
            Field field = next.getValue();
            Object val = source.get(key);
            if (val == null) {
                continue;
            }
            if (!BoolUtil.isParentClass(field.getType(), val.getClass())) {
                val = convert(val, field.getType());
            }
            if (!cover) {
                Object value = ClassUtil.getValue(target, field);
                if (value == null) {
                    ClassUtil.setValue(target, field, val);
                }
            } else {
                ClassUtil.setValue(target, field, val);
            }
        }
        return target;
    }

    /**
     * Convert t.
     *
     * @param <T>    the type parameter
     * @param source the source
     * @param clazz  the clazz
     * @return the t
     */
    public static <T> T convert(Object source, Class<T> clazz) {
        if (source != null) {
            try {
                T t;
                Constructor<T> constructor = ExceptionUtil.sandbox(() -> clazz.getConstructor(), false);
                if (constructor != null) {
                    t = constructor.newInstance();
                } else {
                    Constructor<T> c = ExceptionUtil.sandbox(() -> clazz.getConstructor(source.getClass()), false);
                    t = c.newInstance(source);
                }
                BeanUtils.copyProperties(source, t);
                return t;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * Convert.
     *
     * @param <T>    the type parameter
     * @param source the source
     * @param target the target
     */
    public static <T> void convert(Object source, Object target) {
        if (source != null) {
            BeanUtils.copyProperties(source, target);
        }
    }
}
