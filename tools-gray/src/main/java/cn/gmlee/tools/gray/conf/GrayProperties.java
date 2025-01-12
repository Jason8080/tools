package cn.gmlee.tools.gray.conf;

import cn.gmlee.tools.gray.mod.App;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

/**
 * Gray参数
 */
@Data
@ConfigurationProperties(prefix = "tools.gray")
public class GrayProperties implements Serializable {
    private Boolean log = false;
    private String head = "version";
    private String token = "token";
    private String version;
    private Boolean enable = true;
    private String key = "tools:gray:apps:%s:rules:custom:content";
    private Map<String, App> apps = Collections.emptyMap();
}
