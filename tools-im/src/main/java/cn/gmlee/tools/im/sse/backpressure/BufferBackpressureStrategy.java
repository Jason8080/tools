package cn.gmlee.tools.im.sse.backpressure;

import reactor.core.publisher.Sinks;

/**
 * 缓冲区背压策略（默认）.
 * <p>
 * 使用 multicast + onBackpressureBuffer，客户端可吸收突发流量。
 * 当缓冲区满时，新消息会被丢弃并触发 EmitResult.FAIL_NON_SERIALIZED。
 * </p>
 * <p>
 * 适用场景：大多数常规场景，客户端能够跟上消息速率。
 * </p>
 */
public class BufferBackpressureStrategy implements BackpressureStrategy {

    /**
     * 策略名称
     */
    public static final String NAME = "buffer";

    @Override
    public <T> Sinks.Many<T> createSink(int bufferSize) {
        return Sinks.many().multicast().onBackpressureBuffer(bufferSize);
    }

    @Override
    public String name() {
        return NAME;
    }
}
