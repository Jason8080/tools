package cn.gmlee.tools.base.util;

import cn.gmlee.tools.base.ex.AssertException;
import cn.gmlee.tools.base.ex.RemoteInvokeException;
import cn.gmlee.tools.base.mod.R;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 通用断言工具
 *
 * @author Jas °
 * @date 2020 /9/28 (周一)
 */
public class AssertUtil {

    /**
     * Is digit.
     *
     * @param cs  the cs
     * @param msg the msg
     */
    public static void isDigit(CharSequence cs, String msg) {
        boolean ok = BoolUtil.isDigit(cs);
        if (!ok) {
            throw new AssertException(msg);
        }
    }

    /**
     * Is digit.
     *
     * @param <X>      the type parameter
     * @param cs       the cs
     * @param supplier the supplier
     * @throws X the x
     */
    public static <X extends Throwable> void isDigit(CharSequence cs, Supplier<? extends X> supplier) throws X {
        boolean ok = BoolUtil.isDigit(cs);
        if (!ok) {
            throw supplier.get();
        }
    }

    /**
     * Is ok.
     *
     * @param o   the o
     * @param msg the msg
     */
    public static void isOk(R o, String msg) {
        boolean ok = BoolUtil.isOk(o);
        if (!ok) {
            throw new AssertException(msg, new RemoteInvokeException(o));
        }
    }

    /**
     * Is ok.
     *
     * @param <X>      the type parameter
     * @param o        the o
     * @param supplier the supplier
     * @throws X the x
     */
    public static <X extends Throwable> void isOk(R o, Supplier<? extends X> supplier) throws X {
        boolean ok = BoolUtil.isOk(o);
        if (!ok) {
            throw supplier.get();
        }
    }

    /**
     * Is true.
     *
     * @param <X>      the type parameter
     * @param o        the o
     * @param supplier the supplier
     * @throws X the x
     */
    public static <X extends Throwable> void isTrue(Boolean o, Supplier<? extends X> supplier) throws X {
        boolean aTrue = BoolUtil.isTrue(o);
        if (!aTrue) {
            throw supplier.get();
        }
    }

    /**
     * Is false.
     *
     * @param <X>      the type parameter
     * @param o        the o
     * @param supplier the supplier
     * @throws X the x
     */
    public static <X extends Throwable> void isFalse(Boolean o, Supplier<? extends X> supplier) throws X {
        boolean aFalse = BoolUtil.isFalse(o);
        if (!aFalse) {
            throw supplier.get();
        }
    }

    /**
     * Is true.
     *
     * @param o   the o
     * @param msg the msg
     */
    public static void isTrue(Boolean o, String msg) {
        boolean aTrue = BoolUtil.isTrue(o);
        if (!aTrue) {
            throw new AssertException(msg);
        }
    }

    /**
     * Is false.
     *
     * @param o   the o
     * @param msg the msg
     */
    public static void isFalse(Boolean o, String msg) {
        boolean aFalse = BoolUtil.isFalse(o);
        if (!aFalse) {
            throw new AssertException(msg);
        }
    }

    /**
     * All true.
     *
     * @param <X>      the type parameter
     * @param supplier the supplier
     * @param o        the o
     * @param os       the os
     * @throws X the x
     */
    public static <X extends Throwable> void allTrue(Supplier<? extends X> supplier, Boolean o, Boolean... os) throws X {
        boolean allTrue = BoolUtil.allTrue(o, os);
        if (!allTrue) {
            throw supplier.get();
        }
    }

    /**
     * All false.
     *
     * @param <X>      the type parameter
     * @param supplier the supplier
     * @param o        the o
     * @param os       the os
     * @throws X the x
     */
    public static <X extends Throwable> void allFalse(Supplier<? extends X> supplier, Boolean o, Boolean... os) throws X {
        boolean allFalse = BoolUtil.allFalse(o, os);
        if (!allFalse) {
            throw supplier.get();
        }
    }

    /**
     * All true.
     *
     * @param msg the msg
     * @param o   the o
     * @param os  the os
     */
    public static void allTrue(String msg, Boolean o, Boolean... os) {
        boolean allTrue = BoolUtil.allTrue(o, os);
        if (!allTrue) {
            throw new AssertException(msg);
        }
    }

    /**
     * All false.
     *
     * @param msg the msg
     * @param o   the o
     * @param os  the os
     */
    public static void allFalse(String msg, Boolean o, Boolean... os) {
        boolean allFalse = BoolUtil.allFalse(o, os);
        if (!allFalse) {
            throw new AssertException(msg);
        }
    }

    /**
     * Is empty.
     *
     * @param <X>      the type parameter
     * @param o        the o
     * @param supplier the supplier
     * @throws X the x
     */
    public static <X extends Throwable> void isEmpty(Collection o, Supplier<? extends X> supplier) throws X {
        boolean empty = BoolUtil.isEmpty(o);
        if (!empty) {
            throw supplier.get();
        }
    }

    /**
     * Is empty.
     *
     * @param <X>      the type parameter
     * @param o        the o
     * @param supplier the supplier
     * @throws X the x
     */
    public static <X extends Throwable> void isEmpty(Map o, Supplier<? extends X> supplier) throws X {
        boolean empty = BoolUtil.isEmpty(o);
        if (!empty) {
            throw supplier.get();
        }
    }

    /**
     * Is empty.
     *
     * @param <X>      the type parameter
     * @param o        the o
     * @param supplier the supplier
     * @throws X the x
     */
    public static <X extends Throwable> void isEmpty(Collection[] o, Supplier<? extends X> supplier) throws X {
        boolean empty = BoolUtil.isEmpty(o);
        if (!empty) {
            throw supplier.get();
        }
    }

    /**
     * Is empty.
     *
     * @param o   the o
     * @param msg the msg
     */
    public static void isEmpty(Collection o, String msg) {
        boolean empty = BoolUtil.isEmpty(o);
        if (!empty) {
            throw new AssertException(msg);
        }
    }

    /**
     * Is empty.
     *
     * @param o   the o
     * @param msg the msg
     */
    public static void isEmpty(Map o, String msg) {
        boolean empty = BoolUtil.isEmpty(o);
        if (!empty) {
            throw new AssertException(msg);
        }
    }

    /**
     * Is empty.
     *
     * @param o   the o
     * @param msg the msg
     */
    public static void isEmpty(Collection[] o, String msg) {
        boolean empty = BoolUtil.isEmpty(o);
        if (!empty) {
            throw new AssertException(msg);
        }
    }

    /**
     * Any empty.
     *
     * @param <X>      the type parameter
     * @param supplier the supplier
     * @param o        the o
     * @param os       the os
     * @throws X the x
     */
    public static <X extends Throwable> void anyEmpty(Supplier<? extends X> supplier, Collection o, Collection... os) throws X {
        boolean empty = BoolUtil.anyEmpty(o, os);
        if (!empty) {
            throw supplier.get();
        }
    }

