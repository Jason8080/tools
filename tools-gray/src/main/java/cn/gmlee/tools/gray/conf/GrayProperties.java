package cn.gmlee.tools.gray.conf;

import cn.gmlee.tools.gray.mod.Rule;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Gray参数
 */
@Data
@ConfigurationProperties(prefix = "tools.gray")
public class GrayProperties implements Serializable {
    private Boolean enable = true;
    private String head = "version";
    private String token = "token";
    private String version = "1.0.0";
    private List<String> versions = Collections.emptyList();
    private Map<String, Rule> rules = Collections.emptyMap();
}
