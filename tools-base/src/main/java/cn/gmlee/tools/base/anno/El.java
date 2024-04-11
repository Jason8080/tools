package cn.gmlee.tools.base.anno;

import javax.validation.Payload;
import java.lang.annotation.*;

/**
 * El：表达式
 * <p>起源于JSP未来也将支持这种的写法： this，对象引用，上下文读取； 但目前处于初期阶段咱无支持</p>
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface El {
    /**
     * 条件
     *
     * @return
     */
    String[] conditions() default {};

    /**
     * 表达式
     * <p>
     * 注意: 表达式的执行结果为下列场景时将被忽略
     * ① 执行异常
     * ② 返回结果是null
     * ③ 返回结果是非布尔值
     * ④ 条件执行结果不是true
     * </p>
     *
     * @return
     */
    String[] value() default {};

    String message() default "验证不通过";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}