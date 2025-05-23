package cn.gmlee.tools.ai;

import cn.gmlee.tools.ai.server.stream.RecognitionServer;
import cn.gmlee.tools.base.kit.sound.Microphone;
import cn.gmlee.tools.base.util.TimerUtil;
import io.reactivex.Flowable;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.TargetDataLine;
import java.util.Arrays;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = App.class)
public class RecognitionTests {

    @Autowired
    private RecognitionServer recognitionServer;

    @Test
    public void testAudio() throws Exception {
        TimerUtil.print();
        Microphone microphone = new Microphone();
        Flowable<String> ask = recognitionServer.ask(microphone);
        recoding(microphone);
        StringBuilder sb = new StringBuilder();
        ask.blockingForEach(x -> sb.append(x));
        System.out.println(sb);
        TimerUtil.print();
    }

    private void recoding(Microphone microphone) throws Exception {
        long millis = System.currentTimeMillis() + 5000;
        // 创建音频
        AudioFormat audioFormat = new AudioFormat(16000, 16, 1, true, false);
        // 根据格式匹配默认录音设备
        TargetDataLine targetDataLine = AudioSystem.getTargetDataLine(audioFormat);
        targetDataLine.open(audioFormat);
        // 开始录音
        targetDataLine.start();
        while (millis > System.currentTimeMillis()) {
            byte[] bytes = new byte[1024];
            int read = targetDataLine.read(bytes, 0, bytes.length);
            if(read > 0){
                microphone.write(Arrays.copyOfRange(bytes, 0, read));
            }
        }
        microphone.exit();
    }
}
