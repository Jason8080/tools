package cn.gmlee.tools.base.util;

import cn.gmlee.tools.base.enums.Function;
import cn.gmlee.tools.base.enums.XCode;
import cn.gmlee.tools.base.ex.SkillException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.Objects;

/**
 * 通用抛出异常工具
 *
 * @author Jas °
 * @date 2020 /9/28 (周一)
 */
public class ExceptionUtil {

    private static final Logger logger = LoggerFactory.getLogger(ExceptionUtil.class);

    /**
     * 异常层级的分割符
     * 不是这个分隔符的异常信息无法处理
     */
    public static final String EXCEPTION_MESSAGE_SPLIT = "\\r\\n";

    /**
     * 异常换行分隔符
     * 不是这个分隔符信息无法继续简化
     */
    public static final String LINE_MESSAGE_SPLIT = "\\n";

    /**
     * Cast t.
     *
     * @param <T> the type parameter
     * @return the t
     */
    public static <T> T cast() {
        throw new SkillException(XCode.FAIL.code);
    }

    /**
     * Cast t.
     *
     * @param <T>   the type parameter
     * @param xCode the x code
     * @return the t
     */
    public static <T> T cast(XCode xCode) {
        throw new SkillException(xCode.code, xCode.msg);
    }

    /**
     * Cast t.
     *
     * @param <T>  the type parameter
     * @param code the code
     * @return the t
     */
    public static <T> T cast(Integer code) {
        throw new SkillException(code);
    }

    /**
     * Cast t.
     *
     * @param <T>  the type parameter
     * @param code the code
     * @param msg  the msg
     * @return the t
     */
    public static <T> T cast(Integer code, String msg) {
        throw new SkillException(code, msg);
    }

    /**
     * Cast t.
     *
     * @param <T> the type parameter
     * @param msg the msg
     * @return the t
     */
    public static <T> T cast(String msg) {
        throw new SkillException(XCode.FAIL.code, msg);
    }

    /**
     * Cast t.
     *
     * @param <T> the type parameter
     * @param e   the e
     * @return the t
     */
    public static <T> T cast(Throwable e) {
        if (e instanceof RuntimeException) {
            throw (RuntimeException) e;
        }
        throw new SkillException(XCode.FAIL.code, e);
    }

    /**
     * Cast t.
     *
     * @param <E>   the type parameter
     * @param clazz the clazz
     * @param os    the os
     * @return the t
     */
    public static <E extends RuntimeException> E cast(Class<E> clazz, Object... os) {
        throw ClassUtil.newInstance(clazz, os);
    }

    /**
     * Cast t.
     *
     * @param <T>  the type parameter
     * @param code the code
     * @param e    the e
     * @return the t
     */
    public static <T> T cast(Integer code, Throwable e) {
        throw new SkillException(code, e);
    }

    /**
     * Cast t.
     *
     * @param <T>  the type parameter
     * @param code the code
     * @param msg  the msg
     * @param e    the e
     * @return the t
     */
    public static <T> T cast(Integer code, String msg, Throwable e) {
        throw new SkillException(code, msg, e);
    }

    /**
     * Cast t.
     *
     * @param <T> the type parameter
     * @param msg the msg
     * @param e   the e
     * @return the t
     */
    public static <T> T cast(String msg, Throwable e) {
        throw new SkillException(XCode.FAIL.code, msg, e);
    }

    /**
     * Sandbox.
     *
     * @param fun   the fun
     * @param print the print
     */
    public static void sandbox(Function.Zero fun, boolean print) {
        try {
            fun.run();
        } catch (Throwable e) {
            if(print){
                logger.warn("沙箱异常提示: ", e);
            }
        }
    }

    /**
     * Sandbox.
     *
     * @param fun the fun
     * @param def the def
     */
    public static void sandbox(Function.Zero fun, Function.Zero def) {
        try {
            fun.run();
        } catch (Throwable e) {
            logger.info("沙箱异常提示: ", e);
            suppress(def);
        }
    }

    /**
     * 沙箱执行 (不会有异常抛出).
     *
     * @param fun the fun
     */
    public static void sandbox(Function.Zero fun) {
        sandbox(fun, true);
    }

    /**
     * 沙箱执行 (不会有异常抛出).
     *
     * @param fun the fun
     * @return the boolean
     */
    public static Boolean sandbox(Function.Ok0 fun) {
        return sandbox(fun, true);
    }

    /**
     * Sandbox boolean.
     *
     * @param fun   the fun
     * @param print the print
     * @return the boolean
     */
    public static Boolean sandbox(Function.Ok0 fun, boolean print) {
        try {
            return fun.run();
        } catch (Throwable e) {
            if(print){
                logger.warn("沙箱异常提示: ", e);
            }
            return false;
        }
    }

    /**
     * 沙箱执行 (不会有异常抛出).
     *
     * @param <R> the type parameter
     * @param fun the fun
     * @return the r
     */
    public static <R> R sandbox(Function.Zero2r<R> fun) {
        return sandbox(fun, true);
    }

