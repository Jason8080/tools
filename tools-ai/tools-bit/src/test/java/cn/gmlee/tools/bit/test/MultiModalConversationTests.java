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
}
