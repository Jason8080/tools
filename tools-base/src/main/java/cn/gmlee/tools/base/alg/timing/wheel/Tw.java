package cn.gmlee.tools.base.alg.timing.wheel;

import cn.gmlee.tools.base.util.ThreadUtil;
import lombok.EqualsAndHashCode;

/**
 * 时间轮定义.
 */
@EqualsAndHashCode(callSuper = true)
public class Tw extends Time implements Runnable {
    /**
     * 任务线程池
     */
    private static final ThreadUtil.Pool pool = ThreadUtil.getFixedPool(Runtime.getRuntime().availableProcessors());

    /**
     * Instantiates a new Tw.
     *
     * @param age     the age
     * @param current the current
     * @param max     the max
     */
    public Tw(long age, int current, int max) {
        super(age, current);
        this.max = max;
    }

    /**
     * 最大时间
     */
    protected final Integer max;

    /**
     * 当前时间
     *
     * @return 返回当前时间 long
     */
    public long current() {
        return (long) age.get() * max + current.get();
    }

    /**
     * 转动指针.
     */
    @Override
    public void run() {
        // 转动指针
        if (current.intValue() == max) {
            current.set(0);
            // 转动年轮
            if (age.get() == Long.MAX_VALUE) {
                age.set(0);
            }
            age.incrementAndGet();
        }
        current.incrementAndGet();
    }


    /**
     * Calculate time.
     *
     * @param second the second
     * @return the time
     */
    public Time calculate(int second) {
        int mod = second % this.max;
        long age = mod + this.current.get() > this.max ? second / this.max + 1 : second / this.max;
        int current = mod + this.current.get() > this.max ? this.max - mod : mod + this.current.get();
        long diffAge = Long.MAX_VALUE - this.current.get();
        long newAge = diffAge >= age ? this.age.get() + age : age - diffAge;
        return new Time(newAge, current);
    }

    /**
     * Execute.
     *
     * @param task the task
     */
    public void execute(Task task) {
        // 检查
        if (!task.allow(this)) {
            return;
        }
        pool.execute(task);
        // 重置
        if (task instanceof Task.TimedTask || task instanceof Task.ScheduleTask) {
            task.reset(this);
            return;
        }
        // 废弃
        task.discard = true;
    }
}
