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
    protected Long period = 1000L;
    protected Long initialDelay = 1000L;
    protected Long maxSurvival = 60*1000L;
    protected String mode = "AOP";
    protected List<String> type = new ArrayList<>();
    protected List<String> ignore = new ArrayList<>();
}
