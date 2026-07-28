package cn.gmlee.tools.im.sse.backpressure;

import reactor.core.publisher.Sinks;

/**
 * 重放最新消息背压策略.
 * <p>
 * 使用 replay().latest()，新订阅者会立即收到最后一条消息。
 * </p>
 * <p>
 * 适用场景：新连接需要立即获取当前状态（如配置、在线用户列表）。
 * </p>
 */
public class ReplayLatestBackpressureStrategy implements BackpressureStrategy {

    /**
     * 策略名称
     */
    public static final String NAME = "replay-last";

    @Override
    public <T> Sinks.Many<T> createSink(int bufferSize) {
        return Sinks.many().replay().latest();
    }

    @Override
    public String name() {
        return NAME;
    }
}
