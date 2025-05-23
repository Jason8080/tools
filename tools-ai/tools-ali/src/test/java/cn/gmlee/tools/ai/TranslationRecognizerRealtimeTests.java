package cn.gmlee.tools.ai;

import cn.gmlee.tools.ai.server.stream.TranslationRecognizerRealtimeServer;
import cn.gmlee.tools.base.kit.sound.Microphone;
import cn.gmlee.tools.base.mod.Kv;
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
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = App.class)
public class TranslationRecognizerRealtimeTests {

    @Autowired
    private TranslationRecognizerRealtimeServer translationRecognizerRealtimeServer;

    @Test
    public void testAudio() throws Exception {
        TimerUtil.print();
        Microphone microphone = new Microphone();
        Flowable<Kv<String, Map<String, String>>> ask = translationRecognizerRealtimeServer.ask(microphone, "en");
        recoding(microphone);
        ask.blockingForEach(x -> {
            System.out.println(x);
            System.out.println(x.getKey());
            System.out.println(x.getVal());
        });
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
