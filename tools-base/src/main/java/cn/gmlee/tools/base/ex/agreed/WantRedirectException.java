package cn.gmlee.tools.base.ex.agreed;

import cn.gmlee.tools.base.enums.XCode;
import cn.gmlee.tools.base.ex.AgreedException;

/**
 * 需要重定向异常
 * (前端收到该响应码需要跳转请求).
 *
 * @author Jas °
 * @date 2020 /10/20 (周二)
 */
public class WantRedirectException extends AgreedException {
    /**
     * Instantiates a new Want retry exception.
     */
    public WantRedirectException() {
        super(XCode.REDIRECT.code, XCode.REDIRECT.msg);
    }

    /**
     * Instantiates a new Business exception.
     *
     * @param message the message
     */
    public WantRedirectException(String message) {
        super(XCode.REDIRECT.code, message);
    }
}
