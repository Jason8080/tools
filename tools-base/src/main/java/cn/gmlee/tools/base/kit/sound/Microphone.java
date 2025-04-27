package cn.gmlee.tools.base.kit.sound;

import cn.gmlee.tools.base.util.AssertUtil;
import io.reactivex.Emitter;
import io.reactivex.FlowableEmitter;
import lombok.extern.slf4j.Slf4j;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.TargetDataLine;
import java.io.Serializable;
import java.nio.ByteBuffer;

/**
 * The type Microphone.
 */
@Slf4j
public class Microphone extends Thread implements Serializable {

    private long millis;

    private Emitter<ByteBuffer> emitter;

    /**
     * Instantiates a new Microphone.
     */
    public Microphone() {
        this(null, Long.MAX_VALUE);
    }


    /**
     * Instantiates a new Microphone.
     *
     * @param millis the millis
     */
    public Microphone(long millis) {
        this(null, millis);
    }

    /**
     * Instantiates a new Microphone.
     *
     * @param emitter the emitter
     */
    public Microphone(Emitter<ByteBuffer> emitter) {
        this(emitter, 3000);
    }

    /**
     * Instantiates a new Microphone.
     *
     * @param emitter the emitter
     * @param millis  the millis
     */
    public Microphone(Emitter<ByteBuffer> emitter, long millis) {
        this.emitter = emitter;
        this.millis = System.currentTimeMillis() + millis;
    }

    @Override
    public void run() {
        try {
            // 创建音频
            AudioFormat audioFormat = new AudioFormat(16000, 16, 1, true, false);
            // 根据格式匹配默认录音设备
            TargetDataLine targetDataLine = AudioSystem.getTargetDataLine(audioFormat);
            targetDataLine.open(audioFormat);
            // 开始录音
            targetDataLine.start();
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            // 实时转写
            log.debug("microphone start...");
            while (millis > System.currentTimeMillis()) {
                int read = targetDataLine.read(buffer.array(), 0, buffer.capacity());
                if (read > 0) {
                    buffer.limit(read);
                    // 将录音音频数据发送给流式识别服务
                    emitter.onNext(buffer);
                    buffer = ByteBuffer.allocate(1024);
                    // 录音速率有限，防止cpu占用过高，休眠一小会儿
                    Thread.sleep(5);
                }
            }
            // 通知结束转写
            log.debug("microphone stop...");
            emitter.onComplete();
        } catch (Exception e) {
            log.debug("microphone error...");
            emitter.onError(e);
        }
    }

    /**
     * Start.
     *
     * @param emitter the emitter
     */
    public void start(FlowableEmitter<ByteBuffer> emitter) {
        this.emitter = emitter;
        this.start();
    }

    @Override
    public synchronized void start() {
        AssertUtil.notNull(emitter, "Microphone emitter is not exist");
        super.start();
    }


}
