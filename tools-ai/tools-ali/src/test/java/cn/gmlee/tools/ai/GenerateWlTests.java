package cn.gmlee.tools.ai;

import cn.gmlee.tools.ai.server.impl.GenerateServer;
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
        Flowable<String> ask = generateServer.ask(sys, "请帮我计算出磅单中的净重(单位:吨),多张磅单再计算净重的总和返回");
        StringBuilder sb = new StringBuilder();
        ask.blockingForEach(x -> sb.append(x));
        System.out.println(sb);
        TimerUtil.print();
    }

    @Test
    public void testImage2(){
        // 1911610483048722433
        TimerUtil.print();
        Flowable<String> ask = generateServer.askImage(sys, "请帮我计算出磅单中的净重(单位:吨),多张磅单再计算净重的总和返回",
                "https://prod-public-ldw.oss-cn-shenzhen.aliyuncs.com/%E5%BE%AE%E4%BF%A1%E5%9B%BE%E7%89%87_20250414095502.png?Expires=11744617068&OSSAccessKeyId=LTAI5t6MaMnjGd7qKm9XjjbN&Signature=%2By4fyMAD%2BQ16uEvjKNv6j8oKWsk%3D");
        StringBuilder sb = new StringBuilder();
        ask.blockingForEach(x -> sb.append(x));
        System.out.println(sb);
        TimerUtil.print();
    }

}
