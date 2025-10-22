package cn.gmlee.tools.third.party.lark.conf;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "tools.lark")
public class FeiShuProperties {
    private String appId;
    private String appSecret;
}
