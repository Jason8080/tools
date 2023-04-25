package cn.gmlee.tools.base.kit.task;

import lombok.Getter;

import java.util.TimerTask;

/**
 * 任务.
 */
public class Task extends TimerTask {
    @Getter
    private long count;
    protected Runnable run;
    protected long delay;
    protected long period;

    /**
     * Instantiates a new Task.
     */
    public Task() {
        this.delay = 0;
        this.period = 1000;
    }

    /**
     * Instantiates a new Task.
     *
     * @param period the period
     */
    public Task(long period) {
        this.delay = 0;
        this.period = period;
    }

    /**
     * Instantiates a new Task.
     *
     * @param delay  the delay
     * @param period the period
     */
    public Task(long delay, long period) {
        this.delay = delay;
        this.period = period;
    }

    /**
     * Run.
     */
    @Override
    public void run() {
        run.run();
        if(count != Long.MAX_VALUE){
            count++;
        }
    }

    @Override
    protected Task clone() {
        Task task = new Task(this.delay, this.period);
        task.run = this.run;
        task.count = this.count;
        return task;
    }
}
