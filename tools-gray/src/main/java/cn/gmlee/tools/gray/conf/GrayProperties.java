package cn.gmlee.tools.gray.conf;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Collections;
import java.util.List;

/**
 * Gray参数
 */
@Data
@ConfigurationProperties(prefix = "tools.gray")
public class GrayProperties {
    private Boolean enable = Boolean.TRUE;
    private String head = "version";
    private String token = "token";
    private String version = "1.0.0";
    private List<String> versions = Collections.emptyList();
}
