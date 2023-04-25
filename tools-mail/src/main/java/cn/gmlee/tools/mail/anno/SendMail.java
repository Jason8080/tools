package cn.gmlee.tools.mail.anno;

import cn.gmlee.tools.base.enums.Advice;

import java.lang.annotation.*;

/**
 * 发送邮件注解.
 *
 * @author Jas °
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SendMail {
    /**
     * 监控内容/作者姓名.
     * <p>
     * 它将在邮件标题【】中高亮展示
     * </p>
     *
     * @return the advice [ ]
     */
    String value() default "业务监控";

    /**
     * 通知类型
     *
     * @return string advice [ ]
     */
    Advice[] advice() default {Advice.Throwing};

    /**
     * 收件人 [ ].
     *
     * @return the string [ ]
     */
    String[] recipients();
}
