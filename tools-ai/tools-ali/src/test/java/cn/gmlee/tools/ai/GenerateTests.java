package cn.gmlee.tools.ai;

import cn.gmlee.tools.ai.mod.Ask;
import cn.gmlee.tools.ai.server.stream.GenerateServer;
import cn.gmlee.tools.base.util.NullUtil;
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
    public void testAsk(){
        TimerUtil.print();
        Flowable<Ask> ask = generateServer.ask(null, "我是谁?");
        ask.doOnNext(a -> System.out.println("think..".concat(a.getThink())))
                .doOnNext(a -> System.out.println("reply::".concat(a.getReply())))
                .blockingSubscribe();
        TimerUtil.print();
    }

}
