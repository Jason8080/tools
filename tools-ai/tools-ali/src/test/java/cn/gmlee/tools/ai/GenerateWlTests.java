package cn.gmlee.tools.ai;

import cn.gmlee.tools.ai.mod.Ask;
import cn.gmlee.tools.ai.server.stream.GenerateServer;
import cn.gmlee.tools.base.enums.Regex;
import cn.gmlee.tools.base.util.RegexUtil;
import cn.gmlee.tools.base.util.TimerUtil;
import io.reactivex.Flowable;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = App.class)
public class GenerateWlTests {

    @Autowired
    private GenerateServer generateServer;

    private static final String sys = "用户接下来会把他的磅单图片上传给你,并希望你只返回1个数字结果。" +
            "这些磅单图片可能是打印的,也可能是手写的,还可能一张图片里有多张磅单" +
            "每张磅单里面一般有毛重、皮重、净重," +
            "每个重量的单位(KG/吨)可能在数字前也可能在数字后," +
            "你会根据磅单的排版自行推断出相关重量的单位," +
            "在用户需要计算时能够根据用户的计算需求,统一将重量转换成吨(1吨=1000KG)。"
            ;

    @Test
    public void testAsk(){
        // 1911610483048722433
        TimerUtil.print();
        Flowable<Ask> ask = generateServer.ask(sys, "计算磅单中的净重,单位转换成吨(1吨=1000KG),多张磅单则求和,只要数字");
        StringBuilder sb = new StringBuilder();
        ask.blockingForEach(x -> sb.append(x.getReply()));
        System.out.println(sb);
        System.out.println(RegexUtil.last(sb.toString(), Regex.NUMBER.regex, true));
        TimerUtil.print();
    }

}
