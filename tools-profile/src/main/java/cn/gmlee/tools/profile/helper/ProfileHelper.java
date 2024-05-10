package cn.gmlee.tools.profile.helper;

/**
 * 环境帮助类.
 */
public class ProfileHelper {

    private static volatile Boolean open = Boolean.TRUE;

    private static ThreadLocal<Boolean> env = new InheritableThreadLocal<>();


    /**
     * 启禁.
     *
     * @return the boolean
     */
    public static void enable(boolean enable) {
        ProfileHelper.env.set(enable);
    }

    /**
     * 禁用.
     *
     * @return the boolean
     */
    public static void remove() {
        ProfileHelper.env.remove();
    }

    /**
     * 是否已开启.
     *
     * @return the boolean
     */
    public static boolean enabled() {
        return Boolean.TRUE.equals(env.get());
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * 开关.
     *
     * @return the boolean
     */
    public static void open(boolean open) {
        ProfileHelper.open = open;
    }

    /**
     * 是否已关闭.
     *
     * @return the boolean
     */
    public static boolean closed() {
        return !Boolean.TRUE.equals(open);
    }
}
