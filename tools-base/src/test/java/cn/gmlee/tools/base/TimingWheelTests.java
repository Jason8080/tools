package cn.gmlee.tools.base;

import cn.gmlee.tools.base.alg.timing.wheel.TimingWheel;
import cn.gmlee.tools.base.alg.timing.wheel.Tw;
import cn.gmlee.tools.base.util.TimeUtil;
import org.junit.Test;

public class TimingWheelTests {

    @Test
    public void testTimingWheel() throws Exception {
        // 创建轮盘
        Tw tw = TimingWheel.initializeTw(6);
        // 添加任务 (延时)
        TimingWheel.addDelayTask(tw, 10, () -> System.out.println(TimeUtil.getCurrentDatetime()));
        // 添加任务 (定时)
        TimingWheel.addTimedTask(tw, 3, () -> System.out.println(TimeUtil.getCurrentDatetime()));
        System.in.read();
    }
}
