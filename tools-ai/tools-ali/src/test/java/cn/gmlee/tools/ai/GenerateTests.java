package cn.gmlee.tools.ai;

import cn.gmlee.tools.ai.server.stream.GenerateServer;
import cn.gmlee.tools.base.mod.Kv;
import cn.gmlee.tools.base.util.TimerUtil;
import io.reactivex.Flowable;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = App.class)
public class GenerateTests {

    @Autowired
    private GenerateServer generateServer;

    @Test
    public void testAskThinking(){
        TimerUtil.printf();
        Flowable<Kv<String,String>> ask = generateServer.askThinking(null, "我是谁?");
        // 思考
        ask.blockingForEach(kv -> System.out.println(kv.getDesc()));
        System.out.println("-----------------------------------------------");
        // 回答
        ask.blockingForEach(kv -> System.out.println(kv.getVal()));
        TimerUtil.printf();
    }

}
