package cn.gmlee.tools.jackson.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 编解码 注册.
 */
@Data
@ConfigurationProperties(prefix = "tools.jackson.codec")
public class JacksonCodecProperties {
    private String publicKey;
    private String privateKey;
}
