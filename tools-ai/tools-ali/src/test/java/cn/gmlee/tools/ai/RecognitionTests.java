package cn.gmlee.tools.ai;

import cn.gmlee.tools.ai.server.impl.RecordingServer;
import cn.gmlee.tools.base.kit.sound.Microphone;
import cn.gmlee.tools.base.util.TimerUtil;
import io.reactivex.Flowable;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = App.class)
public class RecognitionTests {

    @Autowired
    private RecordingServer recordingServer;

    @Test
    public void testAudio(){
        TimerUtil.print();
        Microphone microphone = new Microphone(5000);
        Flowable<String> ask = recordingServer.ask(microphone);
        StringBuilder sb = new StringBuilder();
        ask.blockingForEach(x -> sb.append(x));
        System.out.println(sb);
        TimerUtil.print();
    }
}
