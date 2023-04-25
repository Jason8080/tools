package cn.gmlee.tools.base.anno;

import cn.gmlee.tools.base.enums.XTime;

import java.lang.annotation.*;

/**
 * 列/字段描述注解.
 * <p>
 * 常用于Excel导出.
 * </p>
 */
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Column {
    /**
     * 描述.
     *
     * @return the string
     */
    String value() default "";

    /**
     * 是否隐藏.
     *
     * @return the boolean
     */
    boolean hide() default false;

    /**
     * 是否使用JSON序列化.
     * <p>
     *     优先级高于dateFormat (当此项启用则dateFormat失效)
     * </p>
     *
     * @return the boolean
     */
    boolean serializer() default false;

    /**
     * 日期格式化.
     *
     * @return the x time
     */
    XTime dateFormat() default XTime.SECOND_MINUS_BLANK_COLON;
}
