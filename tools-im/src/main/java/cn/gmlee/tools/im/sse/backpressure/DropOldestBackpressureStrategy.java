package cn.gmlee.tools.im.sse.backpressure;

import reactor.core.publisher.Sinks;

/**
 * 丢弃旧消息背压策略（系统默认）.
 * <p>
 * 使用 {@code Sinks.many().replay().latest()}，始终仅保留最新一条消息。
 * 新订阅者会立即收到最后一条消息；旧消息在新消息到达时被替换。
 * </p>
 * <p>
 * <b>注意</b>：此策略忽略 {@code bufferSize} 参数，固定保留 1 条消息。
 * 适用于只有最新消息有意义的实时场景（如股价、位置更新、在线状态）。
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
