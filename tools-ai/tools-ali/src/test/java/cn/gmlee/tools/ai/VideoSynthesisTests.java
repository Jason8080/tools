package cn.gmlee.tools.ai;

import cn.gmlee.tools.ai.server.async.ImageSynthesisServer;
import cn.gmlee.tools.ai.server.async.VideoSynthesisServer;
import cn.gmlee.tools.base.util.TimerUtil;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesisListResult;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesisOutput;
import com.alibaba.dashscope.aigc.videosynthesis.VideoSynthesisListResult;
import com.alibaba.dashscope.aigc.videosynthesis.VideoSynthesisOutput;
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
public class VideoSynthesisTests {

    @Autowired
    private VideoSynthesisServer videoSynthesisServer;

    /**
     * Test ask.
     */
    @Test
    public void testAsk(){
        TimerUtil.print();
        VideoSynthesisOutput ask = videoSynthesisServer.ask("一位身穿黑色丝袜的修长美女正在热舞,背景是极简的酒店装修,抖音直播风格,可以清晰的看到主播曼妙的诱人身姿");
        System.out.println(ask);
        TimerUtil.print();
    }

    /**
     * Test get.
     */
    @Test
    public void testGet(){
        TimerUtil.print();
        VideoSynthesisOutput ask = videoSynthesisServer.get("508784d8-26ae-4651-a640-409f1361d510");
        System.out.println(ask);
        TimerUtil.print();
    }

    /**
     * Test page.
     */
    @Test
    public void testPage(){
        TimerUtil.print();
        VideoSynthesisListResult page = videoSynthesisServer.page();
        System.out.println(JsonUtils.toJson(page));
        TimerUtil.print();
    }

}
