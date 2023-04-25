package cn.gmlee.tools.base.anno;

import cn.gmlee.tools.base.kit.validator.EnumsValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

/**
 * 枚举校验注解.
 *
 * @author Jas
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(
        validatedBy = {EnumsValidator.class}
)
public @interface Enums {
    String separator = ",";

    String value() default "";

    Class<? extends Enum>[] enums() default {};

    boolean required() default false;

    String message() default "不在枚举范围";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