    /**
     * Any empty.
     *
     * @param <X>      the type parameter
     * @param supplier the supplier
     * @param o        the o
     * @param os       the os
     * @throws X the x
     */
    public static <X extends Throwable> void anyEmpty(Supplier<? extends X> supplier, Map o, Map... os) throws X {
        boolean empty = BoolUtil.anyEmpty(o, os);
        if (!empty) {
            throw supplier.get();
        }
    }

    /**
     * Any empty.
     *
     * @param <X>      the type parameter
     * @param supplier the supplier
     * @param os       the os
     * @param oss      the oss
     * @throws X the x
     */
    public static <X extends Throwable> void anyEmpty(Supplier<? extends X> supplier, Collection[] os, Collection... oss) throws X {
        boolean empty = BoolUtil.anyEmpty(os, oss);
        if (!empty) {
            throw supplier.get();
        }
    }

    /**
     * Any empty.
     *
     * @param msg the msg
     * @param o   the o
     * @param os  the os
     */
    public static void anyEmpty(String msg, Collection o, Collection... os) {
        boolean empty = BoolUtil.anyEmpty(o, os);
        if (!empty) {
            throw new AssertException(msg);
        }
    }

    /**
     * Any empty.
     *
     * @param msg the msg
     * @param o   the o
     * @param os  the os
     */
    public static void anyEmpty(String msg, Map o, Map... os) {
        boolean empty = BoolUtil.anyEmpty(o, os);
        if (!empty) {
            throw new AssertException(msg);
        }
    }

    /**
     * Any empty.
     *
     * @param msg the msg
     * @param os  the os
     * @param oss the oss
     */
    public static void anyEmpty(String msg, Collection[] os, Collection... oss) {
        boolean empty = BoolUtil.anyEmpty(os, oss);
        if (!empty) {
            throw new AssertException(msg);
        }
    }

    /**
     * All empty.
     *
     * @param <X>      the type parameter
     * @param supplier the supplier
     * @param o        the o
     * @param os       the os
     * @throws X the x
     */
    public static <X extends Throwable> void allEmpty(Supplier<? extends X> supplier, Collection o, Collection... os) throws X {
        boolean empty = BoolUtil.allEmpty(o, os);
        if (!empty) {
            throw supplier.get();
        }
    }

    /**
     * All empty.
     *
     * @param <X>      the type parameter
     * @param supplier the supplier
     * @param o        the o
     * @param os       the os
     * @throws X the x
     */
    public static <X extends Throwable> void allEmpty(Supplier<? extends X> supplier, Map o, Map... os) throws X {
        boolean empty = BoolUtil.allEmpty(o, os);
        if (!empty) {
            throw supplier.get();
        }
    }

    /**
     * All empty.
     *
     * @param <X>      the type parameter
     * @param supplier the supplier
     * @param os       the os
     * @param oss      the oss
     * @throws X the x
     */
    public static <X extends Throwable> void allEmpty(Supplier<? extends X> supplier, Collection[] os, Collection... oss) throws X {
        boolean empty = BoolUtil.allEmpty(os, oss);
        if (!empty) {
            throw supplier.get();
        }
    }

    /**
     * All empty.
     *
     * @param msg the msg
     * @param o   the o
     * @param os  the os
     */
    public static void allEmpty(String msg, Collection o, Collection... os) {
        boolean empty = BoolUtil.allEmpty(o, os);
        if (!empty) {
            throw new AssertException(msg);
        }
    }

    /**
     * All empty.
     *
     * @param msg the msg
     * @param o   the o
     * @param os  the os
     */
    public static void allEmpty(String msg, Map o, Map... os) {
        boolean empty = BoolUtil.allEmpty(o, os);
        if (!empty) {
            throw new AssertException(msg);
        }
    }

    /**
     * All empty.
     *
     * @param msg the msg
     * @param os  the os
     * @param oss the oss
     */
    public static void allEmpty(String msg, Collection[] os, Collection... oss) {
        boolean empty = BoolUtil.allEmpty(os, oss);
        if (!empty) {
            throw new AssertException(msg);
        }
    }

    /**
     * Not empty.
     *
     * @param <X>      the type parameter
     * @param o        the o
     * @param supplier the supplier
     * @throws X the x
     */
    public static <X extends Throwable> void notEmpty(Collection o, Supplier<? extends X> supplier) throws X {
        boolean notEmpty = BoolUtil.notEmpty(o);
        if (!notEmpty) {
            throw supplier.get();
        }
    }

    /**
     * Not empty.
     *
     * @param <X>      the type parameter
     * @param o        the o
     * @param supplier the supplier
     * @throws X the x
     */
    public static <X extends Throwable> void notEmpty(Map o, Supplier<? extends X> supplier) throws X {
        boolean notEmpty = BoolUtil.notEmpty(o);
        if (!notEmpty) {
            throw supplier.get();
        }
    }

    /**
     * Not empty.
     *
     * @param <X>      the type parameter
     * @param o        the o
     * @param supplier the supplier
     * @throws X the x
     */
    public static <X extends Throwable> void notEmpty(Collection[] o, Supplier<? extends X> supplier) throws X {
        boolean notEmpty = BoolUtil.notEmpty(o);
        if (!notEmpty) {
            throw supplier.get();
        }
    }

    /**
     * Not empty.
     *
     * @param o   the o
     * @param msg the msg
     */
    public static void notEmpty(Collection o, String msg) {
        boolean notEmpty = BoolUtil.notEmpty(o);
        if (!notEmpty) {
            throw new AssertException(msg);
        }
    }

    /**
     * Not empty.
     *
     * @param o   the o
     * @param msg the msg
     */
    public static void notEmpty(Map o, String msg) {
        boolean notEmpty = BoolUtil.notEmpty(o);
        if (!notEmpty) {
            throw new AssertException(msg);
        }
    }

    /**
     * Not empty.
     *
     * @param o   the o
     * @param msg the msg
     */
    public static void notEmpty(Collection[] o, String msg) {
        boolean notEmpty = BoolUtil.notEmpty(o);
        if (!notEmpty) {
            throw new AssertException(msg);
        }
    }

    /**
     * Any not empty.
     *
     * @param <X>      the type parameter
     * @param supplier the supplier
     * @param o        the o
     * @param os       the os
     * @throws X the x
     */
    public static <X extends Throwable> void anyNotEmpty(Supplier<? extends X> supplier, Collection o, Collection... os) throws X {
        boolean notEmpty = BoolUtil.anyNotEmpty(o, os);
        if (!notEmpty) {
            throw supplier.get();
        }
    }

