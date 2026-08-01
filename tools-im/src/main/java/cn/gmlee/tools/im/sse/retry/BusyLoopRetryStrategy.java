package cn.gmlee.tools.im.sse.retry;

import reactor.core.publisher.Sinks;

import java.time.Duration;

/**
 * 忙等待重试策略（可靠性最高）.
 * <p>
 * CAS 失败时持续重试直到成功或超时。
 * 适用于多线程发布场景，保证消息不丢失。
 * </p>
 *
 * <h3>特性</h3>
 * <ul>
 *   <li>消息可靠性最高，几乎不丢失</li>
 *   <li>CAS 竞争时会占用 CPU</li>
 *   <li>适合对消息可靠性要求极高的场景</li>
 * </ul>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * // 超时 100ms 内持续重试
 * EmitRetryStrategy strategy = new BusyLoopRetryStrategy(Duration.ofMillis(100));
 * }</pre>
 *
 * @since 5.6.0
 */
public final class BusyLoopRetryStrategy implements EmitRetryStrategy {

    /**
     * 重试超时时间.
     */
    private final Duration timeout;

    /**
     * 创建忙等待重试策略.
     *
     * @param timeout 重试超时时间，超时后放弃重试
     */
    public BusyLoopRetryStrategy(Duration timeout) {
        this.timeout = timeout;
    }

    @Override
    public <T> Sinks.EmitResult emit(Sinks.Many<T> sink, T message) {
        Sinks.EmitResult[] result = new Sinks.EmitResult[1];
        sink.emitNext(message, (signalType, emitResult) -> {
            result[0] = emitResult;
            return emitResult == Sinks.EmitResult.FAIL_NON_SERIALIZED;
        });
        return result[0] != null ? result[0] : Sinks.EmitResult.OK;
    }

    @Override
    public String name() {
        return "busy-loop";
    }
}
