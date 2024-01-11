package cn.gmlee.tools.base.util;

import cn.gmlee.tools.base.enums.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * 敏捷工具.
 *
 * @author Jas °
 * @date 2021 /8/19 (周四)
 */
public class QuickUtil {

    private static final Logger logger = LoggerFactory.getLogger(QuickUtil.class);

    /**
     * 根据条件执行函数.
     *
     * @param condition 返回值是true则执行
     * @param run       the run
     */
    public static void exec(Function.Ok0 condition, Function.Zero run){
        Boolean ret = ExceptionUtil.suppress(condition);
        if(BoolUtil.isTrue(ret)){
            ExceptionUtil.suppress(run);
        }
    }

    /**
     * 根据条件执行函数.
     *
     * @param <R>       返回值不为空则执行
     * @param condition the condition
     * @param run       the run
     */
    public static <R> void exec(Function.Zero2r<R> condition, Function.Zero run){
        R ret = ExceptionUtil.suppress(condition);
        if(BoolUtil.notNull(ret)){
            ExceptionUtil.suppress(run);
        }
    }

    /**
     * 根据条件执行函数 (条件返回做入参).
     *
     * @param <P>       the type parameter
     * @param condition the condition
     * @param run       the run
     */
    public static <P> void exec(Function.Zero2r<P> condition, Function.One<P> run){
        P ret = ExceptionUtil.suppress(condition);
        if(BoolUtil.notNull(ret)){
            ExceptionUtil.suppress(() -> run.run(ret));
        }
    }

    /**
     * 根据条件执行函数 (返回执行结果).
     *
     * @param <R>       条件返回
     * @param condition 条件函数
     * @param run       执行函数
     * @return r 返回对象
     */
    public static <R> R exec(Function.Ok0 condition, Function.Zero2r<R> run){
        Boolean ret = ExceptionUtil.suppress(condition);
        if(BoolUtil.isTrue(ret)){
            return ExceptionUtil.suppress(run);
        }
        return null;
    }

    /**
     * 根据条件执行函数 (返回执行结果).
     *
     * @param <R>       条件返回
     * @param condition 条件函数
     * @param run       执行函数
     * @return r 返回对象
     */
    public static <R> R exec(Function.Zero2r<R> condition, Function.Zero2r<R> run){
        R ret = ExceptionUtil.suppress(condition);
        if(BoolUtil.notNull(ret)){
            return ExceptionUtil.suppress(run);
        }
        return null;
    }

    /**
     * 根据条件执行函数 (返回执行结果).
     *
     * @param <P>       将条件返回作为函数入参
     * @param <R>       函数返回类型
     * @param condition 条件函数
     * @param run       执行函数
     * @return r 返回对象
     */
    public static <P, R> R exec(Function.Zero2r<P> condition, Function.P2r<P,R> run){
        P ret = ExceptionUtil.suppress(condition);
        if(BoolUtil.notNull(ret)){
            return ExceptionUtil.suppress(() -> run.run(ret));
        }
        return null;
    }

    /**
     * Is true.
     *
     * @param o   the o
     * @param run the run
     */
    public static void isTrue(Boolean o, Function.Zero run) {
        try {
            if (BoolUtil.isTrue(o)) {
                run.run();
            }
        } catch (Throwable throwable) {
            logger.error("敏捷工具提示: ", throwable);
            ExceptionUtil.cast(throwable);
        }
    }

    /**
     * Is false.
     *
     * @param o   the o
     * @param run the run
     */
    public static void isFalse(Boolean o, Function.Zero run) {
        try {
            if (BoolUtil.isFalse(o)) {
                run.run();
            }
        } catch (Throwable throwable) {
            logger.error("敏捷工具提示: ", throwable);
            ExceptionUtil.cast(throwable);
        }
    }

    /**
     * Eq.
     *
     * @param <T>    the type parameter
     * @param source the source
     * @param target the target
     * @param run    the run
     */
    public static <T> void eq(T source, T target, Function.Zero run) {
        try {
            if (BoolUtil.eq(source, target)) {
                run.run();
            }
        } catch (Throwable throwable) {
            logger.error("敏捷工具提示: ", throwable);
            ExceptionUtil.cast(throwable);
        }
    }

