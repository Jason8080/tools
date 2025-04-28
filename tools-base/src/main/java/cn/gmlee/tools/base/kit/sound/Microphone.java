package cn.gmlee.tools.base.kit.sound;

import cn.gmlee.tools.base.kit.buffer.ByteBuffer;
import cn.gmlee.tools.base.util.AssertUtil;
import io.reactivex.Emitter;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;

/**
 * The type Microphone.
 */
@Slf4j
public class Microphone extends Thread implements Serializable {

    private final int capacity = 1024;

    private Emitter<java.nio.ByteBuffer> emitter;

    private volatile ByteBuffer.Spin buffer;

    /**
     * Instantiates a new Microphone.
     */
    public Microphone() {
        this(null);
    }

    /**
     * Instantiates a new Microphone.
     *
     * @param emitter the emitter
     */
    public Microphone(Emitter<java.nio.ByteBuffer> emitter) {
        this.emitter = emitter;
        this.buffer = new ByteBuffer.Spin(capacity);
    }

    @Override
    public void run() {
        try {
            log.debug("microphone start...");
            while (buffer != null && !super.isInterrupted()) {
                if(!buffer.hasRemaining()){
                    continue;
                }
                buffer.flip();
                byte[] read = buffer.read(-1);
                buffer.clear();
                // 发送: 将录音音频数据发送给流式识别服务
                if (emitter != null && read.length > 0) {
                    log.debug("microphone send...");
                    emitter.onNext(java.nio.ByteBuffer.wrap(read));
                }
            }
            log.debug("microphone stop...");
            emitter.onComplete();
        } catch (Exception e) {
            log.debug("microphone error...", e);
            emitter.onError(e);
        } finally {
            emitter = null;
        }
    }

    /**
     * Start.
     *
     * @param emitter the emitter
     */
    public void start(Emitter<java.nio.ByteBuffer> emitter) {
        this.emitter = emitter;
        this.start();
    }

    @Override
    public synchronized void start() {
        AssertUtil.notNull(emitter, "Microphone emitter is not exist");
        super.start();
    }


    /**
     * Write.
     *
     * @param bytes the bytes
     */
    public void write(byte... bytes) {
        buffer.write(bytes);
    }

    /**
     * Exit.
     */
    public synchronized void exit() {
        buffer.close();
        buffer = null;
    }
}
