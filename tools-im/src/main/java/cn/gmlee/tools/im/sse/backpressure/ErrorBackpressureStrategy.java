package cn.gmlee.tools.im.sse.backpressure;

import reactor.core.publisher.Sinks;

/**
 * 错误背压策略.
 * <p>
 * 使用 multicast + onBackpressureBuffer(0)，
 * 当缓冲区满时直接失败，相当于严格模式。
 * </p>
 * <p>
 * 适用场景：要求客户端必须跟上消息速率，否则断开。
 * </p>
 */
public class ErrorBackpressureStrategy implements BackpressureStrategy {

    /**
     * 策略名称
     */
    public static final String NAME = "error";

    @Override
    public <T> Sinks.Many<T> createSink(int bufferSize) {
        // 使用 0 缓冲区，消息会立即失败
        return Sinks.many().multicast().onBackpressureBuffer(0);
    }

    @Override
    public String name() {
        return NAME;
    }
}