    /**
     * Any not empty.
     *
     * @param <X>      the type parameter
     * @param supplier the supplier
     * @param o        the o
     * @param os       the os
     * @throws X the x
     */
    public static <X extends Throwable> void anyNotEmpty(Supplier<? extends X> supplier, Map o, Map... os) throws X {
        boolean notEmpty = BoolUtil.anyNotEmpty(o, os);
        if (!notEmpty) {
            throw supplier.get();
        }
    }

    /**
     * Any not empty.
     *
     * @param <X>      the type parameter
     * @param supplier the supplier
     * @param o        the o
     * @param os       the os
     * @throws X the x
     */
    public static <X extends Throwable> void anyNotEmpty(Supplier<? extends X> supplier, Collection[] o, Collection... os) throws X {
        boolean notEmpty = BoolUtil.anyNotEmpty(o, os);
        if (!notEmpty) {
            throw supplier.get();
        }
    }

    /**
     * Any not empty.
     *
     * @param msg the msg
     * @param o   the o
     * @param os  the os
     */
    public static void anyNotEmpty(String msg, Collection o, Collection... os) {
        boolean notEmpty = BoolUtil.anyNotEmpty(o, os);
        if (!notEmpty) {
            throw new AssertException(msg);
        }
    }

    /**
     * Any not empty.
     *
     * @param msg the msg
     * @param o   the o
     * @param os  the os
     */
    public static void anyNotEmpty(String msg, Map o, Map... os) {
        boolean notEmpty = BoolUtil.anyNotEmpty(o, os);
        if (!notEmpty) {
            throw new AssertException(msg);
        }
    }

    /**
     * Any not empty.
     *
     * @param msg the msg
     * @param o   the o
     * @param os  the os
     */
    public static void anyNotEmpty(String msg, Collection[] o, Collection... os) {
        boolean notEmpty = BoolUtil.anyNotEmpty(o, os);
        if (!notEmpty) {
            throw new AssertException(msg);
        }
    }

    /**
     * All not empty.
     *
     * @param <X>      the type parameter
     * @param supplier the supplier
     * @param o        the o
     * @param os       the os
     * @throws X the x
     */
    public static <X extends Throwable> void allNotEmpty(Supplier<? extends X> supplier, Collection o, Collection... os) throws X {
        boolean notEmpty = BoolUtil.allNotEmpty(o, os);
        if (!notEmpty) {
            throw supplier.get();
        }
    }

    /**
     * All not empty.
     *
     * @param <X>      the type parameter
     * @param supplier the supplier
     * @param o        the o
     * @param os       the os
     * @throws X the x
     */
    public static <X extends Throwable> void allNotEmpty(Supplier<? extends X> supplier, Map o, Map... os) throws X {
        boolean notEmpty = BoolUtil.allNotEmpty(o, os);
        if (!notEmpty) {
            throw supplier.get();
        }
    }

    /**
     * All not empty.
     *
     * @param <X>      the type parameter
     * @param supplier the supplier
     * @param o        the o
     * @param os       the os
     * @throws X the x
     */
    public static <X extends Throwable> void allNotEmpty(Supplier<? extends X> supplier, Collection[] o, Collection... os) throws X {
        boolean notEmpty = BoolUtil.allNotEmpty(o, os);
        if (!notEmpty) {
            throw supplier.get();
        }
    }

    /**
     * All not empty.
     *
     * @param msg the msg
     * @param o   the o
     * @param os  the os
     */
    public static void allNotEmpty(String msg, Collection o, Collection... os) {
        boolean notEmpty = BoolUtil.allNotEmpty(o, os);
        if (!notEmpty) {
            throw new AssertException(msg);
        }
    }

    /**
     * All not empty.
     *
     * @param msg the msg
     * @param o   the o
     * @param os  the os
     */
    public static void allNotEmpty(String msg, Map o, Map... os) {
        boolean notEmpty = BoolUtil.allNotEmpty(o, os);
        if (!notEmpty) {
            throw new AssertException(msg);
        }
    }

    /**
     * All not empty.
     *
     * @param msg the msg
     * @param o   the o
     * @param os  the os
     */
    public static void allNotEmpty(String msg, Collection[] o, Collection... os) {
        boolean notEmpty = BoolUtil.allNotEmpty(o, os);
        if (!notEmpty) {
            throw new AssertException(msg);
        }
    }

    /**
     * Is empty.
     *
     * @param <X>      the type parameter
     * @param o        the o
     * @param supplier the supplier
     * @throws X the x
     */
    public static <X extends Throwable> void isEmpty(Object[] o, Supplier<? extends X> supplier) throws X {
        boolean empty = BoolUtil.isEmpty(o);
        if (!empty) {
            throw supplier.get();
        }
    }

    /**
     * Is empty.
     *
     * @param o   the o
     * @param msg the msg
     */
    public static void isEmpty(Object[] o, String msg) {
        boolean empty = BoolUtil.isEmpty(o);
        if (!empty) {
            throw new AssertException(msg);
        }
    }

    /**
     * Any empty.
     *
     * @param <X>      the type parameter
     * @param supplier the supplier
     * @param o        the o
     * @param os       the os
     * @throws X the x
     */
    public static <X extends Throwable> void anyEmpty(Supplier<? extends X> supplier, Object[] o, Object... os) throws X {
        boolean empty = BoolUtil.anyEmpty(o, os);
        if (!empty) {
            throw supplier.get();
        }
    }

    /**
     * All empty.
     *
     * @param <X>      the type parameter
     * @param supplier the supplier
     * @param o        the o
     * @param os       the os
     * @throws X the x
     */
    public static <X extends Throwable> void allEmpty(Supplier<? extends X> supplier, Object[] o, Object... os) throws X {
        boolean empty = BoolUtil.allEmpty(o, os);
        if (!empty) {
            throw supplier.get();
        }
    }

    /**
     * Any empty.
     *
     * @param msg the msg
     * @param o   the o
     * @param os  the os
     */
    public static void anyEmpty(String msg, Object[] o, Object... os) {
        boolean empty = BoolUtil.anyEmpty(o, os);
        if (!empty) {
            throw new AssertException(msg);
        }
    }

    /**
     * All empty.
     *
     * @param msg the msg
     * @param o   the o
     * @param os  the os
     */
    public static void allEmpty(String msg, Object[] o, Object... os) {
        boolean empty = BoolUtil.allEmpty(o, os);
        if (!empty) {
            throw new AssertException(msg);
        }
    }

    /**
     * Not empty.
     *
     * @param <X>      the type parameter
     * @param o        the o
     * @param supplier the supplier
     * @throws X the x
     */
    public static <X extends Throwable> void notEmpty(Object[] o, Supplier<? extends X> supplier) throws X {
        boolean notEmpty = BoolUtil.notEmpty(o);
        if (!notEmpty) {
            throw supplier.get();
        }
    }