    /**
     * Eq.
     *
     * @param <T>    the type parameter
     * @param source the source
     * @param target the target
     * @param run    the run
     */
    public static <T> void eq(T source, T target, Function.One<T> run) {
        try {
            if (BoolUtil.eq(source, target)) {
                run.run(source);
            }
        } catch (Throwable throwable) {
            logger.error("敏捷工具提示: ", throwable);
            ExceptionUtil.cast(throwable);
        }
    }

    /**
     * 包含才执行.
     *
     * @param source the source
     * @param target the target
     * @param run    the run
     * @return the boolean
     */
    public static void contain(String source, String target, Function.Zero run) {
        try {
            if (BoolUtil.contain(source, target)) {
                run.run();
            }
        } catch (Throwable throwable) {
            logger.error("敏捷工具提示: ", throwable);
            ExceptionUtil.cast(throwable);
        }
    }

    /**
     * 包含才执行.
     *
     * @param run     the run
     * @param source  the source
     * @param targets the targets
     * @return the boolean
     */
    public static void containOne(Function.Zero run, String source, String... targets) {
        try {
            if (BoolUtil.containOne(source, targets)) {
                run.run();
            }
        } catch (Throwable throwable) {
            logger.error("敏捷工具提示: ", throwable);
            ExceptionUtil.cast(throwable);
        }
    }

    /**
     * 包含才执行.
     *
     * @param <T>     the type parameter
     * @param run     the run
     * @param source  the source
     * @param targets the targets
     */
    public static <T> void containOne(Function.Zero run, Collection<T> source, T... targets) {
        try {
            if (BoolUtil.containOne(source, targets)) {
                run.run();
            }
        } catch (Throwable throwable) {
            logger.error("敏捷工具提示: ", throwable);
            ExceptionUtil.cast(throwable);
        }
    }

    /**
     * 包含才执行.
     *
     * @param <T>     the type parameter
     * @param run     the run
     * @param source  the source
     * @param targets the targets
     */
    public static <T> void containOne(Function.Zero run, T[] source, T... targets) {
        try {
            if (BoolUtil.containOne(source, targets)) {
                run.run();
            }
        } catch (Throwable throwable) {
            logger.error("敏捷工具提示: ", throwable);
            ExceptionUtil.cast(throwable);
        }
    }


    /**
     * Not empty.
     *
     * @param <C> the type parameter
     * @param o   the o
     * @param run the run
     */
    public static <C extends Collection> void notEmpty(C o, Function.Zero run) {
        try {
            if (BoolUtil.notEmpty(o)) {
                run.run();
            }
        } catch (Throwable throwable) {
            logger.error("敏捷工具提示: ", throwable);
            ExceptionUtil.cast(throwable);
        }
    }

    /**
     * Is empty.
     *
     * @param <C> the type parameter
     * @param o   the o
     * @param run the run
     */
    public static <C extends Collection> void isEmpty(C o, Function.Zero run) {
        try {
            if (BoolUtil.isEmpty(o)) {
                run.run();
            }
        } catch (Throwable throwable) {
            logger.error("敏捷工具提示: ", throwable);
            ExceptionUtil.cast(throwable);
        }
    }

    /**
     * Not empty.
     *
     * @param <C> the type parameter
     * @param o   the o
     * @param run the run
     */
    public static <C extends Map> void notEmpty(C o, Function.One<C> run) {
        try {
            if (BoolUtil.notEmpty(o)) {
                run.run(o);
            }
        } catch (Throwable throwable) {
            logger.error("敏捷工具提示: ", throwable);
            ExceptionUtil.cast(throwable);
        }
    }

    /**
     * Is empty.
     *
     * @param <C> the type parameter
     * @param o   the o
     * @param run the run
     */
    public static <C extends Map> void isEmpty(C o, Function.Zero run) {
        try {
            if (BoolUtil.isEmpty(o)) {
                run.run();
            }
        } catch (Throwable throwable) {
            logger.error("敏捷工具提示: ", throwable);
            ExceptionUtil.cast(throwable);
        }
    }

    /**
     * Not empty.
     *
     * @param <C> the type parameter
     * @param o   the o
     * @param run the run
     */
    public static <C extends Collection> void notEmpty(C o, Function.One<C> run) {
        try {
            if (BoolUtil.notEmpty(o)) {
                run.run(o);
            }
        } catch (Throwable throwable) {
            logger.error("敏捷工具提示: ", throwable);
            ExceptionUtil.cast(throwable);
        }
    }

