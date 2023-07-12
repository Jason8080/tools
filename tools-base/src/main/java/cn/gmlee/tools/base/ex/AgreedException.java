package cn.gmlee.tools.base.ex;

/**
 * 通用约定响应码异常
 * <p>
 * 用于后端开发者和前端开发者约定好当前场景下使用的响应码
 * </p>
 *
 * @author Jas °
 */
public class AgreedException extends SkillException {
    /**
     * Instantiates a new Agreed exception.
     *
     * @param code the code
     */
    public AgreedException(Integer code) {
        super(code);
    }

    /**
     * Instantiates a new Agreed exception.
     *
     * @param code    the code
     * @param message the message
     */
    public AgreedException(Integer code, String message) {
        super(code, message);
    }
}
