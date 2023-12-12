package cn.gmlee.tools.base.kit.task;

import cn.gmlee.tools.base.util.AssertUtil;
import cn.gmlee.tools.base.util.IdUtil;

import java.io.Serializable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 定时任务管理器.
 */
public class ScheduledTaskManager implements TaskManager {
    private static final ScheduledExecutorService scheduled = Executors.newSingleThreadScheduledExecutor((run) -> new Thread(run, String.format("Tools-ScheduledTaskManager:%s", run.hashCode())));

    /**
     * Gets key.
     *
     * @return the key
     */
    public static synchronized Serializable getKey() {
        String key = IdUtil.uuidReplace();
        if (taskMap.containsKey(key)) {
            return getKey();
        }
        return key;
    }

    /**
     * Submit.
     *
     * @param key the key
     * @param run the run
     */
    public static synchronized void submit(Serializable key, Runnable run) {
        AssertUtil.isFalse(taskMap.containsKey(key), String.format("键重复: %s", key));
        ScheduledTask task = new ScheduledTask();
        task.run = run;
        taskMap.put(key, task);
    }

    /**
     * 提交.
     *
     * @param run the run
     * @return the serializable
     */
    public static synchronized Serializable submit(Runnable run) {
        ScheduledTask task = new ScheduledTask();
        task.run = run;
        return submit(task);
    }

    /**
     * 提交.
     *
     * @param task the task
     * @return the serializable
     */
    public static synchronized Serializable submit(ScheduledTask task) {
        String key = IdUtil.uuidReplace();
        if (taskMap.containsKey(key)) {
            return submit(task);
        }
        taskMap.put(key, task);
        return key;
    }

    /**
     * 启动(不会自动重启).
     *
     * @param key     the key
     * @param allowEx 允许异常(在暂停后发生)
     */
    public static void start(Serializable key, boolean allowEx) {
        ScheduledTask task = (ScheduledTask) taskMap.get(key);
        try {
            start(task, task.delay, task.period);
        } catch (Exception e) {
            if (allowEx) {
                throw e;
            }
        }
    }

    /**
     * 启动(如果被暂停过会自动重启).
     *
     * @param key the key
     */
    public static void start(Serializable key) {
        ScheduledTask task = (ScheduledTask) taskMap.get(key);
        start(task, task.delay, task.period);
    }

    /**
     * 启动(如果被暂停过会自动重启).
     *
     * @param key    the key
     * @param delay  推迟时间
     * @param period 间隔时间
     */
    public static void start(Serializable key, long delay, long period) {
        ScheduledTask task = (ScheduledTask) taskMap.get(key);
        start(task, delay, period);
    }

    /**
     * 启动.
     *
     * @param task   the task
     * @param delay  推迟时间
     * @param period 间隔时间
     */
    private synchronized static void start(ScheduledTask task, long delay, long period) {
        if (task != null) {
            task.delay = delay;
            task.period = period;
            scheduled.scheduleWithFixedDelay(task, task.delay, task.period, TimeUnit.MILLISECONDS);
        }
    }
}
