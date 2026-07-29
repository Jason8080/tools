package cn.gmlee.tools.im.sse.metrics;

/**
 * SSE 指标收集器接口.
 * <p>
 * 定义指标收集的契约，具体实现可以是 Micrometer 实现或 NoOp 实现。
 * 自动配置会根据 classpath 中是否存在 Micrometer 来选择合适的实现。
 * </p>
 *
 * <h3>暴露的指标</h3>
 * <ul>
 *   <li>im.sse.connections.total - 总连接数（Gauge）</li>
 *   <li>im.sse.connections.active - 每 Topic 连接数（Gauge）</li>
 *   <li>im.sse.subscribe.rate - 订阅尝试（Counter，标签：topic, result）</li>
 *   <li>im.sse.publish.rate - 发布尝试（Counter，标签：topic, result）</li>
 *   <li>im.sse.publish.no-subscribers - 无订阅者消息（Counter，标签：topic）</li>
 *   <li>im.sse.reaper.scans - 收割扫描次数（Counter）</li>
 *   <li>im.sse.reaper.zombies - 收割的僵尸连接（Counter）</li>
 *   <li>im.sse.sinks.active - 活跃 Sink 数（Gauge）</li>
 *   <li>im.sse.errors - 错误计数（Counter，标签：type）</li>
 *   <li>im.sse.subscribe.duration - 订阅延迟（Timer，标签：topic）</li>
 *   <li>im.sse.publish.duration - 发布延迟（Timer，标签：topic）</li>
 * </ul>
 *
 * @see MicrometerSseMetrics
 * @see NoOpSseMetrics
 */
public interface SseMetrics {

    /**
     * 记录订阅结果.
     *
     * @param topic  Topic
     * @param result 结果（SUCCESS / REJECTED_GLOBAL / REJECTED_TOPIC / REJECTED_SHUTDOWN）
     */
    void recordSubscribe(String topic, String result);

    /**
     * 记录发布结果.
     *
     * @param topic  Topic
     * @param result 结果（SUCCESS / NO_SUBSCRIBERS / EMIT_FAILURE）
     */
    void recordPublish(String topic, String result);

    /**
     * 记录收割扫描.
     */
    void recordReaperScan();

    /**
     * 记录收割的僵尸连接.
     *
     * @param count 数量
     */
    void recordZombieReaped(int count);

    /**
     * 记录错误.
     * <p>
     * 错误类型来自固定集合（如 reaper_scan、subscribe_init），数量有限。
     * </p>
     *
     * @param type 错误类型
     */
    void recordError(String type);

    /**
     * 记录订阅延迟.
     *
     * @param topic      Topic
     * @param durationMs 延迟（毫秒）
     */
    void recordSubscribeDuration(String topic, long durationMs);

    /**
     * 记录发布延迟.
     *
     * @param topic      Topic
     * @param durationMs 延迟（毫秒）
     */
    void recordPublishDuration(String topic, long durationMs);

    /**
     * 清理指定 Topic 的所有指标.
     * <p>
     * 当 Topic 被销毁时调用，从本地缓存和指标注册表中移除该 Topic 的指标对象，
     * 防止动态 Topic 场景下指标对象无限增长。
     * </p>
     *
     * @param topic 要清理的 Topic
     */
    void cleanupTopic(String topic);

    /**
     * 关闭时清理所有本地指标缓存.
     * <p>
     * 清理所有 Counter/Timer 的本地缓存引用，防止组件重启后旧指标对象残留。
     * 在 SseConnectionManager 的异步关闭任务中调用。
     * </p>
     */
    void shutdownCleanup();

    /**
     * 检查是否启用指标.
     *
     * @return 启用返回 true
     */
    boolean isEnabled();
}
