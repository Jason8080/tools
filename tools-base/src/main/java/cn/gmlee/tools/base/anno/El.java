package cn.gmlee.tools.base.anno;

import javax.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.FIELD})
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