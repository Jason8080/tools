package cn.gmlee.tools.base.util;

/**
 * 防止null异常工具
 *
 * @author Jas°
 * @date 2020 /9/28 (周一)
 */
public class NullUtil {
    /**
     * 获取不为null的字符串.
     *
     * @param source the source
     * @return the string
     */
    public static String get(String source) {
        if (source == null) {
            return "";
        }
        return source;
    }

    /**
     * 获取不为null的对象, 如果是空则采用def对象, 如果def也是null就抛出250异常.
     *
     * @param <T>    the type parameter
     * @param source the source
     * @param def    the def
     * @return the t
     */
    public static <T> T get(T source, T def) {
        if (source == null) {
            if (def == null) {
                return ExceptionUtil.cast("工具使用异常: 默认值也是空");
            }
            return def;
        }
        return source;
    }

    /**
     * 获取不为null的对象, 如果是空则采用tClass创建对象, 如果创建对象出错就抛出250异常.
     *
     * @param <T>    the type parameter
     * @param source the source
     * @param tClass the t class
     * @return the t
     */
    public static <T> T get(T source, Class<T> tClass) {
        if (source == null) {
            try {
                return tClass.newInstance();
            } catch (Exception e) {
                return ExceptionUtil.cast("工具使用异常: 反射创建不了对象", e);
            }
        }
        return source;
    }

    /**
     * 获取不为null的对象, 如果是空则抛出提示性异常.
     *
     * @param <T>    the type parameter
     * @param source the source
     * @param msg    the msg
     * @return the t
     */
    public static <T> T cast(T source, String msg) {
        if (source == null) {
            return ExceptionUtil.cast(msg);
        }
        return source;
    }
}
