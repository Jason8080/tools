package cn.gmlee.tools.ai;

import cn.gmlee.tools.ai.server.impl.DashScopeServer;
import io.reactivex.Flowable;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = App.class)
public class DashScopeWlTests {

    @Autowired
    private DashScopeServer dashScopeServer;

    private static final String sys = "你是一个具备磅单识别的视觉模型," +
            "用户接下来会把他的磅单图片上传给你并有一个计算的需求," +
            "这些磅单图片可能是打印的,也可能是手写的,还可能一张图片里有多张磅单" +
            "每张磅单里面一般有毛重、皮重、净重," +
            "每个重量的单位(KG/吨)可能在数字前也可能在数字后," +
            "你会根据磅单的排版自行推断出相关重量的单位," +
            "在用户所需的计算时你还是一个数学专家," +
            "能够根据用户的计算需求,统一将重量转换成吨(1吨=1000KG), " +
            "并精准的计算出用户所需的净重。"
//             + "值得注意的是: 你只需要返回最终的数字结果不需要任何其他内容描述"
            ;

    @Test
    public void testImage(){
        // 1911610483048722433
        Flowable<String> ask = dashScopeServer.askImage(sys, "请帮我计算出磅单中的净重(单位:吨),多张磅单再计算净重的总和返回,只要数字",
                "https://prod-public-ldw.oss-cn-shenzhen.aliyuncs.com/20250414/%E5%BE%AE%E4%BF%A1%E5%9B%BE%E7%89%87_202504140954571.png?Expires=1744599697&OSSAccessKeyId=LTAI5t6MaMnjGd7qKm9XjjbN&Signature=ZMQo1bTrw5Jl9kU6T8ikSoG%2FFTQ%3D");
        StringBuilder sb = new StringBuilder();
        ask.blockingForEach(x -> sb.append(x));
        System.out.println(sb);
    }

    @Test
    public void testImage2(){
        // 1911605536991227905
        Flowable<String> ask = dashScopeServer.askImage(sys, "请帮我计算出磅单中的净重(单位:吨),多张磅单再计算净重的总和返回,只要数字",
                "https://prod-public-ldw.oss-cn-shenzhen.aliyuncs.com/%E5%BE%AE%E4%BF%A1%E5%9B%BE%E7%89%87_20250414095502.png?Expires=1744600028&OSSAccessKeyId=LTAI5t6MaMnjGd7qKm9XjjbN&Signature=Vx9k%2BW3x8R2q2%2BPfLf1uqtLX2Sw%3D");
        StringBuilder sb = new StringBuilder();
        ask.blockingForEach(x -> sb.append(x));
        System.out.println(sb);
    }

}
