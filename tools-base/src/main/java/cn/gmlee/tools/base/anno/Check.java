package cn.gmlee.tools.base.anno;

import cn.gmlee.tools.base.kit.validator.CheckValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

/**
 * 参数校验注解.
 *
 * @author Jas
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(
        validatedBy = {CheckValidator.class}
)
public @interface Check {
    El[] value() default {};

    String message() default "参数检查不通过";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