    /**
     * Not empty.
     *
     * @param o   the o
     * @param msg the msg
     */
    public static void notEmpty(Object[] o, String msg) {
        boolean notEmpty = BoolUtil.notEmpty(o);
        if (!notEmpty) {
            throw new AssertException(msg);
        }
    }

    /**
     * Any not empty.
     *
     * @param <X>      the type parameter
     * @param supplier the supplier
     * @param o        the o
     * @param os       the os
     * @throws X the x
     */
    public static <X extends Throwable> void anyNotEmpty(Supplier<? extends X> supplier, Object[] o, Object... os) throws X {
        boolean notEmpty = BoolUtil.anyNotEmpty(o, os);
        if (!notEmpty) {
            throw supplier.get();
        }
    }

    /**
     * Any not empty.
     *
     * @param msg the msg
     * @param o   the o
     * @param os  the os
     */
    public static void anyNotEmpty(String msg, Object[] o, Object... os) {
        boolean notEmpty = BoolUtil.anyNotEmpty(o, os);
        if (!notEmpty) {
            throw new AssertException(msg);
        }
    }

    /**
     * All not empty.
     *
     * @param <X>      the type parameter
     * @param supplier the supplier
     * @param o        the o
     * @param os       the os
     * @throws X the x
     */
    public static <X extends Throwable> void allNotEmpty(Supplier<? extends X> supplier, Object[] o, Object... os) throws X {
        boolean notEmpty = BoolUtil.allNotEmpty(o, os);
        if (!notEmpty) {
            throw supplier.get();
        }
    }

    /**
     * All not empty.
     *
     * @param msg the msg
     * @param o   the o
     * @param os  the os
     */
    public static void allNotEmpty(String msg, Object[] o, Object... os) {
        boolean notEmpty = BoolUtil.allNotEmpty(o, os);
        if (!notEmpty) {
            throw new AssertException(msg);
        }
    }

    /**
     * Is empty.
     *
     * @param <X>      the type parameter
     * @param o        the o
     * @param supplier the supplier
     * @throws X the x
     */
    public static <X extends Throwable> void isEmpty(String o, Supplier<? extends X> supplier) throws X {
        boolean empty = BoolUtil.isEmpty(o);
        if (!empty) {
            throw supplier.get();
        }
    }

    /**
     * Is empty.
     *
     * @param o   the o
     * @param msg the msg
     */
    public static void isEmpty(String o, String msg) {
        boolean empty = BoolUtil.isEmpty(o);
        if (!empty) {
            throw new AssertException(msg);
        }
    }

    /**
     * Any empty.
     *
     * @param <X>      the type parameter
     * @param supplier the supplier
     * @param o        the o
     * @param os       the os
     * @throws X the x
     */
    public static <X extends Throwable> void anyEmpty(Supplier<? extends X> supplier, String o, String... os) throws X {
        boolean empty = BoolUtil.anyEmpty(o, os);
        if (!empty) {
            throw supplier.get();
        }
    }

    /**
     * All empty.
     *
     * @param <X>      the type parameter
     * @param supplier the supplier
     * @param o        the o
     * @param os       the os
     * @throws X the x
     */
    public static <X extends Throwable> void allEmpty(Supplier<? extends X> supplier, String o, String... os) throws X {
        boolean empty = BoolUtil.allEmpty(o, os);
        if (!empty) {
            throw supplier.get();
        }
    }

    /**
     * Any empty.
     *
     * @param msg the msg
     * @param o   the o
     * @param os  the os
     */
    public static void anyEmpty(String msg, String o, String... os) {
        boolean empty = BoolUtil.anyEmpty(o, os);
        if (!empty) {
            throw new AssertException(msg);
        }
    }

    /**
     * All empty.
     *
     * @param msg the msg
     * @param o   the o
     * @param os  the os
     */
    public static void allEmpty(String msg, String o, String... os) {
        boolean empty = BoolUtil.allEmpty(o, os);
        if (!empty) {
            throw new AssertException(msg);
        }
    }

    /**
     * Not empty.
     *
     * @param <X>      the type parameter
     * @param o        the o
     * @param supplier the supplier
     * @throws X the x
     */
    public static <X extends Throwable> void notEmpty(String o, Supplier<? extends X> supplier) throws X {
        boolean notEmpty = BoolUtil.notEmpty(o);
        if (!notEmpty) {
            throw supplier.get();
        }
    }

    /**
     * Not empty.
     *
     * @param o   the o
     * @param msg the msg
     */
    public static void notEmpty(String o, String msg) {
        boolean notEmpty = BoolUtil.notEmpty(o);
        if (!notEmpty) {
            throw new AssertException(msg);
        }
    }

    /**
     * All not empty.
     *
     * @param <X>      the type parameter
     * @param supplier the supplier
     * @param o        the o
     * @param os       the os
     * @throws X the x
     */
    public static <X extends Throwable> void allNotEmpty(Supplier<? extends X> supplier, String o, String... os) throws X {
        boolean notEmpty = BoolUtil.allNotEmpty(o, os);
        if (!notEmpty) {
            throw supplier.get();
        }
    }

    /**
     * Any not empty.
     *
     * @param <X>      the type parameter
     * @param supplier the supplier
     * @param o        the o
     * @param os       the os
     * @throws X the x
     */
    public static <X extends Throwable> void anyNotEmpty(Supplier<? extends X> supplier, String o, String... os) throws X {
        boolean notEmpty = BoolUtil.anyNotEmpty(o, os);
        if (!notEmpty) {
            throw supplier.get();
        }
    }

    /**
     * Any not empty.
     *
     * @param msg the msg
     * @param o   the o
     * @param os  the os
     */
    public static void anyNotEmpty(String msg, String o, String... os) {
        boolean notEmpty = BoolUtil.anyNotEmpty(o, os);
        if (!notEmpty) {
            throw new AssertException(msg);
        }
    }

    /**
     * All not empty.
     *
     * @param msg the msg
     * @param o   the o
     * @param os  the os
     */
    public static void allNotEmpty(String msg, String o, String... os) {
        boolean notEmpty = BoolUtil.allNotEmpty(o, os);
        if (!notEmpty) {
            throw new AssertException(msg);
        }
    }

    /**
     * Is null.
     *
     * @param <X>      the type parameter
     * @param o        the o
     * @param supplier the supplier
     * @throws X the x
     */
    public static <X extends Throwable> void isNull(Object o, Supplier<? extends X> supplier) throws X {
        boolean aNull = BoolUtil.isNull(o);
        if (!aNull) {
            throw supplier.get();
        }
    }

    /**
     * Is null.
     *
     * @param <T>      the type parameter
     * @param <R>      the type parameter
     * @param o        the o
     * @param function the function
     * @throws R the r
     */
    public static <T, R extends RuntimeException> void isNull(T o, Function<T, R> function) throws R {
        boolean aNull = BoolUtil.isNull(o);
        if (!aNull) {
            throw function.apply(o);
        }
    }

