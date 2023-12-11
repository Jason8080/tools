package cn.gmlee.tools.base.kit.task;

import cn.gmlee.tools.base.util.AssertUtil;
import cn.gmlee.tools.base.util.IdUtil;

import java.io.Serializable;
import java.util.Map;
import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 异步任务管理器.
 */
public class TimerTaskManager {
    private static final Map<Serializable, Task> taskMap = new ConcurrentHashMap<>();
    private static final Timer timer = new Timer("TOOLS-TimerTaskManager");


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
        Task task = new Task();
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
        Task task = new Task();
        task.run = run;
        return submit(task);
    }

    /**
     * 提交.
     *
     * @param task the task
     * @return the serializable
     */
    public static synchronized Serializable submit(Task task) {
        String key = IdUtil.uuidReplace();
        if (taskMap.containsKey(key)) {
            return submit(task);
        }
        taskMap.put(key, task);
        return key;
    }

    /**
     * 暂停.
     *
     * @param key the key
     */
    public static void suspend(Serializable key) {
        Task task = taskMap.get(key);
        if (task != null) {
            task.cancel();
        }
        timer.purge();
    }

    /**
     * 启动(不会自动重启).
     *
     * @param key     the key
     * @param allowEx 允许异常(在暂停后发生)
     */
    public static void start(Serializable key, boolean allowEx) {
        Task task = taskMap.get(key);
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
        Task task = taskMap.get(key);
        try {
            start(task, task.delay, task.period);
        } catch (Exception e) {
            restart(key);
        }
    }

    /**
     * 启动(如果被暂停过会自动重启).
     *
     * @param key    the key
     * @param delay  推迟时间
     * @param period 间隔时间
     */
    public static void start(Serializable key, long delay, long period) {
        try {
            start(taskMap.get(key), delay, period);
        } catch (Exception e) {
            restart(key);
        }
    }

    /**
     * 启动.
     *
     * @param task   the task
     * @param delay  推迟时间
     * @param period 间隔时间
     */
    private synchronized static void start(Task task, long delay, long period) {
        if (task != null) {
            try {
                task.delay = delay;
                task.period = period;
                timer.schedule(task, task.delay, task.period);
            } catch (Exception e) {
                throw e;
            }
        }
    }

    /**
     * 重启.
     *
     * @param key the key
     */
    public synchronized static void restart(Serializable key) {
        Task task = taskMap.get(key);
        if (task != null) {
            Task clone = task.clone();
            taskMap.put(key, clone);
            timer.schedule(clone, clone.delay, clone.period);
        }
    }

    /**
     * 取消.
     *
     * @param key the key
     */
    public static void remove(Serializable key) {
        Task task = taskMap.remove(key);
        if (task != null) {
            task.cancel();
        }
        timer.purge();
    }
}
