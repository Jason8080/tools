package cn.gmlee.tools.im.ex;

/**
 * SSE 服务关闭异常.
 * <p>
 * 当 SSE 管理器正在关闭、拒绝新连接时抛出。
 * 使用单例模式避免重复创建。
 * </p>
 */
public class SseShutdownException extends SseException {

    /**
     * 单例实例
     */
    public static final SseShutdownException INSTANCE = new SseShutdownException();

    private SseShutdownException() {
        super("SSE 服务正在关闭，拒绝新连接");
    }
}
