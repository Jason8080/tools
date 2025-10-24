package cn.gmlee.tools.third.party.lark.conf;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "tools.lark")
public class FeiShuProperties {
    private String appId = System.getProperty("tools.lark.appId", "cli_a8624fd47914100c");
    private String appSecret = System.getProperty("tools.lark.appSecret", "6Pb5IJgpIt92anHV2Yng4baMycVjuRZP");
}
