package cn.gmlee.tools.base.ex.agreed;

import cn.gmlee.tools.base.enums.XCode;
import cn.gmlee.tools.base.ex.AgreedException;

/**
 * 用户体验异常
 * (msg直接展示给客户的异常消息).
 *
 * @author Jas °
 * @date 2020 /10/20 (周二)
 */
public class UserExperienceException extends AgreedException {
    /**
     * Instantiates a new User experience exception.
     */
    public UserExperienceException() {
        super(XCode.CONSENSUS_USER2002.code, XCode.CONSENSUS_USER2002.msg);
    }

    /**
     * Instantiates a new Business exception.
     *
     * @param message the message
     */
    public UserExperienceException(String message) {
        super(XCode.CONSENSUS_USER2002.code, message);
    }

    /**
     * Instantiates a new Business exception.
     *
     * @param message the message
     * @param cause   the cause
     */
    public UserExperienceException(String message, Throwable cause) {
        super(XCode.CONSENSUS_USER2002.code, message, cause);
    }
}
