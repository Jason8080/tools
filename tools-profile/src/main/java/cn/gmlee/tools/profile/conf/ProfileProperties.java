package cn.gmlee.tools.profile.conf;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Profile参数
 */
@Data
@ConfigurationProperties(prefix = "tools.profile")
public class ProfileProperties {
    private String evn = "env";
    private Boolean enable = Boolean.TRUE;
}
