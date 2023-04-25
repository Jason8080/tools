package cn.gmlee.tools.base.anno;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * 鉴权注解.
 *
 * @author: Jas °
 * @Description 鉴权注解
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataScope {
    /**
     * 行权限.
     *
     * @return the data filter [ ]
     */
    @AliasFor("row")
    DataFilter[] value() default {};

    /**
     * 行鉴权.
     *
     * @return the data auth
     */
    @AliasFor("value")
    DataFilter[] row() default {};

    /**
     * 列鉴权.
     *
     * @return the data auth
     */
    DataFilter[] col() default {};
}
