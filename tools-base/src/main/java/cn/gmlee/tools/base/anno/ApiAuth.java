package cn.gmlee.tools.base.anno;

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
public @interface ApiAuth {
}
