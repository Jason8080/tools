package cn.gmlee.tools.base.ex;


import cn.gmlee.tools.base.enums.XCode;

/**
 * 通用断言异常
 *
 * @author Jas °
 */
public class AssertException extends SkillException {

    /**
     * Instantiates a new Assert exception.
     */
    public AssertException() {
        super(XCode.ASSERT_FAIL);
    }

    /**
     * Instantiates a new Assert exception.
     *
     * @param message the message
     */
    public AssertException(String message) {
        super(XCode.ASSERT_FAIL.code, message);
    }

    /**
     * Instantiates a new Assert exception.
     *
     * @param message the message
     * @param cause   the cause
     */
    public AssertException(String message, Throwable cause) {
        super(XCode.ASSERT_FAIL.code, message, cause);
    }

    /**
     * Instantiates a new Assert exception.
     *
     * @param cause the cause
     */
    public AssertException(Throwable cause) {
        super(XCode.ASSERT_FAIL.code, cause);
    }
}