    /**
     * Is null.
     *
     * @param o   the o
     * @param msg the msg
     */
    public static void isNull(Object o, String msg) {
        boolean aNull = BoolUtil.isNull(o);
        if (!aNull) {
            throw new AssertException(msg);
        }
    }

    /**
     * Any null.
     *
     * @param <X>      the type parameter
     * @param supplier the supplier
     * @param o        the o
     * @param os       the os
     * @throws X the x
     */
    public static <X extends Throwable> void anyNull(Supplier<? extends X> supplier, Object o, Object... os) throws X {
        boolean aNull = BoolUtil.anyNull(o, os);
        if (!aNull) {
            throw supplier.get();
        }
    }

    /**
     * Any null.
     *
     * @param msg the msg
     * @param o   the o
     * @param os  the os
     */
    public static void anyNull(String msg, Object o, Object... os) {
        boolean aNull = BoolUtil.anyNull(o, os);
        if (!aNull) {
            throw new AssertException(msg);
        }
    }

    /**
     * All null.
     *
     * @param <X>      the type parameter
     * @param supplier the supplier
     * @param o        the o
     * @param os       the os
     * @throws X the x
     */
    public static <X extends Throwable> void allNull(Supplier<? extends X> supplier, Object o, Object... os) throws X {
        boolean aNull = BoolUtil.allNull(o, os);
        if (!aNull) {
            throw supplier.get();
        }
    }

    /**
     * All null.
     *
     * @param msg the msg
     * @param o   the o
     * @param os  the os
     */
    public static void allNull(String msg, Object o, Object... os) {
        boolean aNull = BoolUtil.allNull(o, os);
        if (!aNull) {
            throw new AssertException(msg);
        }
    }

    /**
     * Not null.
     *
     * @param <X>      the type parameter
     * @param o        the o
     * @param supplier the supplier
     * @throws X the x
     */
    public static <X extends Throwable> void notNull(Object o, Supplier<? extends X> supplier) throws X {
        boolean notNull = BoolUtil.notNull(o);
        if (!notNull) {
            throw supplier.get();
        }
    }

    /**
     * Not null.
     *
     * @param <T>      the type parameter
     * @param <R>      the type parameter
     * @param o        the o
     * @param function the function
     * @throws R the r
     */
    public static <T, R extends RuntimeException> void notNull(T o, Function<T, R> function) throws R {
        boolean notNull = BoolUtil.notNull(o);
        if (!notNull) {
            throw function.apply(null);
        }
    }

    /**
     * Not null.
     *
     * @param o   the o
     * @param msg the msg
     */
    public static void notNull(Object o, String msg) {
        boolean notNull = BoolUtil.notNull(o);
        if (!notNull) {
            throw new AssertException(msg);
        }
    }

    /**
     * Any not null.
     *
     * @param <X>      the type parameter
     * @param supplier the supplier
     * @param o        the o
     * @param os       the os
     * @throws X the x
     */
    public static <X extends Throwable> void anyNotNull(Supplier<? extends X> supplier, Object o, Object... os) throws X {
        boolean notNull = BoolUtil.anyNotNull(o, os);
        if (!notNull) {
            throw supplier.get();
        }
    }

    /**
     * Any not null.
     *
     * @param msg the msg
     * @param o   the o
     * @param os  the os
     */
    public static void anyNotNull(String msg, Object o, Object... os) {
        boolean notNull = BoolUtil.anyNotNull(o, os);
        if (!notNull) {
            throw new AssertException(msg);
        }
    }

    /**
     * All not null.
     *
     * @param <X>      the type parameter
     * @param supplier the supplier
     * @param o        the o
     * @param os       the os
     * @throws X the x
     */
    public static <X extends Throwable> void allNotNull(Supplier<? extends X> supplier, Object o, Object... os) throws X {
        boolean notNull = BoolUtil.allNotNull(o, os);
        if (!notNull) {
            throw supplier.get();
        }
    }

    /**
     * All not null.
     *
     * @param msg the msg
     * @param o   the o
     * @param os  the os
     */
    public static void allNotNull(String msg, Object o, Object... os) {
        boolean notNull = BoolUtil.allNotNull(o, os);
        if (!notNull) {
            throw new AssertException(msg);
        }
    }

    /**
     * Eq.
     *
     * @param <X>      the type parameter
     * @param source   the source
     * @param target   the target
     * @param supplier the supplier
     * @throws X the x
     */
    public static <X extends Throwable> void eq(Object source, Object target, Supplier<? extends X> supplier) throws X {
        boolean eq = BoolUtil.eq(source, target);
        if (!eq) {
            throw supplier.get();
        }
    }

    /**
     * Eq.
     *
     * @param <X>      the type parameter
     * @param source   the source
     * @param target   the target
     * @param supplier the supplier
     * @throws X the x
     */
    public static <X extends Throwable> void eq(Comparable source, Comparable target, Supplier<? extends X> supplier) throws X {
        boolean gt = BoolUtil.eq(source, target);
        if (!gt) {
            throw supplier.get();
        }
    }

    /**
     * Eq.
     *
     * @param source the source
     * @param target the target
     * @param msg    the msg
     */
    public static void eq(Object source, Object target, String msg) {
        boolean eq = BoolUtil.eq(source, target);
        if (!eq) {
            throw new AssertException(msg);
        }
    }

    /**
     * Eq.
     *
     * @param source the source
     * @param target the target
     * @param msg    the msg
     */
    public static void eq(Comparable source, Comparable target, String msg) {
        boolean gt = BoolUtil.eq(source, target);
        if (!gt) {
            throw new AssertException(msg);
        }
    }

    /**
     * Gt.
     *
     * @param <X>      the type parameter
     * @param source   the source
     * @param target   the target
     * @param supplier the supplier
     * @throws X the x
     */
    public static <X extends Throwable> void gt(Comparable source, Comparable target, Supplier<? extends X> supplier) throws X {
        boolean gt = BoolUtil.gt(source, target);
        if (!gt) {
            throw supplier.get();
        }
    }

    /**
     * Gte.
     *
     * @param <X>      the type parameter
     * @param source   the source
     * @param target   the target
     * @param supplier the supplier
     * @throws X the x
     */
    public static <X extends Throwable> void gte(Comparable source, Comparable target, Supplier<? extends X> supplier) throws X {
        boolean gte = BoolUtil.gte(source, target);
        if (!gte) {
            throw supplier.get();
        }
    }

