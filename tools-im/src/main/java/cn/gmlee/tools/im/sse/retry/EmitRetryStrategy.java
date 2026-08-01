package cn.gmlee.tools.im.sse.retry;

import reactor.core.publisher.Sinks;

/**
 * 消息发射重试策略接口.
 * <p>
 * 使用策略模式封装不同的重试行为，支持运行时切换。
 * 框架提供四种内置策略：
 * </p>
 * <ul>
 *   <li><b>no-retry</b>：单次尝试，失败立即返回（性能最优）</li>
 *   <li><b>busy-loop</b>：忙等待重试直到成功或超时（可靠性最高）</li>
 *   <li><b>bounded</b>：固定次数重试（平衡可靠性和资源）</li>
 *   <li><b>exponential-backoff</b>：指数退避重试（适合高并发）</li>
 * </ul>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * EmitRetryStrategy strategy = new BusyLoopRetryStrategy(Duration.ofMillis(100));
 * Sinks.EmitResult result = strategy.emit(sink, message);
 * if (result.isSuccess()) {
 *     // 发射成功
 * }
 * }</pre>
 *
 * @since 5.6.0
 */
public interface EmitRetryStrategy {

    /**
     * 向 Sink 发射消息.
     * <p>
     * 根据策略实现决定是否重试以及重试方式。
     * </p>
     *
     * @param sink    目标 Sink
     * @param message 待发射的消息
     * @param <T>     消息类型
     * @return 发射结果
     */
    <T> Sinks.EmitResult emit(Sinks.Many<T> sink, T message);

    /**
     * 策略名称.
     *
     * @return 策略名称标识符
     */
    String name();
}
