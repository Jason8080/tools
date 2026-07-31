package cn.gmlee.tools.im.conf;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
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
     * 路由键配置.
     * <p>
     * 指定从 URL 参数中提取哪些字段作为路由标识（routingKey）。
     * routingKey 格式为<b>规范化查询字符串</b>：key 按字母排序，{@code key=value} 以 {@code &} 拼接。
     * </p>
     * <p>
     * <b>语义规则</b>：
     * </p>
     * <ul>
     *   <li>{@code null} / 未配置 / {@code []} — <b>全部 URL 参数参与</b>（默认）</li>
     *   <li>{@code ["*"]} — <b>显式全部参数</b>（等价于 null）</li>
     *   <li>{@code ["to"]} — <b>指定字段</b>（仅提取 to 参数）</li>
     *   <li>{@code ["tenant", "room"]} — <b>指定多字段</b></li>
     * </ul>
     * <p>
     * 无 URL 参数时为广播连接（routingKey = null），不参与定向投递索引。
     * </p>
     * <p>
     * <b>发布方与订阅方对称使用</b>：
     * </p>
     * <ul>
     *   <li><b>订阅方</b>（PULL）：从 URL 参数提取路由标识作为连接身份（如 {@code ?me=alice} → "我是 alice"）</li>
     *   <li><b>发布方</b>（PUSH）：从 URL 参数提取路由标识作为投递目标（如 {@code ?to=alice} → "发给 alice"）</li>
     * </ul>
     * <p>
     * 可通过 {@link EndpointProperties#getRoutingKeys()} 按端点覆盖此全局配置。
     * 组合逻辑由 {@link cn.gmlee.tools.im.spi.routing.RoutingKeyComposer} SPI 控制，
     * 默认实现按 key 字母排序保证参数顺序无关性。
     * </p>
     *
     * <h3>示例</h3>
     * <pre>
     * # 默认（全部参数）— 发布/订阅使用相同 URL 参数
     * # routing-keys 未配置
     * 订阅：GET /pull?me=alice&amp;room=lobby   → routingKey = "me=alice&amp;room=lobby"（身份）
     * 发布：POST /push?me=alice&amp;room=lobby  → targets = {"me=alice&amp;room=lobby"}（目标）
     *
     * # 指定字段 — 订阅方用 me 声明身份
     * routing-keys: ["me"]
     * 订阅：GET /pull?me=alice              → routingKey = "me=alice"
     *
     * # 指定字段 — 发布方用 to 指定目标
     * routing-keys: ["to"]
     * 发布：POST /push?to=alice&amp;to=bob      → targets = {"to=alice", "to=bob"}
     *
     * # 指定多字段
     * routing-keys: ["tenant", "room"]
     * 订阅：GET /pull?tenant=acme&amp;room=lobby → routingKey = "room=lobby&amp;tenant=acme"
     * 发布：POST /push?tenant=acme&amp;room=lobby → targets = {"room=lobby&amp;tenant=acme"}
     * </pre>
     */
    private List<String> routingKeys;

    /**
     * 单 Topic 最大连接数
     */
    private int maxConnectionsPerTopic = 10_000;

    /**
     * 全局最大连接数
     */
    private int maxTotalConnections = 100_000;

    /**
     * 连接最大存活时间.
     * <p>
     * 无论连接是否活跃，超过此时间的连接都会自动关闭。
     * 利用 Reactor 的 {@code take(Duration)} 操作符实现，到期后发送 onComplete 信号，
     * 触发 doFinally 清理流程，客户端的 EventSource 会自动重连。
     * </p>
     * <p>
     * 设置为 0 或负数表示禁用此功能。
     * </p>
     */
    private Duration maxConnectionLifetime = Duration.ofHours(24);

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
