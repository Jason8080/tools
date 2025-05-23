package cn.gmlee.tools.ai;

import cn.gmlee.tools.ai.server.async.VideoSynthesisServer;
import cn.gmlee.tools.base.util.TimerUtil;
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
        TimerUtil.printf();
        VideoSynthesisOutput ask = videoSynthesisServer.askImage("https://img1.baidu.com/it/u=3052116511,4003568839&fm=253&fmt=auto&app=120&f=JPEG?w=500&h=889",
                "她正在热舞");
        System.out.println(ask);
        TimerUtil.printf();
    }

    /**
     * Test get.
     */
    @Test
    public void testGet(){
        TimerUtil.printf();
        VideoSynthesisOutput ask = videoSynthesisServer.get("8b39c679-57eb-4bf1-9617-0e245efe7921");
        System.out.println(ask);
        TimerUtil.printf();
    }

    /**
     * Test page.
     */
    @Test
    public void testPage(){
        TimerUtil.printf();
        VideoSynthesisListResult page = videoSynthesisServer.page();
        System.out.println(JsonUtils.toJson(page));
        TimerUtil.printf();
    }

}
