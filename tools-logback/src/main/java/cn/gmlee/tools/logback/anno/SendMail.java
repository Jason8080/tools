package cn.gmlee.tools.logback.anno;

import cn.gmlee.tools.base.enums.Advice;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * 发送邮件注解.
 *
 * @author Jas°
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SendMail {
    /**
     * Value advice [ ].
     *
     * @return the advice [ ]
     */
    @AliasFor("advice")
    Advice[] value() default {Advice.Throwing};

    /**
     * 通知类型
     *
     * @return string advice [ ]
     */
    @AliasFor("value")
    Advice[] advice() default {Advice.Throwing};

    /**
     * 收件人 [ ].
     *
     * @return the string [ ]
     */
    String[] recipients();
}
