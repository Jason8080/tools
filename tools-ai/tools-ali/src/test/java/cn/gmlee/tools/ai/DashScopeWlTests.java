package cn.gmlee.tools.ai;

import cn.gmlee.tools.ai.server.stream.DashScopeServer;
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
public class DashScopeWlTests {

    @Autowired
    private DashScopeServer dashScopeServer;

    private static final String sys = "用户接下来会把他的磅单图片上传给你,并希望你只返回1个数字结果。" +
            "这些磅单图片可能是打印的,也可能是手写的,还可能一张图片里有多张磅单" +
            "每张磅单里面一般有毛重、皮重、净重," +
            "每个重量的单位(KG/吨)可能在数字前也可能在数字后," +
            "你会根据磅单的排版自行推断出相关重量的单位," +
            "在用户需要计算时能够根据用户的计算需求,统一将重量转换成吨(1吨=1000KG)。"
            ;

    @Test
    public void testImage(){
        // 1911610483048722433
        TimerUtil.println();
        Flowable<String> ask = dashScopeServer.askImage("", "计算磅单中的净重,单位转换成吨(1吨=1000KG),多张磅单则求和,只要数字",
                "https://prod-public-ldw.oss-cn-shenzhen.aliyuncs.com/20250414/%E5%BE%AE%E4%BF%A1%E5%9B%BE%E7%89%87_202504140954571.png?Expires=11744617127&OSSAccessKeyId=LTAI5t6MaMnjGd7qKm9XjjbN&Signature=Q8WdJdlri8ldLDffmpeTKgoHyWU%3D");
        StringBuilder sb = new StringBuilder();
        ask.blockingForEach(x -> sb.append(x));
        System.out.println(sb);
        System.out.println(RegexUtil.last(sb.toString(), Regex.NUMBER.regex, true));
        TimerUtil.println();
    }

    @Test
    public void testImage2(){
        // 1911605536991227905
        // 1914264723835596802 错行
        TimerUtil.println();
        Flowable<String> ask = dashScopeServer.askImage("",
                "请分析图片磅单中的净重",
                "https://prod-public-ldw.oss-cn-shenzhen.aliyuncs.com/%E6%A8%AA%E5%90%91%E9%94%99%E8%A1%8C%E7%A3%85%E5%8D%951.png?Expires=11745231268&OSSAccessKeyId=LTAI5t6MaMnjGd7qKm9XjjbN&Signature=vv4Zt%2BzO9L2oWJ%2BAbs%2BkIECQBcQ%3D"
        );
        StringBuilder sb = new StringBuilder();
        ask.blockingForEach(x -> sb.append(x));
        System.out.println(sb);
        System.out.println(RegexUtil.last(sb.toString(), Regex.NUMBER.regex, true));
        TimerUtil.println();
    }


    public static void main(String[] args) {
        System.out.println(RegexUtil.last("图片中的磅单显示了两个净重数值，需要结合上下文分析：\n" +
                "\n" +
                "1. **过磅记录中的净重**：  \n" +
                "   出厂时的净重明确标示为 **50,440.00 公斤**。但根据常规逻辑，毛重（17,680 公斤）减去皮重（2 吨=2,000 公斤）应为 **15,680 公斤**，与该数值矛盾，可能存在数据录入错误或单位混淆。\n" +
                "\n" +
                "2. **存货位置的净重**：  \n" +
                "   明确标示为 **32,740.00 公斤**，并注明为“去包装净重”。这一数值更符合实际仓储场景（扣除包装后的货物重量），且与“去包装净重”栏的 **32,760.00 公斤** 接近（可能因四舍五入差异）。\n" +
                "\n" +
                "**结论**：  \n" +
                "由于过磅记录中的净重与常规计算逻辑冲突，而存货位置的净重更贴近实际仓储流程，**最终净重应为 32,740.00 公斤**。若需严格按磅单直接显示，则需注明数据可能存在矛盾。", Regex.NUMBER.regex, true));
    }

}