    /**
     * Sandbox r.
     *
     * @param <R>   the type parameter
     * @param fun   the fun
     * @param print the print
     * @return the r
     */
    public static <R> R sandbox(Function.Zero2r<R> fun, boolean print) {
        try {
            return fun.run();
        } catch (Throwable e) {
            if(print){
                logger.warn("沙箱异常提示: ", e);
            }
            return null;
        }
    }

    /**
     * 沙箱执行 (不会有异常抛出).
     *
     * @param <R> the type parameter
     * @param fun the fun
     * @param def the def
     * @return the r
     */
    public static <R> R sandbox(Function.Zero2r<R> fun, R def) {
        try {
            return fun.run();
        } catch (Throwable e) {
            logger.info("沙箱异常已返回默认值: {}", def, e);
            return def;
        }
    }

    /**
     * 沙箱执行 (默认函数可能抛出异常).
     *
     * @param <R> the type parameter
     * @param fun the fun
     * @param def the def
     * @return the r
     */
    public static <R> R sandbox(Function.Zero2r<R> fun, Function.Zero2r<R> def) {
        try {
            return fun.run();
        } catch (Throwable e) {
            R ret = suppress(def);
            logger.info("沙箱异常已返回默认值: {}", ret, e);
            return ret;
        }
    }

    /**
     * Sandbox r.
     *
     * @param <R> the type parameter
     * @param fun the fun
     * @param def the def
     * @return the r
     */
    public static <R> R sandbox(Function.Zero2r<R> fun, Function.P2r<Throwable, R> def) {
        try {
            return fun.run();
        } catch (Throwable e) {
            // 已经返回异常则不打印日志
            return suppress(() -> def.run(e));
        }
    }

    /**
     * 压制异常 (有异常仍然抛出).
     *
     * @param fun the fun
     */
    public static void suppress(Function.Zero fun) {
        try {
            fun.run();
        } catch (Throwable e) {
            cast(e);
        }
    }

    /**
     * 压制异常 (有异常仍然抛出).
     *
     * @param fun the fun
     */
    public static void suppress(Function.Zero fun, Function.One<Throwable> run) {
        try {
            fun.run();
        } catch (Throwable e) {
            // 先执行异常处理
            sandbox(() -> run.run(e));
            // 再抛出异常
            cast(e);
        }
    }

    /**
     * 压制异常 (有异常仍然抛出).
     *
     * @param <R> the type parameter
     * @param fun the fun
     * @return the def
     */
    public static <R> R suppress(Function.Zero2r<R> fun) {
        try {
            return fun.run();
        } catch (Throwable e) {
            return cast(e);
        }
    }

    /**
     * 压制异常 (有异常仍然抛出).
     *
     * @param fun the fun
     * @return the boolean
     */
    public static Boolean suppress(Function.Ok0 fun) {
        try {
            return fun.run();
        } catch (Throwable e) {
            return cast(e);
        }
    }

    /**
     * 获取异常所有堆栈信息.
     *
     * @param throwable the throwable
     * @return the string
     */
    public static String getAllMsg(Throwable throwable) {
        ByteArrayOutputStream all = new ByteArrayOutputStream();
        throwable.printStackTrace(new PrintWriter(all, true));
        return all.toString();
    }


    /**
     * 获取原始异常提示消息.
     *
     * @param throwable the throwable
     * @return origin msg
     */
    public static String getOriginMsg(Throwable throwable) {
        return cutMessage(cause(throwable));
    }


    /**
     * 获取原始异常类型
     *
     * @param e
     * @return
     */
    private static String cause(Throwable e) {
        Throwable cause = e.getCause();
        if (cause != null) {
            return e.getCause().getLocalizedMessage();
        }
        return e.getLocalizedMessage();
    }

    /**
     * 切割出最精简的消息
     *
     * @param message
     * @return
     */
    private static String cutMessage(String message) {
        if (!Objects.isNull(message)) {
            String[] split = message.split(EXCEPTION_MESSAGE_SPLIT);
            if (!Objects.isNull(split)) {
                String detailMessage = split[0];
                String[] messages = getMessages(split, detailMessage, 1);
                String str = messages.length > 1 ? messages[1] : messages[0];
                return str.split(LINE_MESSAGE_SPLIT)[0];
            }
        }
        return null;
    }

    /**
     * 获取具有意义的异常信息
     *
     * @param split
     * @param detailMessage
     * @return
     */
    private static String[] getMessages(String[] split, String detailMessage, int i) {
        //用默认为最后的的消息分割出有意义的提示字符
        String[] messages = detailMessage.split("Exception:");
        //获取的消息1下标信息字符数少于4个是没有意义的,如果之前分割的数组里面还有其他消息,那么看看下一组有没有
        if (messages.length > 1 && messages[1].length() < EXCEPTION_MESSAGE_SPLIT.length() - 1 && split.length > i) {
            detailMessage = split[i];
            return getMessages(split, detailMessage, i++);
        }
        return messages;
    }
}
