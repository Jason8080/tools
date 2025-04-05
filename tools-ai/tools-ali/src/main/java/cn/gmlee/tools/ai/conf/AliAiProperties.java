package cn.gmlee.tools.ai.conf;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "tools.ai.ali")
public class AliAiProperties {
    private String spaceId = "llm-tr5o0shrj1159vlm";
    private String agentKey = "00d39a41238d44faa30f5213360ebeca_p_efm";
    private String apiKey = "sk-7793b148079149f887608357e7f5b2e0";
    private String aliModel = "qwen-omni-turbo";
}
