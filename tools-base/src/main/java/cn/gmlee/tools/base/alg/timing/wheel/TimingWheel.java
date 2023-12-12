package cn.gmlee.tools.base.alg.timing.wheel;

import cn.gmlee.tools.base.kit.task.ScheduledTaskManager;
import cn.gmlee.tools.base.util.TimeUtil;
import org.springframework.scheduling.support.CronSequenceGenerator;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 时间轮算法.
 */
public class TimingWheel {
    /**
     * 任务队列
     */
    private static final ConcurrentHashMap<Tw, ConcurrentLinkedQueue<Task>> TASKS = new ConcurrentHashMap<>();
    /**
     * 定时任务
     */
    private static final Serializable key = ScheduledTaskManager.submit(() -> {
        System.out.println(TimeUtil.getCurrentDatetime());
        for (Map.Entry<Tw, ConcurrentLinkedQueue<Task>> next : TASKS.entrySet()) {
            Tw tw = next.getKey();
            ConcurrentLinkedQueue<Task> queue = next.getValue();
            // 轮转
            tw.run();
            // 执行
            queue.forEach(tw::execute);
            // 清理
            queue.removeIf(task -> task.discard);
        }
    });

    /**
     * 添加任务.
     *
     * @param tw    the tw
     * @param tasks the tasks
     */
    private static void add(Tw tw, Task... tasks) {
        ConcurrentLinkedQueue<Task> queue = TimingWheel.TASKS.get(tw);
        if (queue == null) {
            queue = new ConcurrentLinkedQueue<>();
            TimingWheel.TASKS.put(tw, queue);
        }
        queue.addAll(Arrays.asList(tasks));
    }

    /**
     * 初始化时间轮.
     * <p>
     * 用于: 测试的时候初始化
     * </p>
     *
     * @param max the max
     * @return the tw
     */
    public static Tw initializeTw(int max) {
        long timestamp = TimeUtil.getCurrentTimestampSecond();
        long age = timestamp / max;
        int current = (int) (timestamp % max);
        Tw tw = new Tw(age, current, max);
        TimingWheel.TASKS.put(tw, new ConcurrentLinkedQueue<>());
        // 立即执行: 每秒1次
        ScheduledTaskManager.start(key, 0, 1000);
        return tw;
    }

    /**
     * 初始化时间轮.
     * <p>
     * 用于: 服务器重启时恢复数据
     * </p>
     *
     * @param age     当前轮数
     * @param current 当前时间
     * @param max     最大轮数
     * @param tasks   任务列表
     * @return tw 时间轮
     */
    public static Tw initializeTw(long age, int current, int max, Collection<Task> tasks) {
        Tw tw = new Tw(age, current, max);
        TimingWheel.TASKS.put(tw, new ConcurrentLinkedQueue<>(tasks));
        // 立即执行: 每秒1次
        ScheduledTaskManager.start(key, 0, 1000);
        return tw;
    }

    /**
     * 创建延时任务.
     * <p>
     * 仅到点执行1次
     * </p>
     *
     * @param tw     the tw
     * @param second the second
     * @param run    the run
     * @return the task
     */
    public static void addDelayTask(Tw tw, int second, Runnable run) {
        // 创建任务
        Task task = new Task.DelayTask(tw.calculate(second), second) {
            @Override
            public void run() {
                run.run();
            }
        };
        // 加入任务
        add(tw, task);
    }

    /**
     * 创建定时任务.
     * <p>
     * 到点执行1次, 且自动创建下个执行点
     * </p>
     *
     * @param tw     the tw
     * @param second the second
     * @param run    the run
     */
    public static void addTimedTask(Tw tw, int second, Runnable run) {
        // 创建任务
        Task task = new Task.TimedTask(tw.calculate(second), second) {
            @Override
            public void run() {
                run.run();
            }
        };
        // 加入任务
        add(tw, task);
    }

    /**
     * 创建定时任务.
     * <p>
     * 到点执行1次, 且自动创建下个执行点
     * </p>
     *
     * @param tw   the tw
     * @param cron the cron
     * @param run  the run
     */
    public static void addScheduleTask(Tw tw, String cron, Runnable run) {
        CronSequenceGenerator.isValidExpression(cron);
        // 创建任务
        Task task = new Task.ScheduleTask(tw.calculate(0), new CronSequenceGenerator(cron)) {
            @Override
            public void run() {
                run.run();
            }
        };
        // 加入任务
        add(tw, task);
    }
}
