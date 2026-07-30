package cn.gmlee.tools.im.ex;

import cn.gmlee.tools.im.spi.AccessFilter;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 访问拒绝异常.
 * <p>
 * 当 {@link AccessFilter} 拒绝访问时抛出此异常。
 * 支持不同的 HTTP 状态码（如 401 未认证、403 无权限、429 限流等）。
 * </p>
 *
 * @since 5.6.0
 */
public class AccessDeniedException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * HTTP 状态码
     */
    @Getter
    private final HttpStatus status;

    /**
     * 创建访问拒绝异常（默认 403）.
     *
     * @param message 拒绝原因
     */
    public AccessDeniedException(String message) {
        this(message, HttpStatus.FORBIDDEN);
    }

    /**
     * 创建访问拒绝异常（指定状态码）.
     *
     * @param message 拒绝原因
     * @param status  HTTP 状态码
     */
    public AccessDeniedException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    /**
     * 创建访问拒绝异常（带原因异常）.
     *
     * @param message 拒绝原因
     * @param cause   原因异常
     * @param status  HTTP 状态码
     */
    public AccessDeniedException(String message, Throwable cause, HttpStatus status) {
        super(message, cause);
        this.status = status;
    }
}
