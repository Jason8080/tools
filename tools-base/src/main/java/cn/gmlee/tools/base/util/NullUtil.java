package cn.gmlee.tools.base.util;

import cn.gmlee.tools.base.enums.Function;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * 防止null异常工具
 *
 * @author Jas °
 * @date 2020 /9/28 (周一)
 */
public class NullUtil {

    /**
     * First t.
     *
     * @param <T> the type parameter
     * @param ts  the ts
     * @return the t
     */
    public static <T> T first(T... ts){
        for (int i = 0; i < ts.length; i++) {
            T t = ts[i];
            if(BoolUtil.notNull(t)){
                return t;
            }
        }
        return null;
    }

    /**
     * First t.
     *
     * @param <T> the type parameter
     * @param ts  the ts
     * @return the t
     */
    public static <T extends Collection> T first(T... ts){
        for (int i = 0; i < ts.length; i++) {
            T t = ts[i];
            if(BoolUtil.notEmpty(t)){
                return t;
            }
        }
        return (T) new ArrayList();
    }

    /**
     * First t.
     *
     * @param <T> the type parameter
     * @param ts  the ts
     * @return the t
     */
    public static <T extends Map> T first(T... ts){
        for (int i = 0; i < ts.length; i++) {
            T t = ts[i];
            if(BoolUtil.notEmpty(t)){
                return t;
            }
        }
        return (T) new HashMap();
    }

    /**
     * 捕捉空异常.
     *
     * @param run the run
     * @return the t
     */
    public static void get(Function.Zero run) {
        try {
            run.run();
        } catch (NullPointerException e) {
        } catch (Throwable e) {
            ExceptionUtil.cast(e);
        }
    }

    /**
     * 捕捉空异常.
     *
     * @param <T> the type parameter
     * @param run the run
     * @param def the def
     * @return the t
     */
    public static <T> T get(Function.Zero2r<T> run, T def) {
        try {
            return run.run();
        } catch (NullPointerException e) {
            return def;
        } catch (Throwable e) {
            return ExceptionUtil.cast(e);
        }
    }

    /**
     * Get t.
     *
     * @param <T> the type parameter
     * @param run the run
     * @param def the def
     * @return the t
     */
    public static <T> T get(Function.Zero2r<T> run, Function.Zero2r<T> def) {
        try {
            return run.run();
        } catch (NullPointerException e) {
            return ExceptionUtil.suppress(def);
        } catch (Throwable e) {
            return ExceptionUtil.cast(e);
        }
    }

    /**
     * Get t.
     *
     * @param <T>    the type parameter
     * @param source the source
     * @param def    the def
     * @return the t
     */
    public static <T> T get(T source, Function.Zero2r<T> def) {
        if (source == null) {
            return ExceptionUtil.suppress(def);
        }
        return source;
    }

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
     * Get collection.
     *
     * @param <T>    the type parameter
     * @param source the source
     * @return the collection
     */
    public static <T extends Collection> T get(T source) {
        if (source == null) {
            return (T) new ArrayList(0);
        }
        return source;
    }

    /**
     * Get t [ ].
     *
     * @param <T>    the type parameter
     * @param source the source
     * @return the t [ ]
     */
    public static <T extends Object> T[] get(T[] source) {
        if (source == null) {
            return (T[]) new Object[0];
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
