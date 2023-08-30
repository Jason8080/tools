package cn.gmlee.tools.gray.conf;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Gray参数
 */
@Data
@ConfigurationProperties(prefix = "tools.gray")
public class GrayProperties {
    private String head = "version";
    private String version = "1.0.0.GRAY";
}
