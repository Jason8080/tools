package cn.gmlee.tools.mail.config;

import cn.gmlee.tools.mail.aop.SendMailAspect;
import org.springframework.context.annotation.Import;

/**
 * 声明式业务监控.
 *
 * @author Jas°
 * @date 2021/7/27 (周二)
 */
@Import(SendMailAspect.class)
public class AnnotationSendMailAutoConfiguration {
}
