package cn.gmlee.tools.mail.config;

import cn.gmlee.tools.mail.handler.ApiHandlerExceptionResolver;
import org.springframework.context.annotation.Import;

/**
 * 全局接口监控.
 *
 * @author Jas°
 * @date 2021/7/27 (周二)
 */
@Import(ApiHandlerExceptionResolver.class)
public class ApiExceptionSendMailAutoConfiguration {
}
