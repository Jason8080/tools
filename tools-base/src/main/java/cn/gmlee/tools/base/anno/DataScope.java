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
     * @return flag 标志位
     */
    @AliasFor("row")
    String[] value() default {};

    /**
     * 行鉴权.
     *
     * @return flag 标志位
     */
    @AliasFor("value")
    String[] row() default {};

    /**
     * 列鉴权.
     *
     * @return flag 标志位
     */
    String[] col() default {};
}
