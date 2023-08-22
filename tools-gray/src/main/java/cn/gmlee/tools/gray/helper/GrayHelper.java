package cn.gmlee.tools.gray.helper;

/**
 * The type Gray helper.
 */
public class GrayHelper {

    private static final InheritableThreadLocal<Boolean> enable = new InheritableThreadLocal<Boolean>();

    /**
     * Enable.
     *
     * @param enable the enable
     */
    public static void enable(Boolean enable){
        GrayHelper.enable.set(enable);
    }

    /**
     * Enable boolean.
     *
     * @return the boolean
     */
    public static boolean enable(){
        Boolean enable = GrayHelper.enable.get();
        return Boolean.TRUE.equals(enable);
    }

    /**
     * Clear.
     */
    public static void clear(){
        GrayHelper.enable.remove();
    }
}
