package cn.gmlee.tools.ai.conf;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Bit Ai配置类.
 */
@Data
@ConfigurationProperties(prefix = "tools.ai.bit")
public class BitAiProperties {
    private String spaceId;
    private String agentKey;
    private String appId;
    private String apiKey;
    private String defaultModel = "ep-20250618173501-5rlnj";
}
