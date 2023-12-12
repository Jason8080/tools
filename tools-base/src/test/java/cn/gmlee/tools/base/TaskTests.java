package cn.gmlee.tools.base;

import cn.gmlee.tools.base.kit.task.ScheduledTaskManager;
import cn.gmlee.tools.base.kit.task.TimerTaskManager;
import cn.gmlee.tools.base.util.TimeUtil;
import org.junit.Test;

import java.io.Serializable;

public class TaskTests {

    @Test
    public void testTimer() throws Exception {
        Serializable key1 = TimerTaskManager.submit(() -> System.out.println("1: " + TimeUtil.getCurrentDatetime()));
        TimerTaskManager.start(key1);
        Serializable key2 = TimerTaskManager.submit(() -> System.out.println("2: " + TimeUtil.getCurrentDatetime()));
        TimerTaskManager.start(key2);
        TimerTaskManager.suspend(key1);
        TimerTaskManager.restart(key1);
        System.in.read();
    }

    @Test
    public void testScheduled() throws Exception {
        Serializable key1 = ScheduledTaskManager.submit(() -> System.out.println("1: " + TimeUtil.getCurrentDatetime()));
        ScheduledTaskManager.start(key1);
        Serializable key2 = ScheduledTaskManager.submit(() -> System.out.println("2: " + TimeUtil.getCurrentDatetime()));
        ScheduledTaskManager.start(key2);
        System.in.read();
    }
}
