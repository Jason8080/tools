package cn.gmlee.tools.gray.conf;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Mock参数
 */
@Data
@ConfigurationProperties(prefix = "tools.gray")
public class GrayProperties {
    private String evn = "env";
}
