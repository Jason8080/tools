package cn.gmlee.tools.base.anno;

import java.lang.annotation.*;

/**
 * 数据鉴权.
 *
 * @author Jas °
 * @date 2021 /10/22 (周五)
 */
@Inherited
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DataFilter {
    /**
     * The constant separator.
     */
    String separator = ",";

    /**
     * 原生句柄.
     *
     * @return the string
     */
    String sql() default "";

    /**
     * 采用的鉴权标志位唯一标识.
     * <p>
     * 指定标志位
     * </p>
     *
     * @return the string
     */
    String flag();
}
