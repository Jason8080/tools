package cn.gmlee.tools.redis.anno;

import java.lang.annotation.*;

/**
 * 变量锁.
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface VariableLock {
    /**
     * 来源枚举.
     */
    enum Origin {
        HEAD, // 请求头
        QUERY, // URL
        FORM, // 表单
        ARGS, // 形参
        COOKIE, // cookie
    }

    /**
     * 名称.
     *
     * @return 不允许空
     */
    String[] value();

    /**
     * 来源.
     *
     * @return empty表示全部
     */
    Origin[] origin() default {};

    /**
     * 超时时间.
     * <p>
     *     实际上每次接口完成后会自动释放锁
     * </p>
     *
     * @return 默认1分钟
     */
    long timeout() default 60 * 1000;
}
