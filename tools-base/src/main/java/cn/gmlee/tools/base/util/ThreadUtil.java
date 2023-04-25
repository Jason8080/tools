package cn.gmlee.tools.base.util;

import cn.gmlee.tools.base.enums.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

/**
 * 线程池工具.
 *
 * @author Jas °
 * @date 2021 /7/20 (周二)
 */
@SuppressWarnings("all")
public class ThreadUtil {

    private static final Logger logger = LoggerFactory.getLogger(ThreadUtil.class);

    /**
     * 默认的全局线程池: 根据需要扩建线程池, 最大为Int的最大值个线程, 60s无操作则关闭, 无待机线程。
     */
    private static final Pool GLOBAL_POOL = new Pool(Executors.newCachedThreadPool((run) -> new Thread(run, String.format("Tools-GlobalPool:%s", run.hashCode()))));

    /**
     * Execute.
     *
     * @param run the run
     */
    public static void execute(Runnable run) {
        GLOBAL_POOL.execute(run);
    }

    /**
     * Execute.
     *
     * @param <T>  the type parameter
     * @param call the call
     * @param runs the runs
     */
    public static <T> void execute(Callable<T> call, Function.One<T>... runs) {
        GLOBAL_POOL.execute(call, runs);
    }


    /**
     * 获取新的线程池 (需要用户主动保存Pool才能达到复用目的).
     *
     * @return the independent pool
     */
    public static Pool getIndependentPool() {
        return new Pool(Executors.newCachedThreadPool((run) -> new Thread(run, String.format("Tools-IndependentPool:%s", run.hashCode()))));
    }

    /**
     * 获取新的固定线程数的线程池 (需要用户主动保存Pool才能达到复用目的).
     *
     * @param num the num
     * @return the fixed pool
     */
    public static Pool getFixedPool(int num) {
        return new Pool(Executors.newFixedThreadPool(num, (run) -> new Thread(run, String.format("Tools-FixedThreadPool:%s", run.hashCode()))));
    }

    /**
     * Gets scheduled pool.
     *
     * @param num the num
     * @return the scheduled pool
     */
    public static Pool getScheduledPool(int num) {
        return new Pool(Executors.newScheduledThreadPool(num, (run) -> new Thread(run, String.format("Tools-ScheduledThreadPool:%s", run.hashCode()))));
    }

    /**
     * 获取内存控制的线程池.
     * <p>
     * # 限制最大线程数, 防止内存溢出
     * # 核心线程数为0, 防止内存泄露; 任务完成后60s内存回收;
     * # 阻塞队列固定, 防止内存溢出
     * </p>
     *
     * @param num 单个线程处理的任务数
     * @return the custom pool
     */
    public static Pool getMemoryPool(int num) {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                0, Integer.MAX_VALUE,
                60, TimeUnit.SECONDS,
                new LinkedBlockingDeque(num),
                (run) -> new Thread(run, String.format("Tools-MemoryPool:%s", run.hashCode())));
        return new Pool(executor);
    }

    /**
     * 获取自定义线程池 (需要用户主动保存Pool才能达到复用目的).
     *
     * @param executorService the executor service
     * @return the custom pool
     */
    public static Pool getCustomPool(ExecutorService executorService) {
        return new Pool(executorService);
    }

    /**
     * The type Pool.
     */
    public static class Pool {
        /**
         * 用于独立线程池引用.
         * <p>
         * 用法: new ThreadUtil(executorService)
         * </p>
         */
        private final ExecutorService executorService;

        /**
         * Instantiates a new Pool.
         */
        public Pool() {
            this.executorService = Executors.newCachedThreadPool((run) -> new Thread(run, String.format("Tools-CachedThreadPool:%s", run.hashCode())));
        }

        /**
         * Instantiates a new Pool.
         *
         * @param executorService the executor service
         */
        public Pool(ExecutorService executorService) {
            this.executorService = executorService;
        }

        /**
         * 异步处理程序.
         *
         * @param run the run
         */
        public void execute(Runnable run) {
            executorService.execute(run);
        }

        /**
         * 同步处理程序.
         *
         * @param <T> the type parameter
         * @param run the run
         * @return t t
         * @throws ExecutionException   the execution exception
         * @throws InterruptedException the interrupted exception
         */
        public <T> T sync(Callable<T> run) throws ExecutionException, InterruptedException {
            Future<T> future = executorService.submit(run);
            return future.get();
        }

        /**
         * Sync t.
         *
         * @param <T>      the type parameter
         * @param run      the run
         * @param timeout  the timeout
         * @param timeUnit the time unit
         * @return the t
         * @throws ExecutionException   the execution exception
         * @throws InterruptedException the interrupted exception
         */
        public <T> T sync(Callable<T> run, long timeout, TimeUnit timeUnit) throws InterruptedException, ExecutionException, TimeoutException {
            Future<T> future = executorService.submit(run);
            try {
                return timeout < 1 ? future.get() : future.get(timeout, timeUnit);
            } catch (Throwable e) {
                future.cancel(true);
                throw e;
            }
        }

        /**
         * Execute.
         *
         * @param <T>  the type parameter
         * @param call the call
         * @param runs the runs
         */
        public <T> void execute(Callable<T> call, Function.One<T>... runs) {
            Future<T> future = executorService.submit(call);
            execute(() -> {
                T t = null;
                try {
                    // 堵塞1个线程
                    t = future.get();
                } catch (Exception e) {
                    logger.error(String.format("获取线程执行结果出错: %s", call), e);
                }
                for (Function.One<T> run : runs) {
                    try {
                        run.run(t);
                    } catch (Throwable throwable) {
                        logger.error(String.format("回调函数执行出错: %s", run), throwable);
                    }
                }
            });
        }
    }
}
