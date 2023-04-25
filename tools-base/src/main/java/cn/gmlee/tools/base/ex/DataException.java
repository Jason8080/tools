package cn.gmlee.tools.base.ex;

import cn.gmlee.tools.base.enums.XCode;

/**
 * 通用数据异常
 *
 * @author Jas °
 * @date 2020 /11/13 (周五)
 */
public class DataException extends SkillException {
    /**
     * Instantiates a new Data exception.
     */
    public DataException() {
        super(XCode.UNREASONABLE_NOT_FOUND4004.code, XCode.UNREASONABLE_NOT_FOUND4004.msg);
    }

    /**
     * Instantiates a new Data exception.
     *
     * @param code the code
     */
    public DataException(Integer code) {
        super(code);
    }

    /**
     * Instantiates a new Data exception.
     *
     * @param code    the code
     * @param message the message
     */
    public DataException(Integer code, String message) {
        super(code, message);
    }

    /**
     * Instantiates a new Data exception.
     *
     * @param message the message
     */
    public DataException(String message) {
        super(XCode.UNREASONABLE_NOT_FOUND4004.code, message);
    }

    /**
     * Instantiates a new Data exception.
     *
     * @param message the message
     * @param cause   the cause
     */
    public DataException(String message, Throwable cause) {
        super(XCode.UNREASONABLE_NOT_FOUND4004.code, message, cause);
    }
}
