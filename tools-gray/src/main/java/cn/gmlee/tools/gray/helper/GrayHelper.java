package cn.gmlee.tools.gray.helper;

/**
 * 灰度帮助类.
 */
public class GrayHelper {

    private static final InheritableThreadLocal<Boolean> enable = new InheritableThreadLocal<Boolean>();

    /**
     * Enable.
     *
     * @param enable enable
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
        return !Boolean.FALSE.equals(enable);
    }

    /**
     * Clear.
     */
    public static void clear(){
        GrayHelper.enable.remove();
    }
}
