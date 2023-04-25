package cn.gmlee.tools.base;

import cn.gmlee.tools.base.kit.task.TimerTaskManager;
import cn.gmlee.tools.base.util.TimeUtil;

import java.io.Serializable;

public class TaskTests {
    public static void main(String[] args) throws Exception {
        Serializable key1 = TimerTaskManager.submit(() -> System.out.println("1: " + TimeUtil.getCurrentDatetime()));
        TimerTaskManager.start(key1);
        Serializable key2 = TimerTaskManager.submit(() -> System.out.println("2: " + TimeUtil.getCurrentDatetime()));
        TimerTaskManager.start(key2);
        TimerTaskManager.suspend(key1);
        TimerTaskManager.restart(key1);
    }
}
