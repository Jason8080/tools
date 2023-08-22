package cn.gmlee.tools.prevent.annotation;

import java.lang.annotation.*;

/**
 * @desc: 接口限流注解
 *
 * @author: James
 * @date: 2023/7/27 18:44
 */
@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Limit {

    /**
     * @return KEY 接口防刷KEY（默认接口URI）
     */
    String key() default "";

    /**
     * @return KEY 接口防刷KEY参数（完整的KEY包括 key + keyParams）注意：值不能为null
     */
    String[] keyParams() default "";

    /**
     * 例如 count = 60    timeWindow = 60000     表示60000毫秒内允许访问60次
     */

    /**
     * 限制次数
     * @return
     */
    long count() default 60;

    /**
     * 时间窗口大小(毫秒)
     * @return
     */
    long timeWindow() default 60000;

}
