package cn.gmlee.tools.cache2.anno;

import java.lang.annotation.*;

/**
 * 缓存注解.
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Cache2 {
    /**
     * 筛选条件.
     * <p>
     * 支持在当前实体内取值: ${fieldName}.
     * </p>
     *
     * @return string
     */
    String value() default "";

    /**
     * 源数据.
     * <p>
     * 也可能是API(GET)
     * </p>
     *
     * @return 数据源 string
     */
    String table() default "";
}
