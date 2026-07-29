package cn.gmlee.tools.im.conf;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * SSE 连接配置属性.
 * <p>
 * 配置前缀：{@code im.sse}
 * </p>
 */
@Data
@ConfigurationProperties(prefix = "im.sse")
public class SseProperties {

    /**
     * 单 Topic 最大连接数
     */
    private int maxConnectionsPerTopic = 10_000;

    /**
     * 全局最大连接数
     */
    private int maxTotalConnections = 100_000;

    /**
     * 背压策略配置
     */
    private BackpressureConfig backpressure = new BackpressureConfig();

    /**
     * 收割器配置
     */
    private ReaperConfig reaper = new ReaperConfig();

    /**
     * 优雅关闭配置
     */
    private ShutdownConfig shutdown = new ShutdownConfig();

    /**
     * 指标配置
     */
    private MetricsConfig metrics = new MetricsConfig();

    /**
     * 清理配置
     */
    private CleanupConfig cleanup = new CleanupConfig();

    /**
     * 心跳配置
     */
    private HeartbeatConfig heartbeat = new HeartbeatConfig();

    /**
     * 背压策略配置
     */
    @Data
    public static class BackpressureConfig {
        /**
         * 默认策略名称：buffer / drop-oldest / error
         */
        private String defaultStrategy = "drop-oldest";

        /**
         * 默认缓冲区大小
         */
        private int defaultBufferSize = 1024;

        /**
         * 按 Topic 覆盖策略（key=topic, value=strategyName）
         */
        private Map<String, String> topicOverrides = new HashMap<>();

        /**
         * 按 Topic 覆盖缓冲区大小（key=topic, value=bufferSize）
         */
        private Map<String, Integer> topicBufferOverrides = new HashMap<>();
    }

    /**
     * 收割器配置.
     * <p>
     * 收割器作为 doFinally 的兜底机制，清理未能正常关闭的连接。
     * 连接空闲超时检测基于最后活跃时间（数据收发时间）。
     * </p>
     */
    @Data
    public static class ReaperConfig {
        /**
         * 是否启用收割器
         */
        private boolean enabled = true;

        /**
         * 扫描间隔
         */
        private Duration interval = Duration.ofSeconds(30);

        /**
         * 空闲超时阈值（超过此时间无数据活动视为僵尸连接）
         */
        private Duration idleTimeout = Duration.ofSeconds(3600);

        /**
         * 僵尸连接强制关闭前的宽限期
         */
        private Duration gracePeriod = Duration.ofSeconds(5);
    }

    /**
     * 优雅关闭配置
     */
    @Data
    public static class ShutdownConfig {
        /**
         * 排空超时（等待连接自然关闭的最长时间）
         */
        private Duration drainTimeout = Duration.ofSeconds(30);

        /**
         * 关闭时是否发送告别消息
         */
        private boolean sendGoodbye = true;

        /**
         * 关闭时是否拒绝新连接
         */
        private boolean rejectNew = true;
    }

    /**
     * 指标配置
     */
    @Data
    public static class MetricsConfig {
        /**
         * 是否启用指标收集
         */
        private boolean enabled = true;

        /**
         * 指标前缀
         */
        private String prefix = "im.sse";
    }

    /**
     * 清理配置
     */
    @Data
    public static class CleanupConfig {
        /**
         * 空 Topic 保留时间（防止抖动）
         */
        private Duration emptyTopicTtl = Duration.ofSeconds(60);

        /**
         * Topic 计数器压缩 TTL.
         * <p>
         * 空 Topic 的 AtomicInteger 计数器条目在内存中保留此时间后，
         * 将被压缩移除以回收内存。设置为 0 或负数表示禁用压缩。
         * </p>
         * <p>
         * <b>权衡</b>：移除条目会与并发 subscribe() 的 computeIfAbsent
         * 产生极小的竞态窗口（见 {@link SseConnectionRegistry#compactTopicCounts}），
         * 可能导致计数器偏差 1（软限制，影响可忽略）。
         * 默认 1 小时，适用于动态高基数 Topic 场景。静态 Topic 场景可设大或禁用。
         * </p>
         */
        private Duration compactTtl = Duration.ofHours(1);
    }

    /**
     * 心跳配置.
     * <p>
     * 定期发送 SSE 注释（comment）保持连接活性，防止反向代理
     * （Nginx、ALB 等）因空闲超时而断开 SSE 连接。
     * </p>
     * <p>
     * 心跳以 SSE 注释形式发送（以 {@code :} 开头的行），
     * 浏览器的 EventSource API 会忽略注释，不会触发 onmessage 回调。
     * </p>
     */
    @Data
    public static class HeartbeatConfig {
        /**
         * 是否启用心跳
         */
        private boolean enabled = true;

        /**
         * 心跳发送间隔
         */
        private Duration interval = Duration.ofSeconds(30);

        /**
         * 心跳注释内容（SSE comment）
         */
        private String comment = "heartbeat";
    }
}
