package cn.gmlee.tools.profile.helper;

import lombok.Getter;

/**
 * 环境帮助类.
 */
public class ProfileHelper {

    private static volatile Boolean open = Boolean.TRUE;

    private static ThreadLocal<Boolean> read = new InheritableThreadLocal<>();
    private static ThreadLocal<Boolean> write = new InheritableThreadLocal<>();

    @Getter
    public enum ReadWrite {
        READ,
        WRITE,
        ;
    }


    /**
     * 启禁.
     *
     * @return the boolean
     */
    public static void enable(ReadWrite rw) {
        if (ReadWrite.READ.equals(rw)) {
            ProfileHelper.read.set(true);
        } else if (ReadWrite.WRITE.equals(rw)) {
            ProfileHelper.write.set(true);
        }
    }

    /**
     * 禁用.
     *
     * @return the boolean
     */
    public static void remove() {
        ProfileHelper.read.remove();
        ProfileHelper.write.remove();
    }

    /**
     * 是否已开启.
     *
     * @return the boolean
     */
    public static boolean enabled(ReadWrite rw) {
        return ReadWrite.READ.equals(rw) ? Boolean.TRUE.equals(read.get()) : Boolean.TRUE.equals(write.get());
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
