package cn.gmlee.tools.im.sse;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * SSE 配置属性
 *
 * @author SseIm Framework
 * @since 1.0.0
 */
@Data
@ConfigurationProperties(prefix = "sse-im.sse")
public class SseProperties {

    /**
     * 是否启用 SSE
     */
    private boolean enabled = true;

    /**
     * SSE 基础路径
     */
    private String basePath = "/sse";

    /**
     * 心跳间隔
     */
    private Duration heartbeatInterval = Duration.ofSeconds(15);

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

    /**
     * 连接超时（0 表示永不超时）
     */
    private Duration connectionTimeout = Duration.ZERO;

    /**
     * 是否启用 Last-Event-ID 回放
     */
    private boolean replayEnabled = true;

    /**
     * 最大回放事件数
     */
    private int maxReplayEvents = 200;

    /**
     * SSE retry 指令（毫秒）
     */
    private int retryMillis = 5000;
}
