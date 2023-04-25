package cn.gmlee.tools.webapp.assist;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * 签名辅助工具
 *
 * @author Jas°
 * @date 2021/1/26 (周二)
 */
@Data
@Component("webAppSignAssist")
public class SignAssist {
    @Value("${tools.webapp.sign.appKeyPrefix:REDIS:SIGN:APP_KEY_}")
    private String appKeyPrefix;

    @Value("${tools.webapp.sign.noncePrefix:REDIS:SIGN:IDEM_NONCE_}")
    private String idemNoncePrefix;

    @Value("${tools.webapp.sign.nonceSecond:3}")
    private Integer nonceSecond;

    @Value("${tools.webapp.sign.extraHeaders:#{null}}")
    private String[] extraHeaders;
}
