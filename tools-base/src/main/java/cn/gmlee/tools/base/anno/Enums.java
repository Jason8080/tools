package cn.gmlee.tools.base.anno;

import cn.gmlee.tools.base.kit.validator.EnumsValidator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * 枚举校验注解.
 *
 * @author Jas
 */
@Documented
@Constraint(
        validatedBy = {EnumsValidator.class}
)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER})
public @interface Enums {
    String separator = ",";

    String value() default "";

    Class<?>[] enums() default {};

    boolean required() default false;

    String message() default "不在枚举范围";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
