package cn.gmlee.tools.base.anno;

import cn.gmlee.tools.base.enums.Mark;
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
     * Mark mark.
     *
     * @return the mark
     */
    Mark mark() default Mark.NON;

    /**
     * 是否序列化.
     * <p>
     * 优先级高于dateFormat (当此项启用则dateFormat失效)
     * </p>
     *
     * @return the boolean
     */
    Class<?> serializer() default Serializer.class;

    /**
     * 日期格式化.
     *
     * @return the x time
     */
    XTime dateFormat() default XTime.SECOND_MINUS_BLANK_COLON;


    /**
     * 序列化接口 (默认值: 表示不进行序列化).
     */
    interface Serializer {}

    /**
     * 序列化成JSON.
     */
    class JsonSerializer implements Serializer {}
}
