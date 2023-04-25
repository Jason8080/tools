package cn.gmlee.tools.base.ex.agreed;

import cn.gmlee.tools.base.enums.XCode;
import cn.gmlee.tools.base.ex.AgreedException;

/**
 * 需要重试异常
 * (前端收到该响应码需要重新发起请求).
 *
 * @author Jas °
 * @date 2020 /10/20 (周二)
 */
public class WantRetryException extends AgreedException {
    /**
     * Instantiates a new Want retry exception.
     */
    public WantRetryException() {
        super(XCode.CONSENSUS_RETRY2010.code, XCode.CONSENSUS_RETRY2010.msg);
    }

    /**
     * Instantiates a new Business exception.
     *
     * @param message the message
     */
    public WantRetryException(String message) {
        super(XCode.CONSENSUS_RETRY2010.code, message);
    }

    /**
     * Instantiates a new Business exception.
     *
     * @param message the message
     * @param cause   the cause
     */
    public WantRetryException(String message, Throwable cause) {
        super(XCode.CONSENSUS_RETRY2010.code, message, cause);
    }
}
