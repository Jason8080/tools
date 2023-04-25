package cn.gmlee.tools.datalog.anno;

import java.lang.annotation.*;

/**
 * <p>
 * 忽略日志记录注解
 * 使用该注解的表或字段将不进行修改记录
 * </p>
 *
 * @author Jas °
 * @since 2021 /1/1
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD})
public @interface Note {
    /**
     * Value string.
     *
     * @return the string
     */
    String value() default "";
}
