package cn.gmlee.tools.im.sse.backpressure;

import reactor.core.publisher.Sinks;

/**
 * 背压策略接口.
 * <p>
 * 定义如何创建 Reactor Sink，不同的策略对应不同的背压行为：
 * <ul>
 *   <li>buffer：缓冲区（默认），客户端可吸收突发</li>
 *   <li>drop-oldest：丢弃最旧消息，仅保留最新</li>
 *   <li>drop-newest：丢弃最新消息，保留最旧</li>
 *   <li>error：严格模式，客户端必须跟上</li>
 *   <li>replay-last：新订阅者立即获取最后一条消息</li>
 * </ul>
 * </p>
 * <p>
 * 策略是按 Topic 的，不是按连接的。同一 Topic 的所有订阅者共享同一个 Sink。
 * </p>
 */
public interface BackpressureStrategy {

    /**
     * 创建 Sink.
     *
     * @param <T>        消息类型
     * @param bufferSize 缓冲区大小
     * @return 新创建的 Sink
     */
    <T> Sinks.Many<T> createSink(int bufferSize);

    /**
     * 策略名称（用于配置和指标）.
     *
     * @return 策略名称
     */
    String name();
}
