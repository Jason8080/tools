package cn.gmlee.tools.base.kit.task;

/**
 * 任务.
 */
public class ScheduledTask extends AbstractTask {
    /**
     * Instantiates a new Scheduled task.
     */
    public ScheduledTask() {
    }

    /**
     * Instantiates a new Scheduled task.
     *
     * @param period the period
     */
    public ScheduledTask(long period) {
        super(period);
    }

    /**
     * Instantiates a new Scheduled task.
     *
     * @param delay  the delay
     * @param period the period
     */
    public ScheduledTask(long delay, long period) {
        super(delay, period);
    }
}
