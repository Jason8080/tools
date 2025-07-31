package cn.gmlee.tools.ai;

import cn.gmlee.tools.ai.mod.Ask;
import cn.gmlee.tools.ai.server.stream.GenerateServer;
import cn.gmlee.tools.ai.server.stream.MultiModalConversationServer;
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
        ask.doOnNext(a -> System.out.println("think..".concat(a.getThink())))
                .doOnNext(a -> System.out.println("reply::".concat(a.getReply())))
                .blockingSubscribe();
        TimerUtil.print();
    }

    /**


     List<Map<String, Object>> users = Arrays.stream(images).map(image -> Collections.singletonMap("image", (Object) image)).collect(Collectors.toList());
     users.add(Collections.singletonMap("text", user));
     MultiModalMessage userMessage = MultiModalMessage.builder().role(Role.USER.getValue())
     .content(users).build();


     */
    @Test
    public void testAskImagesAlike(){ // 合同变量加了空格
        TimerUtil.print();
        Flowable<Ask> ask = multiModalConversationServer.askImages("qwen-vl-max-latest",
                "你的输出只能是true 或 false, 不能有其他任何输出。",
                "你是一名极其严谨且经验丰富的合同审计专家。你的核心任务是精确检测“待比较合同文件”与“原始标准合同模板”之间在文本内容上的任何实质性差异，即潜在的增、删、改，同时智能忽略格式、标点和大小写等非实质性变更。\n" +
                        "\n" +
                        "输入文件\n" +
                        "原始标准合同模板： 这是你进行比对的基准文件，其内容被视为不可变。\n" +
                        "\n" +
                        "待比较合同文件： 这是需要你审计和分析的文件，以识别是否存在篡改。\n" +
                        "\n" +
                        "比对流程与规则\n" +
                        "文本标准化（预处理）：\n" +
                        "在进行任何比对之前，请对两份合同的纯文本内容执行以下标准化操作：\n" +
                        "\n" +
                        "移除所有空白字符： 这包括但不限于空格、制表符和所有形式的换行符。\n" +
                        "\n" +
                        "忽略所有标点符号： 所有标点符号（如逗号、句号、分号、括号、下划线等）应在比对前被移除或忽略。\n" +
                        "\n" +
                        "统一转换为小写： 所有文本内容都应转换为小写字母，以消除大小写差异带来的影响。\n" +
                        "\n" +
                        "目的： 确保比对的焦点纯粹集中在文本字符序列本身，而非其格式或呈现方式。\n" +
                        "\n" +
                        "逐字精准比对：\n" +
                        "对经过上述标准化处理后的两份文本内容进行逐字逐句的精确比对。任何在标准化后导致“待比较合同文件”与“原始标准合同模板”文本序列不一致的地方，都应被视为潜在的篡改。",
                "https://prod-public-ldw.oss-cn-shenzhen.aliyuncs.com/%E4%B8%80%E5%8F%A3%E4%BB%B7%E9%87%87%E8%B4%AD%E5%90%88%E5%90%8C-ZL2025071010152301-YHXMGX250710XMG0001_00.jpg?Expires=11753669119&OSSAccessKeyId=LTAI5t6MaMnjGd7qKm9XjjbN&Signature=8lIvH4EPiZxXZ%2FQS6JAh86x8U%2FY%3D",
                "https://prod-public-ldw.oss-cn-shenzhen.aliyuncs.com/%E4%B8%80%E5%8F%A3%E4%BB%B7%E9%87%87%E8%B4%AD%E5%90%88%E5%90%8C-ZL2025071010152301-YHXMGX250710XMG0001-%E5%89%AF%E6%9C%AC1_00.jpg?Expires=11753688042&OSSAccessKeyId=LTAI5t6MaMnjGd7qKm9XjjbN&Signature=rKXLiY2eVT0UGDiNDVNcyiB6xBk%3D");
        ask.doOnNext(a -> System.out.println("think..".concat(a.getThink())))
                .doOnNext(a -> System.out.println("reply::".concat(a.getReply())))
                .blockingSubscribe();
        TimerUtil.print();
    }


    @Test
    public void testAskImagesNonAlike(){ // 合同模板改了1个字
        TimerUtil.print();
        Flowable<Ask> ask = multiModalConversationServer.askImages("qwen-vl-max-latest",
                "你的输出只能是true 或 false, 不能有其他任何输出。",
                "你是一名极其严谨且经验丰富的合同审计专家。你的核心任务是精确检测“待比较合同文件”与“原始标准合同模板”之间在文本内容上的任何实质性差异，即潜在的增、删、改，同时智能忽略格式、标点和大小写等非实质性变更。\n" +
                        "\n" +
                        "输入文件\n" +
                        "原始标准合同模板： 这是你进行比对的基准文件，其内容被视为不可变。\n" +
                        "\n" +
                        "待比较合同文件： 这是需要你审计和分析的文件，以识别是否存在篡改。\n" +
                        "\n" +
                        "比对流程与规则\n" +
                        "文本标准化（预处理）：\n" +
                        "在进行任何比对之前，请对两份合同的纯文本内容执行以下标准化操作：\n" +
                        "\n" +
                        "移除所有空白字符： 这包括但不限于空格、制表符和所有形式的换行符。\n" +
                        "\n" +
                        "忽略所有标点符号： 所有标点符号（如逗号、句号、分号、括号、下划线等）应在比对前被移除或忽略。\n" +
                        "\n" +
                        "统一转换为小写： 所有文本内容都应转换为小写字母，以消除大小写差异带来的影响。\n" +
                        "\n" +
                        "目的： 确保比对的焦点纯粹集中在文本字符序列本身，而非其格式或呈现方式。\n" +
                        "\n" +
                        "逐字精准比对：\n" +
                        "对经过上述标准化处理后的两份文本内容进行逐字逐句的精确比对。任何在标准化后导致“待比较合同文件”与“原始标准合同模板”文本序列不一致的地方，都应被视为潜在的篡改。",
                "https://prod-public-ldw.oss-cn-shenzhen.aliyuncs.com/%E4%B8%80%E5%8F%A3%E4%BB%B7%E9%87%87%E8%B4%AD%E5%90%88%E5%90%8C-ZL2025071010152301-YHXMGX250710XMG0001_00.jpg?Expires=11753669119&OSSAccessKeyId=LTAI5t6MaMnjGd7qKm9XjjbN&Signature=8lIvH4EPiZxXZ%2FQS6JAh86x8U%2FY%3D",
//                "https://prod-public-ldw.oss-cn-shenzhen.aliyuncs.com/%E4%B8%80%E5%8F%A3%E4%BB%B7%E9%87%87%E8%B4%AD%E5%90%88%E5%90%8C-ZL2025071010152301-YHXMGX250710XMG0001_00.jpg?Expires=11753669119&OSSAccessKeyId=LTAI5t6MaMnjGd7qKm9XjjbN&Signature=8lIvH4EPiZxXZ%2FQS6JAh86x8U%2FY%3D"
                "https://prod-public-ldw.oss-cn-shenzhen.aliyuncs.com/20250728/%E4%B8%80%E5%8F%A3%E4%BB%B7%E9%87%87%E8%B4%AD%E5%90%88%E5%90%8C-ZL2025071010152301-YHXMGX250710XMG0001-%E5%89%AF%E6%9C%AC_001.jpg?Expires=11753684128&OSSAccessKeyId=LTAI5t6MaMnjGd7qKm9XjjbN&Signature=OJLLBPLdbXpNj2ofRpjzBu7aRdk%3D"
        );
        ask.doOnNext(a -> System.out.println("think..".concat(a.getThink())))
                .doOnNext(a -> System.out.println("reply::".concat(a.getReply())))
                .blockingSubscribe();
        TimerUtil.print();
    }


    @Test
    public void testAskImagesNon1() { // 合同模板改了1个字
        testAskImages("请提取合同中的模板内容",
                "https://prod-public-ldw.oss-cn-shenzhen.aliyuncs.com/%E4%B8%80%E5%8F%A3%E4%BB%B7%E9%87%87%E8%B4%AD%E5%90%88%E5%90%8C-ZL2025071010152301-YHXMGX250710XMG0001_00.jpg?Expires=11753669119&OSSAccessKeyId=LTAI5t6MaMnjGd7qKm9XjjbN&Signature=8lIvH4EPiZxXZ%2FQS6JAh86x8U%2FY%3D",
                "https://prod-public-ldw.oss-cn-shenzhen.aliyuncs.com/20250728/%E4%B8%80%E5%8F%A3%E4%BB%B7%E9%87%87%E8%B4%AD%E5%90%88%E5%90%8C-ZL2025071010152301-YHXMGX250710XMG0001-%E5%89%AF%E6%9C%AC_001.jpg?Expires=11753684128&OSSAccessKeyId=LTAI5t6MaMnjGd7qKm9XjjbN&Signature=OJLLBPLdbXpNj2ofRpjzBu7aRdk%3D"
        );
    }


    @Test
    public void testAskImagesNon2() { // 合同变量加了空格
        testAskImages("请提取合同中的模板内容",
                "https://prod-public-ldw.oss-cn-shenzhen.aliyuncs.com/%E4%B8%80%E5%8F%A3%E4%BB%B7%E9%87%87%E8%B4%AD%E5%90%88%E5%90%8C-ZL2025071010152301-YHXMGX250710XMG0001_00.jpg?Expires=11753669119&OSSAccessKeyId=LTAI5t6MaMnjGd7qKm9XjjbN&Signature=8lIvH4EPiZxXZ%2FQS6JAh86x8U%2FY%3D",
                "https://prod-public-ldw.oss-cn-shenzhen.aliyuncs.com/%E4%B8%80%E5%8F%A3%E4%BB%B7%E9%87%87%E8%B4%AD%E5%90%88%E5%90%8C-ZL2025071010152301-YHXMGX250710XMG0001-%E5%89%AF%E6%9C%AC1_00.jpg?Expires=11753688042&OSSAccessKeyId=LTAI5t6MaMnjGd7qKm9XjjbN&Signature=rKXLiY2eVT0UGDiNDVNcyiB6xBk%3D"
        );
    }

    private void testAskImages(String prompt, String source, String target){
        TimerUtil.print();
        Flowable<Ask> ask1 = multiModalConversationServer.askImages("qwen-vl-ocr-latest", null, prompt, source);
        StringBuilder sb1 = new StringBuilder();
        ask1.blockingForEach(a -> sb1.append(a.getReply()));
        System.out.println(sb1);
        TimerUtil.print();
        StringBuilder sb2 = new StringBuilder();
        Flowable<Ask> ask2 = multiModalConversationServer.askImages("qwen-vl-ocr-latest", null, prompt, target);
        ask2.blockingForEach(a -> sb2.append(a.getReply()));
        System.out.println(sb2);
        TimerUtil.print();
        System.out.println(sb1.toString().equalsIgnoreCase(sb2.toString()));
    }

}
