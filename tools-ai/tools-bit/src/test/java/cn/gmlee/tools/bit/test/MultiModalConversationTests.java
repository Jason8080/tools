package cn.gmlee.tools.bit.test;

import cn.gmlee.tools.base.util.CharUtil;
import cn.gmlee.tools.base.util.NullUtil;
import cn.gmlee.tools.bit.App;
import cn.gmlee.tools.bit.server.stream.MultiModalConversationServer;
import cn.gmlee.tools.base.mod.Ask;
import cn.gmlee.tools.base.util.TimerUtil;
import io.reactivex.Flowable;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = App.class)
public class MultiModalConversationTests {

    @Autowired
    private MultiModalConversationServer multiModalConversationServer;

    @Test
    public void testAsk(){
        TimerUtil.print();
        Flowable<Ask> ask = multiModalConversationServer.ask(null, "我是谁?");
        StringBuilder think = new StringBuilder();
        StringBuilder reply = new StringBuilder();
        ask.doOnNext(a -> {
            think.append(a.getThink());
            reply.append(a.getReply());
            System.out.println(CharUtil.firstNonEmpty(a.getThink(), a.getReply()));
        }).blockingSubscribe();
        System.out.println(think);
        System.out.println("-----------------------------------------------------------------------------------------");
        System.out.println(reply);
        TimerUtil.print();
    }