    /**
     * Lt.
     *
     * @param <X>      the type parameter
     * @param source   the source
     * @param target   the target
     * @param supplier the supplier
     * @throws X the x
     */
    public static <X extends Throwable> void lt(Comparable source, Comparable target, Supplier<? extends X> supplier) throws X {
        boolean lt = BoolUtil.lt(source, target);
        if (!lt) {
            throw supplier.get();
        }
    }

    /**
     * Lte.
     *
     * @param <X>      the type parameter
     * @param source   the source
     * @param target   the target
     * @param supplier the supplier
     * @throws X the x
     */
    public static <X extends Throwable> void lte(Comparable source, Comparable target, Supplier<? extends X> supplier) throws X {
        boolean lte = BoolUtil.lte(source, target);
        if (!lte) {
            throw supplier.get();
        }
    }

    /**
     * Gt.
     *
     * @param source the source
     * @param target the target
     * @param msg    the msg
     */
    public static void gt(Comparable source, Comparable target, String msg) {
        boolean gt = BoolUtil.gt(source, target);
        if (!gt) {
            throw new AssertException(msg);
        }
    }

    /**
     * Gte.
     *
     * @param source the source
     * @param target the target
     * @param msg    the msg
     */
    public static void gte(Comparable source, Comparable target, String msg) {
        boolean gte = BoolUtil.gte(source, target);
        if (!gte) {
            throw new AssertException(msg);
        }
    }

    /**
     * Lt.
     *
     * @param source the source
     * @param target the target
     * @param msg    the msg
     */
    public static void lt(Comparable source, Comparable target, String msg) {
        boolean lt = BoolUtil.lt(source, target);
        if (!lt) {
            throw new AssertException(msg);
        }
    }

    /**
     * Lte.
     *
     * @param source the source
     * @param target the target
     * @param msg    the msg
     */
    public static void lte(Comparable source, Comparable target, String msg) {
        boolean lte = BoolUtil.lte(source, target);
        if (!lte) {
            throw new AssertException(msg);
        }
    }

    /**
     * Contain.
     *
     * @param <X>      the type parameter
     * @param source   the source
     * @param target   the target
     * @param supplier the supplier
     * @throws X the x
     */
    public static <X extends Throwable> void contain(String source, String target, Supplier<? extends X> supplier) throws X {
        boolean contain = BoolUtil.contain(source, target);
        if (!contain) {
            throw supplier.get();
        }
    }

    /**
     * Not contain.
     *
     * @param <X>      the type parameter
     * @param source   the source
     * @param target   the target
     * @param supplier the supplier
     * @throws X the x
     */
    public static <X extends Throwable> void notContain(String source, String target, Supplier<? extends X> supplier) throws X {
        boolean contain = BoolUtil.notContain(source, target);
        if (!contain) {
            throw supplier.get();
        }
    }

    /**
     * Contain.
     *
     * @param source the source
     * @param target the target
     * @param msg    the msg
     */
    public static void contain(String source, String target, String msg) {
        boolean contain = BoolUtil.contain(source, target);
        if (!contain) {
            throw new AssertException(msg);
        }
    }

    /**
     * Not contain.
     *
     * @param source the source
     * @param target the target
     * @param msg    the msg
     */
    public static void notContain(String source, String target, String msg) {
        boolean contain = BoolUtil.notContain(source, target);
        if (!contain) {
            throw new AssertException(msg);
        }
    }

    /**
     * Contain all.
     *
     * @param <X>      the type parameter
     * @param supplier the supplier
     * @param source   the source
     * @param targets  the targets
     * @throws X the x
     */
    public static <X extends Throwable> void containAll(Supplier<? extends X> supplier, String source, String... targets) throws X {
        boolean containAll = BoolUtil.containAll(source, targets);
        if (!containAll) {
            throw supplier.get();
        }
    }

    /**
     * Not contain all.
     *
     * @param <X>      the type parameter
     * @param supplier the supplier
     * @param source   the source
     * @param targets  the targets
     * @throws X the x
     */
    public static <X extends Throwable> void notContainAll(Supplier<? extends X> supplier, String source, String... targets) throws X {
        boolean containAll = BoolUtil.notContainAll(source, targets);
        if (!containAll) {
            throw supplier.get();
        }
    }

    /**
     * Contain all.
     *
     * @param msg     the msg
     * @param source  the source
     * @param targets the targets
     */
    public static void containAll(String msg, String source, String... targets) {
        boolean containAll = BoolUtil.containAll(source, targets);
        if (!containAll) {
            throw new AssertException(msg);
        }
    }

    /**
     * Not contain all.
     *
     * @param msg     the msg
     * @param source  the source
     * @param targets the targets
     */
    public static void notContainAll(String msg, String source, String... targets) {
        boolean containAll = BoolUtil.notContainAll(source, targets);
        if (!containAll) {
            throw new AssertException(msg);
        }
    }

    /**
     * Contain one.
     *
     * @param <X>      the type parameter
     * @param supplier the supplier
     * @param source   the source
     * @param targets  the targets
     * @throws X the x
     */
    public static <X extends Throwable> void containOne(Supplier<? extends X> supplier, String source, String... targets) throws X {
        boolean containOne = BoolUtil.containOne(source, targets);
        if (!containOne) {
            throw supplier.get();
        }
    }

    /**
     * Not contain one.
     *
     * @param <X>      the type parameter
     * @param supplier the supplier
     * @param source   the source
     * @param targets  the targets
     * @throws X the x
     */
    public static <X extends Throwable> void notContainOne(Supplier<? extends X> supplier, String source, String... targets) throws X {
        boolean containOne = BoolUtil.notContainOne(source, targets);
        if (!containOne) {
            throw supplier.get();
        }
    }

    /**
     * Contain one.
     *
     * @param msg     the msg
     * @param source  the source
     * @param targets the targets
     */
    public static void containOne(String msg, String source, String... targets) {
        boolean containOne = BoolUtil.containOne(source, targets);
        if (!containOne) {
            throw new AssertException(msg);
        }
    }

    /**
     * Not contain one.
     *
     * @param msg     the msg
     * @param source  the source
     * @param targets the targets
     */
    public static void notContainOne(String msg, String source, String... targets) {
        boolean containOne = BoolUtil.notContainOne(source, targets);
        if (!containOne) {
            throw new AssertException(msg);
        }
    }

    /**
     * Contain all.
     *
     * @param <X>      the type parameter
     * @param <T>      the type parameter
     * @param supplier the supplier
     * @param source   the source
     * @param targets  the targets
     * @throws X the x
     */
    public static <X extends Throwable, T> void containAll(Supplier<? extends X> supplier, T[] source, T... targets) throws X {
        boolean containAll = BoolUtil.containAll(source, targets);
        if (!containAll) {
            throw supplier.get();
        }
    }

