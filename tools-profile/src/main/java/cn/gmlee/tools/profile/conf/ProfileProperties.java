package cn.gmlee.tools.profile.conf;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Profile参数
 */
@Data
@ConfigurationProperties(prefix = ProfileProperties.PREFIX)
public class ProfileProperties {
    public static final String PREFIX = "tools.profile";
    private Boolean open = Boolean.FALSE;
    private Boolean print = Boolean.FALSE;
    private String field = "env";
}
