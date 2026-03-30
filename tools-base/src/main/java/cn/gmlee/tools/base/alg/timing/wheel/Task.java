package cn.gmlee.tools.base.alg.timing.wheel;

import cn.gmlee.tools.base.util.TimeUtil;
import lombok.EqualsAndHashCode;
import org.springframework.scheduling.support.CronExpression;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * 任务定义.
 */
@EqualsAndHashCode(callSuper = true)
public abstract class Task extends Time implements Runnable {
    /**
     * 已废弃
     */
    protected boolean discard;

    /**
     * Instantiates a new Time.
     *
     * @param age the age
     */
    private Task(long age, int current) {
        super(age, current);
    }

    /**
     * Reset.
     *
     * @param tw the tw
     */
    public abstract void reset(Tw tw);

    /**
     * Allow boolean.
     *
     * @param tw the tw
     * @return the boolean
     */
    public abstract boolean allow(Tw tw);

    /**
     * 延时任务.
     */
    public static abstract class DelayTask extends Task {
        /**
         * The Period.
         */
        protected int period;

        /**
         * Instantiates a new Task.
         *
         * @param time   the time
         * @param period the period
         */
        public DelayTask(Time time, int period) {
            super(time.age.get(), time.current.get());
            this.period = period;
        }

        @Override
        public void reset(Tw tw) {
            Time time = tw.calculate(this.period);
            this.age.set(time.age.get());
            this.current.set(time.current.get());
        }

        @Override
        public boolean allow(Tw tw) {
            if (tw == null) {
                return false;
            }
            return this.age.get() == tw.age.get() && this.current.get() == tw.current.get();
        }
    }

    /**
     * 定时任务.
     */
    public static abstract class TimedTask extends Task {
        /**
         * The Period.
         */
        protected int period;

        /**
         * Instantiates a new Task.
         *
         * @param time   the time
         * @param period the period
         */
        public TimedTask(Time time, int period) {
            super(time.age.get(), time.current.get());
            this.period = period;
        }

        @Override
        public void reset(Tw tw) {
            Time time = tw.calculate(this.period);
            this.age.set(time.age.get());
            this.current.set(time.current.get());
        }

        @Override
        public boolean allow(Tw tw) {
            if (tw == null) {
                return false;
            }
            return this.age.get() == tw.age.get() && this.current.get() == tw.current.get();
        }
    }

    /**
     * 准时任务.
     */
    public static abstract class ScheduleTask extends Task {
        protected long next;
        protected long last;
        protected CronExpression cron;

        /**
         * Instantiates a new Task.
         *
         * @param time the time
         * @param cron the cron
         */
        public ScheduleTask(Time time, CronExpression cron) {
            super(time.age.get(), time.current.get());
            this.cron = cron;
            this.next = TimeUtil.getTimestampSecond(nextCronAfter(cron, TimeUtil.getCurrentDate()));
        }

        private static Date nextCronAfter(CronExpression cron, Date from) {
            LocalDateTime ldt = LocalDateTime.ofInstant(from.toInstant(), ZoneId.systemDefault());
            LocalDateTime next = cron.next(ldt);
            if (next == null) {
                return from;
            }
            return Date.from(next.atZone(ZoneId.systemDefault()).toInstant());
        }

        @Override
        public void reset(Tw tw) {
            // 重置时间
            this.last = TimeUtil.getCurrentTimestampSecond();
            // 计算下次
            Date current = nextCronAfter(cron, TimeUtil.getCurrentDate());
            this.next = TimeUtil.getTimestampSecond(current);
        }

        @Override
        public boolean allow(Tw tw) {
            if (tw == null) {
                return false;
            }
            Long current = TimeUtil.getCurrentTimestampSecond();
            return last != next && current == next;
        }
    }
}
