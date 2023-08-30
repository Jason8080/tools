package cn.gmlee.tools.profile.conf;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Collections;
import java.util.List;

/**
 * Profile参数
 */
@Data
@ConfigurationProperties(prefix = "tools.profile")
public class ProfileProperties {
    private String evn = "env";
    private String head = "version";
    private List<String> versions = Collections.singletonList("1.0.0.GRAY");
}
