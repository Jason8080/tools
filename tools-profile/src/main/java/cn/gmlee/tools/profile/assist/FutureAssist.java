package cn.gmlee.tools.profile.assist;

import cn.gmlee.tools.base.util.ExceptionUtil;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

/**
 * 新起线程的同步策略辅助工具.
 */
public class FutureAssist {

    /**
     * Supply async future.
     *
     * @param <V>      the type parameter
     * @param callable the callable
     * @return the future
     */
    public static <V> V supplyAsync(Callable<V> callable) {
        FutureTask<V> task = new FutureTask(callable);
        Thread thread = new Thread(task, FutureAssist.class.getSimpleName());
        thread.start();
        return ExceptionUtil.suppress(() -> task.get());
    }
}
