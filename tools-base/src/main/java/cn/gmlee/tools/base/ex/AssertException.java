package cn.gmlee.tools.base.ex;


import cn.gmlee.tools.base.enums.XCode;

/**
 * 通用断言异常
 *
 * @author Jas°
 * @date 2020/9/28 (周一)
 */
public class AssertException extends SkillException {

    public AssertException() {
        super(XCode.CONSENSUS_USER2002.code, XCode.CONSENSUS_USER2002.msg);
    }

    public AssertException(String message) {
        super(XCode.CONSENSUS_USER2002.code, message);
    }

    public AssertException(String message, Throwable cause) {
        super(XCode.CONSENSUS_USER2002.code, message, cause);
    }

    public AssertException(Throwable cause) {
        super(XCode.CONSENSUS_USER2002.code, cause);
    }
}
