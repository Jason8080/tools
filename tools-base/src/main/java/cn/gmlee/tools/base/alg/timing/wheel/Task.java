package cn.gmlee.tools.base.alg.timing.wheel;

import lombok.EqualsAndHashCode;

/**
 * 任务定义.
 */
@EqualsAndHashCode(callSuper = true)
public abstract class Task extends Time implements Runnable {
    /**
     * 间隔时间
     */
    protected int period;
    /**
     * 已废弃
     */
    protected boolean discard;
    /**
     * Instantiates a new Time.
     *
     * @param age     the age
     * @param current the current
     */
    private Task(int age, int current, int period) {
        super(age, current);
        this.period = period;
    }

    /**
     * Reset.
     *
     * @param tw the tw
     */
    public void reset(Tw tw) {
        Time time = tw.calculate(this.period);
        this.age.set(time.age.get());
        this.current.set(time.current.get());
    }

    /**
     * 延时任务.
     */
    public static abstract class DelayTask extends Task {
        /**
         * Instantiates a new Task.
         *
         * @param time   the time
         * @param period the period
         */
        public DelayTask(Time time, int period) {
            super(time.age.get(), time.current.get(), period);
        }
    }

    /**
     * 定时任务.
     */
    public static abstract class TimedTask extends Task {

        /**
         * Instantiates a new Task.
         *
         * @param time   the time
         * @param period the period
         */
        public TimedTask(Time time, int period) {
            super(time.age.get(), time.current.get(), period);
        }
    }
}
