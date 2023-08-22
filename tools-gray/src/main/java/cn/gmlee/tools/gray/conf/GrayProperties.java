package cn.gmlee.tools.gray.conf;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Gray参数
 */
@Data
@ConfigurationProperties(prefix = "tools.gray")
public class GrayProperties {
    private String evn = "env";
    private String head = "TOOLS-GRAY";
    private String version = "1.0.0";
}
