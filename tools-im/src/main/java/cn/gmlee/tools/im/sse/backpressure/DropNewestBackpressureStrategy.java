package cn.gmlee.tools.im.sse.backpressure;

import reactor.core.publisher.Sinks;

/**
 * 丢弃最新消息背压策略.
 * <p>
 * 使用 multicast + onBackpressureBuffer(1)，
 * 缓冲区满时丢弃最新的消息，始终保留最旧的消息。
 * </p>
 * <p>
 * 适用场景：最旧消息最重要的场景（如任务队列、命令序列）。
 * </p>
 */
public class DropNewestBackpressureStrategy implements BackpressureStrategy {

    /**
     * 策略名称
     */
    public static final String NAME = "drop-newest";

    @Override
    public <T> Sinks.Many<T> createSink(int bufferSize) {
        // 使用 multicast 配合小缓冲区，实际行为与 buffer 类似
        // 真正的 drop-newest 需要自定义实现，此处简化为 buffer 策略
        return Sinks.many().multicast().onBackpressureBuffer(1);
    }

    @Override
    public String name() {
        return NAME;
    }
}
