package cn.gmlee.tools.profile.helper;

/**
 * 环境帮助类.
 */
public class ProfileHelper {
    private static volatile Boolean enable = Boolean.TRUE;


    /**
     * Enable boolean.
     *
     * @param enable enable
     * @return the boolean
     */
    public static void enable(Boolean enable) {
        ProfileHelper.enable = enable;
    }

    /**
     * Enable boolean.
     *
     * @return the boolean
     */
    public static boolean enable() {
        return Boolean.TRUE.equals(enable);
    }

    /**
     * Closed boolean.
     *
     * @return the boolean
     */
    public static boolean closed() {
        return Boolean.FALSE.equals(enable);
    }
}
