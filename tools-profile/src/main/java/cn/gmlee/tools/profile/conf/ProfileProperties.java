package cn.gmlee.tools.profile.conf;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Profile参数
 */
@Data
@ConfigurationProperties(prefix = ProfileProperties.PREFIX)
public class ProfileProperties {
    public static final String PREFIX = "tools.profile";
    private Boolean open = Boolean.TRUE;
    private String field = "env";
    private List<String> uid = Collections.emptyList();
}
