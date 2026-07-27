package cn.gmlee.tools.im.conf;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "im.sse")
public class SseProperties {
    /**
     * 单 Topic 最大连接数
     */
    private int maxConnectionsPerTopic = 10000;

    /**
     * 全局最大连接数
     */
    private int maxTotalConnections = 100000;

    /**
     * Sinks.Many 缓冲区大小
     */
    private int bufferSize = 1024;
}
