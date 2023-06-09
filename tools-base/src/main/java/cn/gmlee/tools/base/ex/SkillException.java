package cn.gmlee.tools.base.ex;

import cn.gmlee.tools.base.enums.XCode;

/**
 * Skill Exception.
 *
 * @author Jas °
 */
public class SkillException extends RuntimeException {

    private Integer code;

    /**
     * Gets code.
     *
     * @return the code
     */
    public Integer getCode() {
        return code;
    }

    /**
     * Instantiates a new Skill exception.
     */
    public SkillException() {
        super(XCode.FAIL.msg);
        this.code = XCode.FAIL.code;
    }

    /**
     * Instantiates a new Skill exception.
     *
     * @param e the e
     */
    public SkillException(SkillException e) {
        super(e.getMessage());
        this.code = e.code;
    }

    /**
     * Instantiates a new Skill exception.
     *
     * @param xCode the x code
     */
    public SkillException(XCode xCode) {
        super(xCode.msg);
        this.code = xCode.code;
    }

    /**
     * Instantiates a new Skill exception.
     *
     * @param code the code
     */
    public SkillException(Integer code) {
        this.code = code;
    }

    /**
     * Instantiates a new Skill exception.
     *
     * @param code    the code
     * @param message the message
     */
    public SkillException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * Instantiates a new Skill exception.
     *
     * @param code    the code
     * @param message the message
     * @param cause   the cause
     */
    public SkillException(Integer code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    /**
     * Instantiates a new Skill exception.
     *
     * @param code  the code
     * @param cause the cause
     */
    public SkillException(Integer code, Throwable cause) {
        super(cause);
        this.code = code;
    }
}
