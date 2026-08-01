package cn.gmlee.tools.im.sse.retry;

import reactor.core.publisher.Sinks;

/**
 * 无重试策略（默认，性能最优）.
 * <p>
 * 单次尝试发射，失败立即返回。
 * 适用于单线程发布场景（如 MQ Consumer 单线程消费）。
 * </p>
 *
 * <h3>特性</h3>
 * <ul>
 *   <li>零额外开销，性能最优</li>
 *   <li>多线程竞争时可能丢失消息</li>
 *   <li>适合对消息可靠性要求不高的场景</li>
 * </ul>
 *
 * @since 5.6.0
 */
public final class NoRetryStrategy implements EmitRetryStrategy {

    /**
     * 单例实例.
     */
    public static final NoRetryStrategy INSTANCE = new NoRetryStrategy();

    private NoRetryStrategy() {
    }

    @Override
    public <T> Sinks.EmitResult emit(Sinks.Many<T> sink, T message) {
        return sink.tryEmitNext(message);
    }

    @Override
    public String name() {
        return "no-retry";
    }
}
