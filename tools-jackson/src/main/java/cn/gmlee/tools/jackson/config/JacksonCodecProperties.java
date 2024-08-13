package cn.gmlee.tools.jackson.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

/**
 * 编解码 注册.
 */
@Data
@ConfigurationProperties(prefix = "tools.jackson")
public class JacksonCodecProperties {

    private Map<String, Properties> codec;

    @Data
    public static class Properties {
        private String publicKey;
        private String privateKey;
    }
}
