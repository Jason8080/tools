package cn.gmlee.tools.sign.conf;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Collections;
import java.util.Map;

@Data
@ConfigurationProperties(prefix = "ldw.sign")
public class LdwSignProperties {
    private Map<String, String> app = Collections.emptyMap();
}
