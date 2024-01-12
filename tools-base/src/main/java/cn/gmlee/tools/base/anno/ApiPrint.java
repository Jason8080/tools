package cn.gmlee.tools.base.anno;

import java.lang.annotation.*;

/**
 * 日志打印注解.
 * <p>api方法描述.</p>
 *
 * @author Jas °
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiPrint {
    /**
     * Api 功能描述
     *
     * @return string string
     */
    String value() default "";

    /**
     * Api 功能类型.
     * <p>
     *     -1常规, 0组织, 1用户, 2角色, 3菜单, 4可见性, 5数据权限, 6登录
     * </p>
     *
     * @return the int
     */
    int type() default -1;

    /**
     * Length integer.
     *
     * @return the integer
     */
    int length() default -1;
}
