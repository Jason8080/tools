package cn.gmlee.tools.ai;

import cn.gmlee.tools.ai.server.async.ImageSynthesisServer;
import cn.gmlee.tools.base.util.TimerUtil;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesisListResult;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesisOutput;
import com.alibaba.dashscope.utils.JsonUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * The type Image synthesis tests.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = App.class)
public class ImageSynthesisTests {

    @Autowired
    private ImageSynthesisServer imageSynthesisServer;

    /**
     * Test ask.
     */
    @Test
    public void testAsk(){
        TimerUtil.print();
        ImageSynthesisOutput ask = imageSynthesisServer.ask("一位身穿黑色丝袜的修长美女正在热舞,背景是极简的酒店装修,抖音直播风格,可以清晰的看到主播曼妙的诱人身姿");
        System.out.println(ask);
        TimerUtil.print();
    }

    /**
     * Test get.
     */
    @Test
    public void testGet(){
        TimerUtil.print();
        ImageSynthesisOutput ask = imageSynthesisServer.get("23ae4211-1838-4cca-9745-f82c35020d8b");
        System.out.println(ask);
        TimerUtil.print();
    }

    /**
     * Test page.
     */
    @Test
    public void testPage(){
        TimerUtil.print();
        ImageSynthesisListResult page = imageSynthesisServer.page();
        System.out.println(JsonUtils.toJson(page));
        TimerUtil.print();
    }

}
