package cn.gmlee.tools.im.sse.retry;

import reactor.core.publisher.Sinks;

import java.time.Duration;

/**
 * 有限重试策略（平衡可靠性和资源）.
 * <p>
 * 固定重试次数，每次重试间隔固定时间。
 * 在可靠性和资源消耗之间取得平衡。
 * </p>
 *
 * <h3>特性</h3>
 * <ul>
 *   <li>重试次数可控，资源消耗可预测</li>
 *   <li>适合中等可靠性要求的场景</li>
 *   <li>避免无限重试导致的资源耗尽</li>
 * </ul>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * // 最多重试 3 次，每次间隔 10ms
 * EmitRetryStrategy strategy = new BoundedRetryStrategy(3, Duration.ofMillis(10));
 * }</pre>
 *
 * @since 5.6.0
 */
public final class BoundedRetryStrategy implements EmitRetryStrategy {

    /**
     * 最大重试次数.
     */
    private final int maxRetries;

    /**
     * 重试间隔.
     */
    private final Duration retryInterval;

    /**
     * 创建有限重试策略.
     *
     * @param maxRetries    最大重试次数
     * @param retryInterval 每次重试的间隔时间
     */
    public BoundedRetryStrategy(int maxRetries, Duration retryInterval) {
        this.maxRetries = maxRetries;
        this.retryInterval = retryInterval;
    }

    @Override
    public <T> Sinks.EmitResult emit(Sinks.Many<T> sink, T message) {
        Sinks.EmitResult result;
        int attempts = 0;

        do {
            result = sink.tryEmitNext(message);
            if (result.isSuccess()) {
                return result;
            }

            attempts++;
            if (attempts < maxRetries && result == Sinks.EmitResult.FAIL_NON_SERIALIZED) {
                try {
                    Thread.sleep(retryInterval.toMillis());
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
        return "bounded";
    }
}
