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
     *   <li>{@code ["room"]} — <b>指定字段</b>（仅提取 room 参数）</li>
     *   <li>{@code ["tenant", "room"]} — <b>指定多字段</b>（如多租户聊天室）</li>
     * </ul>
     * <p>
     * 无 URL 参数时为广播连接（routingKey = null），不参与定向投递索引。
     * </p>
     * <p>
     * <b>发布方与订阅方对称使用</b>：
     * </p>
     * <ul>
     *   <li><b>订阅方</b>（PULL）：从 URL 参数提取路由标识作为连接身份（如 {@code ?tenant=acme&room=lobby} → "我是 acme/lobby 的订阅者"）</li>
     *   <li><b>发布方</b>（PUSH）：从 URL 参数提取路由标识作为投递目标（如 {@code ?tenant=acme&room=lobby} → "发给 acme/lobby"）</li>
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
     * 订阅：GET /pull?tenant=acme&amp;room=lobby   → routingKey = "room=lobby&amp;tenant=acme"（身份）
     * 发布：POST /push?tenant=acme&amp;room=lobby  → targets = {"room=lobby&amp;tenant=acme"}（目标）
     *
     * # 指定单字段 — 仅按房间路由
     * routing-keys: ["room"]
     * 订阅：GET /pull?room=lobby               → routingKey = "room=lobby"
     * 发布：POST /push?room=lobby&amp;room=main    → targets = {"room=lobby", "room=main"}
     *
     * # 指定多字段 — 多租户聊天室（tenant + room）
     * routing-keys: ["tenant", "room"]
     * 订阅：GET /pull?tenant=acme&amp;room=lobby   → routingKey = "room=lobby&amp;tenant=acme"
     * 发布：POST /push?tenant=acme&amp;room=lobby  → targets = {"room=lobby&amp;tenant=acme"}
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
     * 消息发射重试配置
     */
    private EmitRetryConfig emitRetry = new EmitRetryConfig();

    /**
     * 断点续传配置（SSE {@code id:} + {@code Last-Event-ID}）
     *
     * @since 5.7.0
     */
    private ResumeConfig resume = new ResumeConfig();

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

    /**
     * 消息发射重试配置.
     * <p>
     * 控制多线程并发发布时的重试策略。
     * Reactor Sinks 的 {@code tryEmitNext()} 使用 CAS 序列化，
     * 多线程竞争时可能返回 {@code FAIL_NON_SERIALIZED}。
     * </p>
     * <p>
     * 提供四种重试策略：
     * </p>
     * <ul>
     *   <li><b>no-retry</b>：单次尝试，失败立即返回（默认，性能最优）</li>
     *   <li><b>busy-loop</b>：忙等待重试直到成功或超时（可靠性最高）</li>
     *   <li><b>bounded</b>：固定次数重试（平衡可靠性和资源）</li>
     *   <li><b>exponential-backoff</b>：指数退避重试（适合高并发）</li>
     * </ul>
     *
     * @since 5.6.0
     */
    @Data
    public static class EmitRetryConfig {
        /**
         * 默认重试策略：no-retry / busy-loop / bounded / exponential-backoff
         */
        private String defaultStrategy = "no-retry";

        /**
         * busy-loop 策略超时时间
         */
        private Duration busyLoopTimeout = Duration.ofMillis(100);

        /**
         * bounded 策略最大重试次数
         */
        private int boundedMaxRetries = 3;

        /**
         * bounded 策略重试间隔
         */
        private Duration boundedRetryInterval = Duration.ofMillis(10);

        /**
         * exponential-backoff 策略最大重试次数
         */
        private int exponentialMaxRetries = 5;

        /**
         * exponential-backoff 策略初始间隔
         */
        private Duration exponentialInitialInterval = Duration.ofMillis(10);

        /**
         * exponential-backoff 策略乘数
         */
        private double exponentialMultiplier = 2.0;

        /**
         * exponential-backoff 策略最大间隔
         */
        private Duration exponentialMaxInterval = Duration.ofMillis(1000);

        /**
         * 按 Topic 覆盖策略（key=topic, value=strategyName）
         */
        private Map<String, String> topicOverrides = new HashMap<>();
    }

    /**
     * 断点续传配置（v5.7.0）.
     * <p>
     * 控制 SSE {@code id:} 字段下发与 {@code Last-Event-ID} 重连回放。
     * 完整机制见 {@link cn.gmlee.tools.im.resume.ResumeSupport}。
     * </p>
     *
     * <h3>配置示例</h3>
     * <pre>
     * im:
     *   sse:
     *     resume:
     *       enabled: true                    # 续传总开关（需注册 MessageHistoryStore Bean）
     *       emit-id: true                    # 每条消息下发 SSE id: 字段
     *       retry-advice: 3s                 # 可选：下发 retry: 字段建议客户端重连间隔
     *       max-replay: 500                  # 单次重连最多回放条数（超限发 resync）
     *       max-pending: 1024                # 回放期间实时消息待缓冲上限（溢出发 resync）
     *       replay-timeout: 5s               # 历史回放总时长预算（超时降级 + resync；0 = 不限制）
     *       snowflake:
     *         enabled: false                 # CLUSTER 部署必须开启（全局有序 ID）
     *         worker-id: -1                  # -1 = 按主机名哈希自动分配
     *       in-memory:
     *         enabled: false                 # 内存历史存储（开发/单机）
     *         capacity-per-topic: 1000
     * </pre>
     *
     * @since 5.7.0
     */
    @Data
    public static class ResumeConfig {
        /**
         * 续传读取侧总开关.
         * <p>
         * 关闭后即使客户端携带 Last-Event-ID 也仅返回实时流。
         * 写入侧（{@code MessageHistoryStore.store}）由是否注册存储 Bean 决定，不受此开关影响。
         * </p>
         */
        private boolean enabled = true;

        /**
         * 是否下发 SSE {@code id:} 字段.
         * <p>
         * 关闭可节省少量带宽，但客户端将失去续传能力（无法上报位点）。
         * </p>
         */
        private boolean emitId = true;

        /**
         * 客户端重连间隔建议（下发为 SSE {@code retry:} 字段）.
         * <p>
         * null 表示不下发（浏览器 EventSource 默认约 3s）。
         * </p>
         */
        private Duration retryAdvice;

        /**
         * 单次重连最大回放条数.
         * <p>
         * 超出时停止回放并向客户端发送 {@code event: resync} 信号（提示全量刷新）。
         * </p>
         */
        private int maxReplay = 500;

        /**
         * 回放期间实时消息待缓冲队列上限.
         * <p>
         * 溢出时发送 {@code event: resync} 并丢弃积压（续接不中断）。
         * </p>
         */
        private int maxPending = 1024;

        /**
         * 历史回放超时（总时长预算）.
         * <p>
         * 限制单次续传会话回放的<b>总时长</b>：预算耗尽仍未回放完成
         * （含元素缓慢持续到达、元素间隔超时始终未触发的场景），
         * 降级为实时流并发送 {@code event: resync}。
         * 预算内同时作为元素间隔超时，兜底存储读取挂起。
         * {@code 0} 或负值表示不限制（不推荐）。
         * </p>
         */
        private Duration replayTimeout = Duration.ofSeconds(5);

        /**
         * 雪花算法 ID 生成器配置
         */
        private SnowflakeConfig snowflake = new SnowflakeConfig();

        /**
         * 内存历史存储配置
         */
        private InMemoryHistoryConfig inMemory = new InMemoryHistoryConfig();
    }

    /**
     * 雪花算法事件 ID 生成器配置.
     * <p>
     * <b>CLUSTER 多实例 + 断点续传场景必须开启</b>：默认自增 ID 仅单 JVM 有序，
     * 多实例序列交叉会导致回放乱序。
     * </p>
     *
     * @since 5.7.0
     */
    @Data
    public static class SnowflakeConfig {
        /**
         * 是否启用雪花算法 ID 生成器
         */
        private boolean enabled = false;

        /**
         * 实例编号（0~1023），集群内必须唯一.
         * <p>
         * -1（默认）表示按主机名哈希自动分配（存在小概率冲突，
         * 生产环境建议显式配置）。
         * </p>
         */
        private long workerId = -1;
    }

    /**
     * 内存历史存储配置.
     * <p>
     * 适用于 STANDALONE 单机部署与开发/测试环境；进程重启丢失历史。
     * 生产 CLUSTER 部署请实现自定义 {@link cn.gmlee.tools.im.resume.MessageHistoryStore}。
     * </p>
     *
     * @since 5.7.0
     */
    @Data
    public static class InMemoryHistoryConfig {
        /**
         * 是否启用内存历史存储
         */
        private boolean enabled = false;

        /**
         * 每 Topic 最大保留消息数（超出逐出最旧）
         */
        private int capacityPerTopic = 1000;
    }
}
