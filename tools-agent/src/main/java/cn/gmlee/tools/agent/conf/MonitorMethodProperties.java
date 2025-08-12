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
    protected Boolean enable = Boolean.TRUE;
    protected Integer period = 1000;
    protected Integer initialDelay = 1000;
    protected List<String> packages = new ArrayList<>();
    protected List<String> ignorePackages = new ArrayList<>();
}
