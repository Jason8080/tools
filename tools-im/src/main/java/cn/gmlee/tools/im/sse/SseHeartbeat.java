package cn.gmlee.tools.im.sse;

/**
 * SSE 心跳哨兵对象.
 * <p>
 * 在合并数据流和心跳流时，用于区分心跳事件和真实数据事件。
 * 使用单例模式避免重复创建。
 * </p>
 * <p>
 * 在 SubscriberEndpoint 层，SseHeartbeat 会被转换为 SSE 注释：
 * {@code : heartbeat <timestamp>\n\n}
 * 符合 SSE 规范，客户端无需任何改动。
 * </p>
 */
public final class SseHeartbeat {

    /**
     * 单例实例
     */
    public static final SseHeartbeat INSTANCE = new SseHeartbeat();

    private SseHeartbeat() {
    }

    @Override
    public String toString() {
        return "heartbeat";
    }
}
