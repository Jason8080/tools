package cn.gmlee.tools.base;

import cn.gmlee.tools.base.alg.timing.wheel.TimingWheel;
import cn.gmlee.tools.base.alg.timing.wheel.Tw;
import cn.gmlee.tools.base.enums.XTime;
import cn.gmlee.tools.base.util.TimeUtil;
import org.junit.Test;
import org.springframework.scheduling.support.CronSequenceGenerator;

import java.io.*;
import java.util.Date;

public class TimingWheelTests {

    @Test
    public void testTimingWheel() throws Exception {
        // 创建轮盘
        Tw tw = TimingWheel.initializeTw(6);
        // 添加任务 (延时)
        TimingWheel.addDelayTask(tw, 10, () -> System.out.println("延时+1 ------ " + TimeUtil.getCurrentDatetime()));
        // 添加任务 (定时)
        TimingWheel.addTimedTask(tw, 3, () -> System.out.println("定时+1 ------ " + TimeUtil.getCurrentDatetime()));
        // 添加任务 (准时)
        TimingWheel.addScheduleTask(tw, "0/5 * * * * ?", () -> System.out.println("准时+1 ------ " + TimeUtil.getCurrentDatetime()));
        System.in.read();
    }

    @Test
    public void testCron() throws Exception {
        String cron = "0/5 * * * * ?";
        CronSequenceGenerator.isValidExpression(cron);
        CronSequenceGenerator generator = new CronSequenceGenerator(cron);
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        String content = "请输入";
        while ((content = in.readLine()) != null){
            Date next = generator.next(TimeUtil.getCurrentDate());
            System.out.println(TimeUtil.format(next, XTime.SECOND_MINUS_BLANK_COLON));
        }
    }
}
