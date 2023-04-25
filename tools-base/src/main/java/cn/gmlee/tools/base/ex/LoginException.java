package cn.gmlee.tools.base.ex;

import cn.gmlee.tools.base.enums.XCode;

/**
 * 登陆异常
 *
 * @author Jas °
 * @date 2020 /10/20 (周二)
 */
public class LoginException extends SkillException {

    /**
     * Instantiates a new Login exception.
     */
    public LoginException() {
        this(XCode.SOA_AUTH7001.code, XCode.SOA_AUTH7001.msg);
    }


    /**
     * Instantiates a new Login exception.
     *
     * @param code    the code
     * @param message the message
     */
    public LoginException(Integer code, String message) {
        super(code, message);
    }

    /**
     * Msg login exception.
     *
     * @param msg the msg
     * @return the login exception
     */
    public static LoginException newly(String msg) {
        return new LoginException(XCode.SOA_AUTH7001.code, msg);
    }

    /**
     * Newly login exception.
     *
     * @param code the code
     * @return the login exception
     */
    public static LoginException newly(Integer code) {
        return new LoginException(code, XCode.SOA_AUTH7001.msg);
    }

    /**
     * Newly login exception.
     *
     * @param code the code
     * @param msg  the msg
     * @return the login exception
     */
    public static LoginException newly(Integer code, String msg) {
        return new LoginException(code, msg);
    }
}
