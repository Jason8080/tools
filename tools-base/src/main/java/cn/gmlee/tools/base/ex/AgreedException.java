package cn.gmlee.tools.base.ex;

import cn.gmlee.tools.base.enums.XCode;

/**
 * 通用约定响应码异常
 * <p>
 * 用于后端开发者和前端开发者约定好当前场景下使用的响应码
 * </p>
 *
 * @author Jas °
 * @date 2020 /10/20 (周二)
 */
public class AgreedException extends SkillException {
    /**
     * Instantiates a new Agreed exception.
     */
    public AgreedException() {
        super(XCode.CONSENSUS2000.code, XCode.CONSENSUS2000.msg);
    }

    /**
     * Instantiates a new Custom exception.
     *
     * @param code the code
     */
    public AgreedException(Integer code) {
        super(code);
    }

    /**
     * Instantiates a new Custom exception.
     *
     * @param code    the code
     * @param message the message
     */
    public AgreedException(Integer code, String message) {
        super(code, message);
    }

    /**
     * Instantiates a new Custom exception.
     *
     * @param code    the code
     * @param message the message
     * @param cause   the cause
     */
    public AgreedException(Integer code, String message, Throwable cause) {
        super(code, message, cause);
    }

    /**
     * Instantiates a new Custom exception.
     *
     * @param code  the code
     * @param cause the cause
     */
    public AgreedException(Integer code, Throwable cause) {
        super(code, cause);
    }
}
