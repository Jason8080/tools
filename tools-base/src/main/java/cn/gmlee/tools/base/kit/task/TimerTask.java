package cn.gmlee.tools.base.kit.task;

/**
 * 任务.
 */
public class TimerTask extends AbstractTask {
    /**
     * Instantiates a new Timer task.
     */
    public TimerTask() {
        super();
    }

    /**
     * Instantiates a new Timer task.
     *
     * @param period the period
     */
    public TimerTask(long period) {
        super(period);
    }

    /**
     * Instantiates a new Timer task.
     *
     * @param delay  the delay
     * @param period the period
     */
    public TimerTask(long delay, long period) {
        super(delay, period);
    }

    protected TimerTask clone() {
        TimerTask task = new TimerTask(this.delay, this.period);
        task.run = this.run;
        task.count = this.count;
        return task;
    }
}
