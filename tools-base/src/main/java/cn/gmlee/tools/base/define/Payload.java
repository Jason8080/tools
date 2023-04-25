package cn.gmlee.tools.base.define;

/**
 * 载荷.
 * <p>
 * 记录使用场景:
 * 1. Jwt
 * </p>
 *
 * @author Jas°
 */
public interface Payload {
    /**
     * 获取用户编号.
     *
     * @return the uid
     */
    Long getUid();

    /**
     * 获取到期时间.
     *
     * @return the exp
     */
    Long getExp();
}
