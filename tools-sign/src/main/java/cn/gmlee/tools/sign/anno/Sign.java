package cn.gmlee.tools.sign.anno;

import java.lang.annotation.*;

/**
 * 签名.
 *
 * @author Jas °
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Sign {

    /**
     * 应用编号.
     *
     * @return the string
     */
    String appId() default "default";
}
