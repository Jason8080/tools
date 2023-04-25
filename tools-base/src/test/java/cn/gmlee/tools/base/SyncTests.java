package cn.gmlee.tools.base;

import lombok.SneakyThrows;

import java.util.concurrent.*;

public class SyncTests {

    private static ThreadPoolExecutor executor = new ThreadPoolExecutor(
            400,
            400,
            1L,
            TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(100),
            new ThreadPoolExecutor.CallerRunsPolicy());

    private static final ThreadLocal threadLocal = new InheritableThreadLocal();

    /**
     * 测试InheritableThreadLocal在另起线程中(存/取参的情况)
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        for (int i = 1; i < 10; i++) {
            int j = i; // 语法要求 可以忽略.
            new Thread(() -> second("第" + j + "个请求")).start();
        }
    }

    @SneakyThrows
    private static void first(String str) {
        // 保存请求头
        threadLocal.set(str);
        // 另起线程处理
        Future future = CompletableFuture.supplyAsync(() -> get());
        System.out.println(future.get());
    }

    @SneakyThrows
    private static void second(String str) {
        // 保存请求头
        threadLocal.set(str);
        // 另起线程处理
        Future future = executor.submit(() -> get());
        System.out.println(future.get());
    }

    @SneakyThrows
    private static void third(String str) {
        // 保存请求头
        threadLocal.set(str);
        // 另起线程处理
        FutureTask future = new FutureTask(() -> get());
        new Thread(future).start();
        System.out.println(future.get());
    }

    private synchronized static Object get() {
        Object o = threadLocal.get();
//        threadLocal.remove();
        return o;
    }
}
