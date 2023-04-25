package cn.gmlee.tools.base.anno;

import cn.gmlee.tools.base.kit.validator.NonEmptyValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

/**
 * 必填校验注解.
 *
 * @author Jas
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(
        validatedBy = {NonEmptyValidator.class}
)
public @interface NonEmpty {
    Group[] value() default {};

    String message() default "必填参数是空";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    @interface Group {
        Field[] value() default {};

        /**
         * 检测条件.
         * <p>
         *     存在该字段值时才会进行检测
         * </p>
         *
         * @return
         */
        String[] exist() default {};

        /**
         * 检测条件.
         * <p>
         *     不存在该字段值时才会进行检测
         * </p>
         *
         * @return
         */
        String[] nonExist() default {};
    }

    @interface Field {
        String[] value() default {};

        /**
         * 与或.
         *
         * @return
         */
        boolean and() default false;
    }
}
