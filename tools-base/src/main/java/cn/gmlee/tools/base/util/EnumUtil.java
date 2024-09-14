package cn.gmlee.tools.base.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.Objects;

/**
 * 枚举类工具类
 *
 * @author Jas°
 */
public class EnumUtil {

    private static final Logger logger = LoggerFactory.getLogger(EnumUtil.class);

    /**
     * 获取静态
     *
     * @param name
     * @param enumClass
     * @return
     */
    public static <E extends Enum> E name(Object name, Class<E> enumClass) {
        try {
            Field[] fields = enumClass.getDeclaredFields();
            for (Field field : fields) {
                String fieldName = name != null ? name.toString() : null;
                if (BoolUtil.equalsIgnoreCase(field.getName(), fieldName)) {
                    return (E) ClassUtil.getValue(enumClass, field);
                }
            }
        } catch (Exception e) {
            logger.error("枚举名不存在", e);
        }
        return null;
    }

    /**
     * 根据第一个自定义常量值获取枚举
     *
     * @param value
     * @param enumClass
     * @param <E>
     * @return
     */
    public static <E extends Enum> E value(Object value, Class<E> enumClass) {
        try {
            Field[] fields = enumClass.getDeclaredFields();
            for (Field field : fields) {
                if (!field.isEnumConstant() && !field.isSynthetic()) {
                    for (E e : enumClass.getEnumConstants()) {
                        Object o = ClassUtil.getValue(e, field);
                        if (Objects.equals(o, value)) {
                            return e;
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("枚举值不存在", e);
        }
        return null;
    }

    /**
     * 根据指定常量名和常量值获取枚举
     *
     * @param name
     * @param value
     * @param enumClass
     * @param <E>
     * @return
     */
    public static <E extends Enum> E value(String name, Object value, Class<E> enumClass) {
        try {
            Field[] fields = enumClass.getDeclaredFields();
            for (Field field : fields) {
                String fieldName = field.getName();
                if (fieldName.equalsIgnoreCase(name)) {
                    if (!field.isEnumConstant() && !field.isSynthetic()) {
                        for (E e : enumClass.getEnumConstants()) {
                            Object o = ClassUtil.getValue(e, field);
                            if (Objects.equals(o, value)) {
                                return e;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("枚举不存在", e);
        }
        return null;
    }
}