    /**
     * Not contain all.
     *
     * @param <X>      the type parameter
     * @param <T>      the type parameter
     * @param supplier the supplier
     * @param source   the source
     * @param targets  the targets
     * @throws X the x
     */
    public static <X extends Throwable, T> void notContainAll(Supplier<? extends X> supplier, T[] source, T... targets) throws X {
        boolean containAll = BoolUtil.notContainAll(source, targets);
        if (!containAll) {
            throw supplier.get();
        }
    }

    /**
     * Contain all.
     *
     * @param <T>     the type parameter
     * @param msg     the msg
     * @param source  the source
     * @param targets the targets
     */
    public static <T> void containAll(String msg, T[] source, T... targets) {
        boolean containAll = BoolUtil.containAll(source, targets);
        if (!containAll) {
            throw new AssertException(msg);
        }
    }

    /**
     * Not contain all.
     *
     * @param <T>     the type parameter
     * @param msg     the msg
     * @param source  the source
     * @param targets the targets
     */
    public static <T> void notContainAll(String msg, T[] source, T... targets) {
        boolean containAll = BoolUtil.notContainAll(source, targets);
        if (!containAll) {
            throw new AssertException(msg);
        }
    }

    /**
     * Contain one.
     *
     * @param <X>      the type parameter
     * @param <T>      the type parameter
     * @param supplier the supplier
     * @param source   the source
     * @param targets  the targets
     * @throws X the x
     */
    public static <X extends Throwable, T> void containOne(Supplier<? extends X> supplier, T[] source, T... targets) throws X {
        boolean containOne = BoolUtil.containOne(source, targets);
        if (!containOne) {
            throw supplier.get();
        }
    }

    /**
     * Not contain one.
     *
     * @param <X>      the type parameter
     * @param <T>      the type parameter
     * @param supplier the supplier
     * @param source   the source
     * @param targets  the targets
     * @throws X the x
     */
    public static <X extends Throwable, T> void notContainOne(Supplier<? extends X> supplier, T[] source, T... targets) throws X {
        boolean containOne = BoolUtil.notContainOne(source, targets);
        if (!containOne) {
            throw supplier.get();
        }
    }

    /**
     * Contain one.
     *
     * @param <T>     the type parameter
     * @param msg     the msg
     * @param source  the source
     * @param targets the targets
     */
    public static <T> void containOne(String msg, T[] source, T... targets) {
        boolean containOne = BoolUtil.containOne(source, targets);
        if (!containOne) {
            throw new AssertException(msg);
        }
    }

    /**
     * Not contain one.
     *
     * @param <T>     the type parameter
     * @param msg     the msg
     * @param source  the source
     * @param targets the targets
     */
    public static <T> void notContainOne(String msg, T[] source, T... targets) {
        boolean containOne = BoolUtil.notContainOne(source, targets);
        if (!containOne) {
            throw new AssertException(msg);
        }
    }

    /**
     * Contain all.
     *
     * @param <X>      the type parameter
     * @param <T>      the type parameter
     * @param supplier the supplier
     * @param source   the source
     * @param targets  the targets
     * @throws X the x
     */
    public static <X extends Throwable, T> void containAll(Supplier<? extends X> supplier, Collection<T> source, T... targets) throws X {
        boolean containAll = BoolUtil.containAll(source, targets);
        if (!containAll) {
            throw supplier.get();
        }
    }

    /**
     * Not contain all.
     *
     * @param <X>      the type parameter
     * @param <T>      the type parameter
     * @param supplier the supplier
     * @param source   the source
     * @param targets  the targets
     * @throws X the x
     */
    public static <X extends Throwable, T> void notContainAll(Supplier<? extends X> supplier, Collection<T> source, T... targets) throws X {
        boolean containAll = BoolUtil.notContainAll(source, targets);
        if (!containAll) {
            throw supplier.get();
        }
    }

    /**
     * Contain all.
     *
     * @param <T>     the type parameter
     * @param msg     the msg
     * @param source  the source
     * @param targets the targets
     */
    public static <T> void containAll(String msg, Collection<T> source, T... targets) {
        boolean containAll = BoolUtil.containAll(source, targets);
        if (!containAll) {
            throw new AssertException(msg);
        }
    }

    /**
     * Not contain all.
     *
     * @param <T>     the type parameter
     * @param msg     the msg
     * @param source  the source
     * @param targets the targets
     */
    public static <T> void notContainAll(String msg, Collection<T> source, T... targets) {
        boolean containAll = BoolUtil.notContainAll(source, targets);
        if (!containAll) {
            throw new AssertException(msg);
        }
    }

    /**
     * Contain one.
     *
     * @param <X>      the type parameter
     * @param <T>      the type parameter
     * @param supplier the supplier
     * @param source   the source
     * @param targets  the targets
     * @throws X the x
     */
    public static <X extends Throwable, T> void containOne(Supplier<? extends X> supplier, Collection<T> source, T... targets) throws X {
        boolean containOne = BoolUtil.containOne(source, targets);
        if (!containOne) {
            throw supplier.get();
        }
    }

    /**
     * Not contain one.
     *
     * @param <X>      the type parameter
     * @param <T>      the type parameter
     * @param supplier the supplier
     * @param source   the source
     * @param targets  the targets
     * @throws X the x
     */
    public static <X extends Throwable, T> void notContainOne(Supplier<? extends X> supplier, Collection<T> source, T... targets) throws X {
        boolean containOne = BoolUtil.notContainOne(source, targets);
        if (!containOne) {
            throw supplier.get();
        }
    }

    /**
     * Contain one.
     *
     * @param <T>     the type parameter
     * @param msg     the msg
     * @param source  the source
     * @param targets the targets
     */
    public static <T> void containOne(String msg, Collection<T> source, T... targets) {
        boolean containOne = BoolUtil.containOne(source, targets);
        if (!containOne) {
            throw new AssertException(msg);
        }
    }

    /**
     * Not contain one.
     *
     * @param <T>     the type parameter
     * @param msg     the msg
     * @param source  the source
     * @param targets the targets
     */
    public static <T> void notContainOne(String msg, Collection<T> source, T... targets) {
        boolean containOne = BoolUtil.notContainOne(source, targets);
        if (!containOne) {
            throw new AssertException(msg);
        }
    }

    /**
     * Contain keys.
     *
     * @param <X>      the type parameter
     * @param <K>      the type parameter
     * @param <V>      the type parameter
     * @param supplier the supplier
     * @param map      the map
     * @param keys     the keys
     * @throws X the x
     */
    public static <X extends Throwable, K, V> void containKeys(Supplier<? extends X> supplier, Map<K, V> map, K... keys) throws X {
        boolean containKeys = BoolUtil.containKeys(map, keys);
        if (!containKeys) {
            throw supplier.get();
        }
    }

