package cn.gmlee.tools.agent.conf;


import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 监控配置.
 */
@Data
public class MonitorMethodProperties {
    protected Boolean enable = Boolean.TRUE;
    protected Integer period = 1000;
    protected Integer initialDelay = 1000;
    protected Integer maxSurvival = 60*60*1000;
    protected List<String> type = new ArrayList<>();
    protected List<String> ignore = new ArrayList<>();
}