    /**
     * Is empty.
     *
     * @param <O> the type parameter
     * @param o   the o
     * @param run the run
     */
    public static <O extends CharSequence> void isEmpty(O o, Function.One<O> run) {
        try {
            if (BoolUtil.isEmpty(o)) {
                run.run(o);
            }
        } catch (Throwable throwable) {
            logger.error("敏捷工具提示: ", throwable);
            ExceptionUtil.cast(throwable);
        }
    }

    /**
     * Is empty.
     *
     * @param <O> the type parameter
     * @param o   the o
     * @param run the run
     */
    public static <O extends CharSequence> void isEmpty(O o, Function.Zero run) {
        try {
            if (BoolUtil.isEmpty(o)) {
                run.run();
            }
        } catch (Throwable throwable) {
            logger.error("敏捷工具提示: ", throwable);
            ExceptionUtil.cast(throwable);
        }
    }

    /**
     * 非空才执行.
     *
     * @param <O> the type parameter
     * @param o   the o
     * @param run the run
     */
    public static <O extends CharSequence> void notEmpty(O o, Function.One<O> run) {
        try {
            if (BoolUtil.notEmpty(o)) {
                run.run(o);
            }
        } catch (Throwable throwable) {
            logger.error("敏捷工具提示: ", throwable);
            ExceptionUtil.cast(throwable);
        }
    }


    /**
     * Is empty.
     *
     * @param <O> the type parameter
     * @param o   the o
     * @param run the run
     */
    public static <O extends Collection> void isEmpty(O o, Function.One<O> run) {
        try {
            if (BoolUtil.isEmpty(o)) {
                run.run(o);
            }
        } catch (Throwable throwable) {
            logger.error("敏捷工具提示: ", throwable);
            ExceptionUtil.cast(throwable);
        }
    }

    /**
     * 为空才执行.
     *
     * @param <O> the type parameter
     * @param os  the os
     * @param run the run
     */
    public static <O> void notEmpty(O[] os, Function.One<O[]> run) {
        try {
            if (BoolUtil.notEmpty(os)) {
                run.run(os);
            }
        } catch (Throwable throwable) {
            logger.error("敏捷工具提示: ", throwable);
            ExceptionUtil.cast(throwable);
        }
    }

    /**
     * Is null.
     *
     * @param <T> the type parameter
     * @param o   the o
     * @param run the run
     */
    public static <T> void isNull(T o, Function.Zero run) {
        try {
            if (BoolUtil.isNull(o)) {
                run.run();
            }
        } catch (Throwable throwable) {
            logger.error("敏捷工具提示: ", throwable);
            ExceptionUtil.cast(throwable);
        }
    }

    /**
     * 非空才执行.
     *
     * @param <T> the type parameter
     * @param o   the o
     * @param run the run
     */
    public static <T> void notNull(T o, Function.One<T> run) {
        try {
            if (BoolUtil.notNull(o)) {
                run.run(o);
            }
        } catch (Throwable throwable) {
            logger.error("敏捷工具提示: ", throwable);
            ExceptionUtil.cast(throwable);
        }
    }

    /**
     * 双元执行.
     * <p>
     * 代替三元运算符.
     * </p>
     *
     * @param ok  the ok
     * @param yes the yes
     * @param no  the no
     * @return the t
     */
    public static void is(Boolean ok, Function.Zero yes, Function.Zero no) {
        is(x -> ok, yes, no);
    }

    /**
     * 双元执行.
     * <p>
     * 代替三元运算符.
     * </p>
     *
     * @param <T> the type parameter
     * @param ok  the ok
     * @param yes the yes
     * @param no  the no
     * @return the t
     */
    public static <T> T is(Boolean ok, Function.Zero2r<T> yes, Function.Zero2r<T> no) {
        return is(x -> ok, yes, no);
    }

    /**
     * 三元执行.
     * <p>
     * 代替三元运算符.
     * </p>
     *
     * @param check the check
     * @param yes   the yes
     * @param no    the no
     * @return the t
     */
    public static void is(Function.OkOs check, Function.Zero yes, Function.Zero no) {
        try {
            if (BoolUtil.isTrue(check.run())) {
                yes.run();
            } else {
                no.run();
            }
        } catch (Throwable throwable) {
            logger.error("敏捷工具提示: ", throwable);
            ExceptionUtil.cast(throwable);
        }
    }