    /**
     * Not contain keys.
     *
     * @param <X>      the type parameter
     * @param <K>      the type parameter
     * @param <V>      the type parameter
     * @param supplier the supplier
     * @param map      the map
     * @param keys     the keys
     * @throws X the x
     */
    public static <X extends Throwable, K, V> void notContainKeys(Supplier<? extends X> supplier, Map<K, V> map, K... keys) throws X {
        boolean containKeys = BoolUtil.notContainKeys(map, keys);
        if (!containKeys) {
            throw supplier.get();
        }
    }

    /**
     * Contain key.
     *
     * @param <X>      the type parameter
     * @param <K>      the type parameter
     * @param <V>      the type parameter
     * @param supplier the supplier
     * @param map      the map
     * @param keys     the keys
     * @throws X the x
     */
    public static <X extends Throwable, K, V> void containKey(Supplier<? extends X> supplier, Map<K, V> map, K... keys) throws X {
        boolean containKey = BoolUtil.containKey(map, keys);
        if (!containKey) {
            throw supplier.get();
        }
    }

    /**
     * Not contain key.
     *
     * @param <X>      the type parameter
     * @param <K>      the type parameter
     * @param <V>      the type parameter
     * @param supplier the supplier
     * @param map      the map
     * @param keys     the keys
     * @throws X the x
     */
    public static <X extends Throwable, K, V> void notContainKey(Supplier<? extends X> supplier, Map<K, V> map, K... keys) throws X {
        boolean containKey = BoolUtil.notContainKey(map, keys);
        if (!containKey) {
            throw supplier.get();
        }
    }

    /**
     * Contain keys.
     *
     * @param <K>  the type parameter
     * @param <V>  the type parameter
     * @param msg  the msg
     * @param map  the map
     * @param keys the keys
     */
    public static <K, V> void containKeys(String msg, Map<K, V> map, K... keys) {
        boolean containKeys = BoolUtil.containKeys(map, keys);
        if (!containKeys) {
            throw new AssertException(msg);
        }
    }

    /**
     * Not contain keys.
     *
     * @param <K>  the type parameter
     * @param <V>  the type parameter
     * @param msg  the msg
     * @param map  the map
     * @param keys the keys
     */
    public static <K, V> void notContainKeys(String msg, Map<K, V> map, K... keys) {
        boolean containKeys = BoolUtil.notContainKeys(map, keys);
        if (!containKeys) {
            throw new AssertException(msg);
        }
    }

    /**
     * Contain key.
     *
     * @param <K>  the type parameter
     * @param <V>  the type parameter
     * @param msg  the msg
     * @param map  the map
     * @param keys the keys
     */
    public static <K, V> void containKey(String msg, Map<K, V> map, K... keys) {
        boolean containKey = BoolUtil.containKey(map, keys);
        if (!containKey) {
            throw new AssertException(msg);
        }
    }

    /**
     * Not contain key.
     *
     * @param <K>  the type parameter
     * @param <V>  the type parameter
     * @param msg  the msg
     * @param map  the map
     * @param keys the keys
     */
    public static <K, V> void notContainKey(String msg, Map<K, V> map, K... keys) {
        boolean containKey = BoolUtil.notContainKey(map, keys);
        if (!containKey) {
            throw new AssertException(msg);
        }
    }

    /**
     * Contain values.
     *
     * @param <X>      the type parameter
     * @param <K>      the type parameter
     * @param <V>      the type parameter
     * @param supplier the supplier
     * @param map      the map
     * @param values   the values
     * @throws X the x
     */
    public static <X extends Throwable, K, V> void containValues(Supplier<? extends X> supplier, Map<K, V> map, V... values) throws X {
        boolean containValues = BoolUtil.containValues(map, values);
        if (!containValues) {
            throw supplier.get();
        }
    }

    /**
     * Not contain values.
     *
     * @param <X>      the type parameter
     * @param <K>      the type parameter
     * @param <V>      the type parameter
     * @param supplier the supplier
     * @param map      the map
     * @param values   the values
     * @throws X the x
     */
    public static <X extends Throwable, K, V> void notContainValues(Supplier<? extends X> supplier, Map<K, V> map, V... values) throws X {
        boolean containValues = BoolUtil.notContainValues(map, values);
        if (!containValues) {
            throw supplier.get();
        }
    }

    /**
     * Contain value.
     *
     * @param <X>      the type parameter
     * @param <K>      the type parameter
     * @param <V>      the type parameter
     * @param supplier the supplier
     * @param map      the map
     * @param values   the values
     * @throws X the x
     */
    public static <X extends Throwable, K, V> void containValue(Supplier<? extends X> supplier, Map<K, V> map, V... values) throws X {
        boolean containValue = BoolUtil.containValue(map, values);
        if (!containValue) {
            throw supplier.get();
        }
    }

    /**
     * Not contain value.
     *
     * @param <X>      the type parameter
     * @param <K>      the type parameter
     * @param <V>      the type parameter
     * @param supplier the supplier
     * @param map      the map
     * @param values   the values
     * @throws X the x
     */
    public static <X extends Throwable, K, V> void notContainValue(Supplier<? extends X> supplier, Map<K, V> map, V... values) throws X {
        boolean containValue = BoolUtil.notContainValue(map, values);
        if (!containValue) {
            throw supplier.get();
        }
    }

    /**
     * Contain values.
     *
     * @param <K>    the type parameter
     * @param <V>    the type parameter
     * @param msg    the msg
     * @param map    the map
     * @param values the values
     */
    public static <K, V> void containValues(String msg, Map<K, V> map, V... values) {
        boolean containValues = BoolUtil.containValues(map, values);
        if (!containValues) {
            throw new AssertException(msg);
        }
    }

    /**
     * Not contain values.
     *
     * @param <K>    the type parameter
     * @param <V>    the type parameter
     * @param msg    the msg
     * @param map    the map
     * @param values the values
     */
    public static <K, V> void notContainValues(String msg, Map<K, V> map, V... values) {
        boolean containValues = BoolUtil.notContainValues(map, values);
        if (!containValues) {
            throw new AssertException(msg);
        }
    }

    /**
     * Contain value.
     *
     * @param <K>    the type parameter
     * @param <V>    the type parameter
     * @param msg    the msg
     * @param map    the map
     * @param values the values
     */
    public static <K, V> void containValue(String msg, Map<K, V> map, V... values) {
        boolean containValue = BoolUtil.containValue(map, values);
        if (!containValue) {
            throw new AssertException(msg);
        }
    }

    /**
     * Not contain value.
     *
     * @param <K>    the type parameter
     * @param <V>    the type parameter
     * @param msg    the msg
     * @param map    the map
     * @param values the values
     */
    public static <K, V> void notContainValue(String msg, Map<K, V> map, V... values) {
        boolean containValue = BoolUtil.notContainValue(map, values);
        if (!containValue) {
            throw new AssertException(msg);
        }
    }

}
