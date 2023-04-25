package cn.gmlee.tools.base.ex;

import cn.gmlee.tools.base.enums.XCode;

/**
 * 验证码异常.
 *
 * @author Jas
 */
public class VcException extends SkillException {

    /**
     * 一般用于存储唯一键.
     * <p>
     * 比如: 用户民
     * </p>
     */
    private String key;

    /**
     * Gets key.
     *
     * @return the key
     */
    public String getKey() {
        return key;
    }

    /**
     * Instantiates a new Vc exception.
     */
    public VcException() {
        super(XCode.CONSENSUS_VC2012);
    }


    /**
     * Instantiates a new Vc exception.
     *
     * @param key the key
     */
    public VcException(String key) {
        super(XCode.CONSENSUS_VC2012);
        this.key = key;
    }

    /**
     * Instantiates a new Vc exception.
     *
     * @param key     the key
     * @param message the message
     */
    public VcException(String key, String message) {
        super(XCode.CONSENSUS_VC2012.code, message);
        this.key = key;
    }

    /**
     * Instantiates a new Business exception.
     *
     * @param key     the key
     * @param message the message
     * @param cause   the cause
     */
    public VcException(String key, String message, Throwable cause) {
        super(XCode.CONSENSUS_VC2012.code, message, cause);
        this.key = key;
    }
}
