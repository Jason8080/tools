package cn.gmlee.tools.im.ex;

/**
 * SSE 连接管理异常基类.
 * <p>
 * 所有 SSE 相关的异常都继承此类，便于统一捕获和处理。
 * 继承 RuntimeException 以兼容响应式 API。
 * </p>
 */
public abstract class SseException extends RuntimeException {

    protected SseException(String message) {
        super(message);
    }

    protected SseException(String message, Throwable cause) {
        super(message, cause);
    }
}
