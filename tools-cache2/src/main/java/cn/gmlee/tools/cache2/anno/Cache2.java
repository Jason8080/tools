package cn.gmlee.tools.cache2.anno;

import java.lang.annotation.*;

/**
 * 缓存注解.
 * <p>
 *     默认配置参照{@link cn.gmlee.tools.cache2.config.Cache2Conf}
 * </p>
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Cache2 {

    /**
     * 源数据.
     * <p>
     * 也可能是API(GET)、ENUM
     * </p>
     *
     * @return 数据源 string
     */
    String target() default "";

    /**
     * 上传字段.
     *
     * <p>
     * key值与put值相等时, 匹配成功 (使用get值填充修饰字段)
     * </p>
     *
     * @return 上传字段 string
     */
    String put() default "";

    /**
     * 筛选条件(where).
     * <p>
     * 支持在当前实体内取值: ${fieldName}.
     * </p>
     *
     * @return string
     */
    String value() default "";

    /**
     * 是否开启缓存
     *
     * @return boolean
     */
    boolean enable() default true;
}