    @Test
    public void testAskImage(){
        TimerUtil.print();
        Flowable<Ask> ask = multiModalConversationServer.askImages(null, null, "核心任务：\n" +
                        "\n" +
                        "从提供的磅单图片中，准确提取并识别以下核心信息：公司名称、净重、毛重和单位。\n" +
                        "\n" +
                        "输出格式：\n" +
                        "\n" +
                        "请严格按照以下JSON数组格式输出结果，所有字段值必须是字符串类型：\n" +
                        "\n" +
                        "[{\"公司名称\":\"...\", \"净重\":\"...\", \"毛重\":\"...\", \"单位\":\"...\"}]\n" +
                        "\n" +
                        "识别与提取规则：\n" +
                        "\n" +
                        "数据错位处理：\n" +
                        "\n" +
                        "图片中的标签和数值可能存在错行，AI需要根据上下文和语义进行智能匹配。\n" +
                        "\n" +
                        "示例场景： 如果“净重”标签在第一行，而其数值在第二行，AI仍应正确关联。\n" +
                        "\n" +
                        "公司名称识别：\n" +
                        "\n" +
                        "优先： 提取磅单图片中最醒目、通常位于顶部的“标题”作为公司名称。\n" +
                        "\n" +
                        "示例： 如果磅单顶部显示“开封正大有限公司”，则 {\"公司名称\":\"开封正大有限公司\", ...}\n" +
                        "\n" +
                        "次之： 如果无明确标题或标题不适合，则查找“供应商”或类似标签旁边的值。\n" +
                        "\n" +
                        "示例： 如果有“供应商：某某公司”，则 {\"公司名称\":\"某某公司\", ...}\n" +
                        "\n" +
                        "默认： 如果上述均无法识别，则返回“非磅单”。\n" +
                        "\n" +
                        "示例： 如果图片内容无法判断为磅单，则 {\"公司名称\":\"非磅单\", ...}\n" +
                        "\n" +
                        "净重 (Net Weight) 识别：\n" +
                        "\n" +
                        "最高优先级： 严格优先读取标签为“净重”旁边的数值。\n" +
                        "\n" +
                        "示例： 图片中显示 净重: 44,600.00，则 {\"净重\":\"44600.00\", ...}\n" +
                        "\n" +
                        "次要优先级（当无“净重”标签时）：\n" +
                        "\n" +
                        "查找“去包装净重”标签旁的数值。\n" +
                        "\n" +
                        "示例： 去包装净重: 25.000，则 {\"净重\":\"25.000\", ...}\n" +
                        "\n" +
                        "查找“收货净重”标签旁的数值。\n" +
                        "\n" +
                        "示例： 收货净重: 100.50，则 {\"净重\":\"100.50\", ...}\n" +
                        "\n" +
                        "兜底策略（当无任何净重相关标签时）：\n" +
                        "\n" +
                        "若图片中存在“实际卸船吨位”，则读取其数值作为净重。\n" +
                        "\n" +
                        "示例： 实际卸船吨位: 500.00，则 {\"净重\":\"500.00\", ...}\n" +
                        "\n" +
                        "若图片中存在“毛重”标签，则读取其数值作为净重。\n" +
                        "\n" +
                        "示例： 如果“净重”类标签均无，但有 毛重: 98.520，则 {\"净重\":\"98.520\", ...}\n" +
                        "\n" +
                        "特殊情况处理：\n" +
                        "\n" +
                        "如果识别到的“净重”数值与“称重”数值完全相同，则改为读取“毛重”对应的数值作为净重。\n" +
                        "\n" +
                        "示例： 磅单显示 净重: 50.00, 称重: 50.00, 毛重: 100.00，则 {\"净重\":\"100.00\", ...}\n" +
                        "\n" +
                        "毛重 (Gross Weight) 识别：\n" +
                        "\n" +
                        "最高优先级： 读取标签为“毛重”旁边的数值。\n" +
                        "\n" +
                        "示例： 图片中显示 毛重: 98.526，则 {\"毛重\":\"98.526\", ...}\n" +
                        "\n" +
                        "次要优先级（当无“毛重”标签时）： 依次查找并读取以下标签旁的数值：\n" +
                        "\n" +
                        "“总重”\n" +
                        "\n" +
                        "示例： 总重: 120.00，则 {\"毛重\":\"120.00\", ...}\n" +
                        "\n" +
                        "“总重量”\n" +
                        "\n" +
                        "示例： 总重量: 150.00，则 {\"毛重\":\"150.00\", ...}\n" +
                        "\n" +
                        "“一次称重”\n" +
                        "\n" +
                        "示例： 一次称重: 130.00，则 {\"毛重\":\"130.00\", ...}\n" +
                        "\n" +
                        "“二次称重”\n" +
                        "\n" +
                        "示例： 二次称重: 10.00 (这可能是皮重，AI需自行判断，但此处按规则读取)，则 {\"毛重\":\"10.00\", ...}\n" +
                        "\n" +
                        "“称重”\n" +
                        "\n" +
                        "示例： 称重: 200.00，则 {\"毛重\":\"200.00\", ...}\n" +
                        "\n" +
                        "默认： 如果上述所有标签均未找到，则返回“0”。\n" +
                        "\n" +
                        "示例： 如果无法识别毛重，则 {\"毛重\":\"0\", ...}\n" +
                        "\n" +
                        "单位 (Unit) 识别：\n" +
                        "\n" +
                        "优先级： 读取“单位”标签旁的文字，或在净重/毛重数值后面紧跟的单位。\n" +
                        "\n" +
                        "常见示例：\n" +
                        "\n" +
                        "净重: 28.439 KG -> {\"单位\":\"KG\", ...}\n" +
                        "\n" +
                        "毛重: 98.52 吨 -> {\"单位\":\"吨\", ...}\n" +
                        "\n" +
                        "单位: 公斤 -> {\"单位\":\"公斤\", ...}\n" +
                        "\n" +
                        "净重(t): 50.00 -> {\"单位\":\"t\", ...}\n" +
                        "\n" +
                        "默认： 如果无法识别任何单位，则返回“无”。\n" +
                        "\n" +
                        "示例： {\"单位\":\"无\", ...}\n" +
                        "\n" +
                        "数值格式处理：\n" +
                        "\n" +
                        "千分位逗号： 净重和毛重数值中的千分位逗号（,）应被忽略，不作为小数点。\n" +
                        "\n" +
                        "示例： 28,430.00 应读取为 \"28430.00\"。\n" +
                        "\n" +
                        "示例： 98,520 应读取为 \"98520\"。\n" +
                        "\n" +
                        "小数精度： 保持数值的原始精度，不应随意增减小数点后的位数。\n" +
                        "\n" +
                        "示例： 28.439 应读取为 \"28.439\"。\n" +
                        "\n" +
                        "示例： 98.52 应读取为 \"98.52\"。\n" +
                        "\n" +
                        "示例： 98.520 应读取为 \"98.520\"。"
                , "https://prod-public-ldw.oss-cn-shenzhen.aliyuncs.com/%E5%A4%9A%E5%87%80%E9%87%8D1.jpg?Expires=11755741577&OSSAccessKeyId=LTAI5t6MaMnjGd7qKm9XjjbN&Signature=VEodSrvGdthE0QgM1seWyG5uTQ4%3D"
                , "https://prod-public-ldw.oss-cn-shenzhen.aliyuncs.com/%E5%A4%9A%E5%87%80%E9%87%8D2.jpg?Expires=11755741632&OSSAccessKeyId=LTAI5t6MaMnjGd7qKm9XjjbN&Signature=D8OZzwBKWP6SRcl6ENKRTazlPOA%3D"
        );
        StringBuilder think = new StringBuilder();
        StringBuilder reply = new StringBuilder();
        ask.doOnNext(a -> {
            think.append(a.getThink());
            reply.append(a.getReply());
            System.out.println(CharUtil.firstNonEmpty(a.getThink(), a.getReply()));
        }).blockingSubscribe();
        System.out.println(think);
        System.out.println("-----------------------------------------------------------------------------------------");
        System.out.println(reply);
        TimerUtil.print();
    }
}
