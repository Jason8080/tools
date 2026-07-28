package cn.gmlee.tools.im.sse.backpressure;

import reactor.core.publisher.Sinks;

/**
 * 丢弃最旧消息背压策略.
 * <p>
 * 使用 multicast + onBackpressureBuffer(1)，
 * 缓冲区满时丢弃最旧的消息，始终保留最新的消息。
 * </p>
 * <p>
 * 适用场景：实时场景，只有最新消息有意义（如股价、位置更新）。
 * </p>
 */
public class DropOldestBackpressureStrategy implements BackpressureStrategy {

    /**
     * 策略名称
     */
    public static final String NAME = "drop-oldest";

    @Override
    public <T> Sinks.Many<T> createSink(int bufferSize) {
        // 使用 replay().latest() 实现丢弃旧消息的效果
        return Sinks.many().replay().latest();
    }

    @Override
    public String name() {
        return NAME;
    }
}
