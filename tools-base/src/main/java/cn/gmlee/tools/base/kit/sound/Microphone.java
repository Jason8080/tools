package cn.gmlee.tools.base.kit.sound;

import cn.gmlee.tools.base.kit.buffer.ConcurrentByteBuffer;
import cn.gmlee.tools.base.util.AssertUtil;
import cn.gmlee.tools.base.util.ExceptionUtil;
import io.reactivex.Emitter;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.nio.ByteBuffer;

/**
 * The type Microphone.
 */
@Slf4j
public class Microphone extends Thread implements Serializable {

    private Emitter<ByteBuffer> emitter;

    private volatile ConcurrentByteBuffer buffer;

    /**
     * Instantiates a new Microphone.
     */
    public Microphone() {
        this(null, 1024);
    }

    /**
     * Instantiates a new Microphone.
     *
     * @param capacity the capacity
     */
    public Microphone(int capacity) {
        this(null, capacity);
    }

    /**
     * Instantiates a new Microphone.
     *
     * @param emitter the emitter
     */
    public Microphone(Emitter<ByteBuffer> emitter) {
        this(emitter, 1024);
    }

    /**
     * Instantiates a new Microphone.
     *
     * @param emitter  the emitter
     * @param capacity the capacity
     */
    public Microphone(Emitter<ByteBuffer> emitter, int capacity) {
        this.emitter = emitter;
        this.buffer = new ConcurrentByteBuffer(capacity);
    }

    @Override
    public void run() {
        try {
            log.info("Microphone start...");
            while (!super.isInterrupted() && !buffer.isClosed()) {
                byte[] read = this.read(-1);
                // 发送: 将录音音频数据发送给流式识别服务
                if (emitter != null && read.length > 0) {
                    log.debug("Microphone send...");
                    emitter.onNext(ByteBuffer.wrap(read));
                }
            }
            log.info("Microphone stop...");
            emitter.onComplete();
        } catch (Exception e) {
            log.error("Microphone error...", e);
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
    public void start(Emitter<ByteBuffer> emitter) {
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
        ExceptionUtil.suppress(() -> buffer.write(bytes));
    }


    /**
     * Read byte [ ].
     *
     * @param length the length
     * @return the byte [ ]
     */
    public byte[] read(int length) {
        return ExceptionUtil.suppress(() -> buffer.read(length));
    }

    /**
     * Exit.
     */
    public synchronized void exit() {
        buffer.close();
    }
}
