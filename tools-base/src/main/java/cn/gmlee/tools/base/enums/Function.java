package cn.gmlee.tools.base.enums;

/**
 * 镇压异常的接口函数
 *
 * @author Jas °
 * @date 2021 /1/12 (周二)
 */
public interface Function {
    /**
     * The interface Ok 0.
     */
    @FunctionalInterface
    interface Ok0 extends Function {
        /**
         * Run boolean.
         *
         * @return the boolean
         * @throws Throwable the throwable
         */
        Boolean run() throws Throwable;
    }

    /**
     * The interface Ok 1.
     *
     * @param <P> the type parameter
     */
    @FunctionalInterface
    interface Ok1<P> extends Function {
        /**
         * Run boolean.
         *
         * @param p the p
         * @return the boolean
         * @throws Throwable the throwable
         */
        Boolean run(P p) throws Throwable;
    }

    /**
     * The interface Ok 2.
     *
     * @param <P> the type parameter
     */
    @FunctionalInterface
    interface Ok2<P> extends Function {
        /**
         * Run boolean.
         *
         * @param p1 the p 1
         * @param p2 the p 2
         * @return the boolean
         * @throws Throwable the throwable
         */
        Boolean run(P p1, P p2) throws Throwable;
    }

    /**
     * The interface Ok two.
     *
     * @param <P1> the type parameter
     * @param <P2> the type parameter
     */
    @FunctionalInterface
    interface OkTwo<P1, P2> extends Function {
        /**
         * Run boolean.
         *
         * @param p1 the p 1
         * @param p2 the p 2
         * @return the boolean
         * @throws Throwable the throwable
         */
        Boolean run(P1 p1, P2 p2) throws Throwable;
    }

    /**
     * The interface Ok os.
     */
    @FunctionalInterface
    interface OsOk extends Function {
        /**
         * Run boolean.
         *
         * @param os the os
         * @return the boolean
         * @throws Throwable the throwable
         */
        Boolean run(Object... os) throws Throwable;
    }

    /**
     * The interface Os one.
     *
     * @param <R> the type parameter
     */
    @FunctionalInterface
    interface OsOne<R> extends Function {
        /**
         * Run boolean.
         *
         * @param os the os
         * @return the boolean
         * @throws Throwable the throwable
         */
        R run(Object... os) throws Throwable;
    }

    /**
     * The interface Zero.
     */
    @FunctionalInterface
    interface Zero extends Function {
        /**
         * Run.
         *
         * @throws Throwable the throwable
         */
        void run() throws Throwable;
    }

    /**
     * The interface Zero 2 r.
     *
     * @param <R> the type parameter
     */
    @FunctionalInterface
    interface Zero2r<R> extends Function {
        /**
         * Run.
         *
         * @return the r
         * @throws Throwable the throwable
         */
        R run() throws Throwable;
    }

    /**
     * The interface One.
     *
     * @param <P> the type parameter
     */
    @FunctionalInterface
    interface One<P> extends Function {
        /**
         * Run.
         *
         * @param p the p
         * @throws Throwable the throwable
         */
        void run(P p) throws Throwable;
    }

    /**
     * The interface P 2 p.
     *
     * @param <P> the type parameter
     */
    @FunctionalInterface
    interface P2v<P> extends Function {
        /**
         * Run.
         *
         * @param p the p
         * @return the p
         * @throws Throwable the throwable
         */
        void run(P p) throws Throwable;
    }

    /**
     * The interface V 2 r.
     *
     * @param <R> the type parameter
     */
    @FunctionalInterface
    interface V2r<R> extends Function {
        /**
         * Run r.
         *
         * @return the r
         * @throws Throwable the throwable
         */
        R run() throws Throwable;
    }

    /**
     * The interface P 2 p.
     *
     * @param <P> the type parameter
     */
    @FunctionalInterface
    interface P2p<P> extends Function {
        /**
         * Run p.
         *
         * @param p the p
         * @return the p
         * @throws Throwable the throwable
         */
        P run(P p) throws Throwable;
    }

    /**
     * The interface P 2 p.
     *
     * @param <P> the type parameter
     * @param <R> the type parameter
     */
    @FunctionalInterface
    interface P2r<P, R> extends Function {
        /**
         * Run r.
         *
         * @param p the p
         * @return the p
         * @throws Throwable the throwable
         */
        R run(P p) throws Throwable;
    }

    /**
     * The interface P 2 p.
     *
     * @param <P> the type parameter
     */
    @FunctionalInterface
    interface TwoP2p<P> extends Function {
        /**
         * Run p.
         *
         * @param p1 the p 1
         * @param p2 the p 2
         * @return the p
         * @throws Throwable the throwable
         */
        P run(P p1, P p2) throws Throwable;
    }

    /**
     * The interface P 2 p.
     *
     * @param <P> the type parameter
     * @param <R> the type parameter
     */
    @FunctionalInterface
    interface TwoP2r<P, R> extends Function {
        /**
         * Run r.
         *
         * @param p1 the p 1
         * @param p2 the p 2
         * @return the p
         * @throws Throwable the throwable
         */
        R run(P p1, P p2) throws Throwable;
    }

    /**
     * The interface Two 2 v.
     *
     * @param <One> the type parameter
     * @param <Two> the type parameter
     */
    @FunctionalInterface
    interface Two2v<One, Two> extends Function {
        /**
         * Run.
         *
         * @param one the one
         * @param two the two
         * @throws Throwable the throwable
         */
        void run(One one, Two two) throws Throwable;
    }

    /**
     * The interface P 2 p.
     *
     * @param <One> the type parameter
     * @param <Two> the type parameter
     * @param <R>   the type parameter
     */
    @FunctionalInterface
    interface Two2r<One, Two, R> extends Function {
        /**
         * Run r.
         *
         * @param one the one
         * @param two the two
         * @return the r
         * @throws Throwable the throwable
         */
        R run(One one, Two two) throws Throwable;
    }

    /**
     * The interface Os 2 o.
     *
     * @param <One> the type parameter
     */
    @FunctionalInterface
    interface OneOsOne<One> extends Function {
        /**
         * Run one.
         *
         * @param one the one
         * @param os  the os
         * @return the r
         * @throws Throwable the throwable
         */
        One run(One one, Object... os) throws Throwable;
    }
}
