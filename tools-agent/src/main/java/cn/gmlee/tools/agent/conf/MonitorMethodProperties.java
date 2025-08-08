package cn.gmlee.tools.agent.conf;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * 监控配置.
 */
@Data
@ConfigurationProperties(prefix = "tools.agent.monitor.method")
public class MonitorMethodProperties {
    private Boolean enable = Boolean.TRUE;
    private List<String> packages = new ArrayList<>();
    private List<String> ignorePackages = new ArrayList<>();
}
