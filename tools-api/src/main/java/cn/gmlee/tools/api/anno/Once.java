package cn.gmlee.tools.api.anno;

import java.lang.annotation.*;

/**
 * 防重放注解
 *
 * <p>
 * 不允许在指定范围内, 重复访问
 * </p>
 *
 * @author Jas°
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Once {
    /**
     * 指定多久才能访问一次.
     * <p>
     *     认定为重复请求的标准: MD5值一致则视为重复.
     *     MD5计算办法: MD5(请求头JSON_该注解修饰的方法接收的所有参数JSON)
     * </p>
     */
    long value() default 3000;
}
