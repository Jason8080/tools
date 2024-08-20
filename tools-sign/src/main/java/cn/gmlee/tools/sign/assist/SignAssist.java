package cn.gmlee.tools.sign.assist;

import cn.gmlee.tools.sign.anno.Sign;

/**
 * The type Sign assist.
 */
public class SignAssist {
    private static final ThreadLocal<Sign> current = new InheritableThreadLocal<>();

    /**
     * Set.
     *
     * @param sign the sign
     */
    public static void set(Sign sign) {
        current.set(sign);
    }

    /**
     * Get sign.
     *
     * @return the sign
     */
    public static Sign get() {
        return current.get();
    }

    /**
     * Remove.
     */
    public static void remove() {
        current.remove();
    }
}