    /**
     * 三元执行.
     * <p>
     * 代替三元运算符.
     * </p>
     *
     * @param <T>   the type parameter
     * @param check the check
     * @param yes   the yes
     * @param no    the no
     * @return the t
     */
    public static <T> T is(Function.OkOs check, Function.Zero2r<T> yes, Function.Zero2r<T> no) {
        try {
            if (BoolUtil.isTrue(check.run())) {
                return yes.run();
            } else {
                return no.run();
            }
        } catch (Throwable throwable) {
            logger.error("敏捷工具提示: ", throwable);
            return ExceptionUtil.cast(throwable);
        }
    }


    /**
     * 分批处理工具.
     *
     * @param <P>  the type parameter
     * @param <R>  the type parameter
     * @param c    the c
     * @param size the size
     * @param run  the run
     * @return list list
     */
    public static <P, R> List<R> batch(Collection<P> c, int size, Function.P2r<List<P>, R> run) {
        List<R> rs = new ArrayList();
        if (BoolUtil.isEmpty(c)) {
            return rs;
        }
        Object[] array = c.toArray();
        int batch = c.size() / size;
        for (int i = 0, b = 0; i < c.size() && b <= batch; b++, i += size) {
            int to = i + size;
            if (to > c.size()) {
                to = c.size();
            }
            P[] sub = (P[]) Arrays.copyOfRange(array, i, to);
            try {
                rs.add(run.run(Arrays.asList(sub)));
            } catch (Throwable e) {
                ExceptionUtil.cast(String.format("敏捷工具异常"), e);
            }
        }
        return rs;
    }


    /**
     * 分批处理工具.
     *
     * @param <P>  the type parameter
     * @param c    the c
     * @param size the size
     * @param run  the run
     */
    public static <P> void batch(Collection<P> c, int size, Function.P2v<List<P>> run) {
        if (BoolUtil.isEmpty(c)) {
            return;
        }
        Object[] array = c.toArray();
        int batch = c.size() / size;
        for (int i = 0, b = 0; i < c.size() && b <= batch; b++, i += size) {
            int to = i + size;
            if (to > c.size()) {
                to = c.size();
            }
            P[] sub = (P[]) Arrays.copyOfRange(array, i, to);
            try {
                run.run(Arrays.asList(sub));
            } catch (Throwable e) {
                ExceptionUtil.cast(String.format("敏捷工具异常"), e);
            }
        }
    }

    /**
     * 分批处理工具.
     *
     * @param <P>  the type parameter
     * @param <R>  the type parameter
     * @param c    the c
     * @param size the size
     * @param run  the run
     * @return the list
     */
    public static <P, R> List<R> batch(P[] c, int size, Function.P2r<P[], R> run) {
        List<R> rs = new ArrayList();
        if (BoolUtil.isEmpty(c)) {
            return rs;
        }
        int batch = c.length / size;
        for (int i = 0, b = 0; i < c.length && b <= batch; b++, i += size) {
            int to = i + size;
            if (to > c.length) {
                to = c.length;
            }
            P[] sub = Arrays.copyOfRange(c, i, to);
            try {
                rs.add(run.run(sub));
            } catch (Throwable e) {
                ExceptionUtil.cast(String.format("敏捷工具异常"), e);
            }
        }
        return rs;
    }

    /**
     * 分批处理工具.
     *
     * @param <P>  the type parameter
     * @param c    the c
     * @param size the size
     * @param run  the run
     */
    public static <P> void batch(P[] c, int size, Function.P2v<P[]> run) {
        if (BoolUtil.isEmpty(c)) {
            return;
        }
        int batch = c.length / size;
        for (int i = 0, b = 0; i < c.length && b <= batch; b++, i += size) {
            int to = i + size;
            if (to > c.length) {
                to = c.length;
            }
            P[] sub = Arrays.copyOfRange(c, i, to);
            try {
                run.run(sub);
            } catch (Throwable e) {
                ExceptionUtil.cast(String.format("敏捷工具异常"), e);
            }
        }
    }
}
