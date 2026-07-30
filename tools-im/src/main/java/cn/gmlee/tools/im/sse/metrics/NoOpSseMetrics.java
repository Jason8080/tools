package cn.gmlee.tools.im.sse.metrics;

/**
 * 无操作的 SSE 指标收集器实现.
 * <p>
 * 当 classpath 中不存在 Micrometer 或配置禁用指标时使用此实现。
 * 所有方法均为空操作，无任何资源分配，确保无内存泄漏。
 * </p>
 * <p>
 * 此实现是线程安全的（所有方法都是空操作），可作为单例使用。
 * </p>
 *
 * @see SseMetrics
 * @see MicrometerSseMetrics
 */
public class NoOpSseMetrics implements SseMetrics {

    /**
     * 单例实例（可选使用）.
     */
    public static final NoOpSseMetrics INSTANCE = new NoOpSseMetrics();

    @Override
    public void recordSubscribe(String topic, String result) {
        // No-op
    }

    @Override
    public void recordPublish(String topic, String result) {
        // No-op
    }

    @Override
    public void recordReaperScan() {
        // No-op
    }

    @Override
    public void recordZombieReaped(int count) {
        // No-op
    }

    @Override
    public void recordTopicCompaction(int count) {
        // No-op
    }

    @Override
    public void recordError(String type) {
        // No-op
    }

    @Override
    public void recordSubscribeDuration(String topic, long durationMs) {
        // No-op
    }

    @Override
    public void recordPublishDuration(String topic, long durationMs) {
        // No-op
    }

    @Override
    public void cleanupTopic(String topic) {
        // No-op: 无指标缓存，无需清理
    }

    @Override
    public void shutdownCleanup() {
        // No-op: 无指标缓存，无需清理
    }

    @Override
    public boolean isEnabled() {
        return false;
    }
}
