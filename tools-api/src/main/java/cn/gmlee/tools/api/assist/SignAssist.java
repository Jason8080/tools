package cn.gmlee.tools.api.assist;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 签名辅助工具
 *
 * @author Jas°
 * @date 2021/1/26 (周二)
 */
@Data
@Component("apiSignAssist")
public class SignAssist {
    @Value("#{${tools.api.sign.appKeys:{}}}")
    private Map<String, String> appKeys = new HashMap();

    @Value("${tools.api.sign.nonceSecond:3}")
    private Integer nonceSecond;

    @Value("${tools.api.sign.extraHeaders:#{null}}")
    private String[] extraHeaders;
}
