package cn.gmlee.tools.profile.helper;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 环境帮助类.
 */
public class ProfileHelper {

    private static final AtomicBoolean enable = new AtomicBoolean(true);

    /**
     * Enable.
     *
     * @param enable enable
     */
    public static void enable(Boolean enable){
        ProfileHelper.enable.set(enable);
    }

    /**
     * Enable boolean.
     *
     * @return the boolean
     */
    public static boolean enable(){
        Boolean enable = ProfileHelper.enable.get();
        return !Boolean.FALSE.equals(enable);
    }
}
