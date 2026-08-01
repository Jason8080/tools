package cn.gmlee.tools.im.sse.retry;

import reactor.core.publisher.Sinks;

import java.time.Duration;

/**
 * 指数退避重试策略（适合高并发）.
 * <p>
 * 重试间隔指数增长，避免在高竞争场景下加剧竞争。
 * 适用于高并发发布场景。
 * </p>
 *
 * <h3>特性</h3>
 * <ul>
 *   <li>退避策略避免竞争加剧</li>
 *   <li>适合高并发、高竞争场景</li>
 *   <li>最大间隔限制避免过长等待</li>
 * </ul>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * // 最多重试 5 次，初始间隔 10ms，乘数 2.0，最大间隔 1000ms
 * EmitRetryStrategy strategy = new ExponentialBackoffRetryStrategy(
 *     5, Duration.ofMillis(10), 2.0, Duration.ofMillis(1000)
 * );
 * }</pre>
 *
 * @since 5.6.0
 */
public final class ExponentialBackoffRetryStrategy implements EmitRetryStrategy {

    /**
     * 最大重试次数.
     */
    private final int maxRetries;

    /**
     * 初始重试间隔.
     */
    private final Duration initialInterval;

    /**
     * 间隔乘数.
     */
    private final double multiplier;

    /**
     * 最大重试间隔.
     */
    private final Duration maxInterval;

    /**
     * 创建指数退避重试策略.
     *
     * @param maxRetries       最大重试次数
     * @param initialInterval  初始重试间隔
     * @param multiplier       间隔乘数（每次重试间隔 = 上次间隔 × 乘数）
     * @param maxInterval      最大重试间隔（限制间隔上限）
     */
    public ExponentialBackoffRetryStrategy(int maxRetries, Duration initialInterval,
                                           double multiplier, Duration maxInterval) {
        this.maxRetries = maxRetries;
        this.initialInterval = initialInterval;
        this.multiplier = multiplier;
        this.maxInterval = maxInterval;
    }

    @Override
    public <T> Sinks.EmitResult emit(Sinks.Many<T> sink, T message) {
        Sinks.EmitResult result;
        Duration currentInterval = initialInterval;
        int attempts = 0;

        do {
            result = sink.tryEmitNext(message);
            if (result.isSuccess()) {
                return result;
            }

            attempts++;
            if (attempts < maxRetries && result == Sinks.EmitResult.FAIL_NON_SERIALIZED) {
                try {
                    Thread.sleep(currentInterval.toMillis());
                    currentInterval = Duration.ofMillis(
                            Math.min(
                                    (long) (currentInterval.toMillis() * multiplier),
                                    maxInterval.toMillis()
                            )
                    );
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return result;
                }
            } else {
                break;
            }
        } while (true);

        return result;
    }

    @Override
    public String name() {
        return "exponential-backoff";
    }
}
