package cn.gmlee.tools.base.ex;

import cn.gmlee.tools.base.enums.XCode;
import cn.gmlee.tools.base.mod.JsonResult;

/**
 * 通用远程调用异常
 *
 * @author Jas °
 * @date 2020 /10/20 (周二)
 */
public class RemoteInvokeException extends SkillException {
    /**
     * Instantiates a new Remote invoke exception.
     */
    public RemoteInvokeException() {
        super(XCode.THIRD_PARTY_FAIL.code, XCode.THIRD_PARTY_FAIL.msg);
    }

    /**
     * Instantiates a new Remote invoke exception.
     *
     * @param result the result
     */
    public RemoteInvokeException(JsonResult result) {
        super(result.getCode(), result.getMsg());
    }

    /**
     * Instantiates a new Remote invoke exception.
     *
     * @param result the result
     * @param cause  the cause
     */
    public RemoteInvokeException(JsonResult result, Throwable cause) {
        super(result.getCode(), result.getMsg(), cause);
    }

    /**
     * Instantiates a new Remote invoke exception.
     *
     * @param code    the code
     * @param message the message
     */
    public RemoteInvokeException(Integer code, String message) {
        super(code, message);
    }

    /**
     * Instantiates a new Remote invoke exception.
     *
     * @param code    the code
     * @param message the message
     * @param cause   the cause
     */
    public RemoteInvokeException(Integer code, String message, Throwable cause) {
        super(code, message, cause);
    }
}
