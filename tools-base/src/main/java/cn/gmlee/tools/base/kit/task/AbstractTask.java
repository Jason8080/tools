package cn.gmlee.tools.base.kit.task;

import cn.gmlee.tools.base.util.ExceptionUtil;
import lombok.Getter;

/**
 * 任务.
 */
public class AbstractTask extends java.util.TimerTask implements Task {
    @Getter
    protected long count;
    protected Runnable run;
    protected long delay;
    protected long period;

    /**
     * Instantiates a new Task.
     */
    public AbstractTask() {
        this.delay = 0;
        this.period = 1000;
    }

    /**
     * Instantiates a new Task.
     *
     * @param period the period
     */
    public AbstractTask(long period) {
        this.delay = 0;
        this.period = period;
    }

    /**
     * Instantiates a new Task.
     *
     * @param delay  the delay
     * @param period the period
     */
    public AbstractTask(long delay, long period) {
        this.delay = delay;
        this.period = period;
    }

    /**
     * Run.
     */
    @Override
    public void run() {
        // 沙箱执行
        ExceptionUtil.sandbox(run::run);
        // 计数统计
        if(count != Long.MAX_VALUE){
            count++;
        }
    }
}
