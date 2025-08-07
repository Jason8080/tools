package cn.gmlee.tools.agent.conf;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Collections;
import java.util.List;

/**
 * 监控配置.
 */
@Data
@ConfigurationProperties(prefix = "tools.agent.monitor")
public class MonitorProperties {
    private Boolean enable = Boolean.TRUE;
    private List<String> packages = Collections.singletonList("*");
}
