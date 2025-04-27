package cn.gmlee.tools.base.kit.sound;

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

    private final int capacity = 1024;

    private Emitter<ByteBuffer> emitter;

    private volatile ByteBuffer buffer;

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
    public Microphone(Emitter<ByteBuffer> emitter) {
        this.emitter = emitter;
        this.buffer = ByteBuffer.allocate(capacity);
    }

    @Override
    public void run() {
        try {
            log.debug("microphone start...");
            while (buffer != null && !super.isInterrupted()) {
                // 发送: 将录音音频数据发送给流式识别服务
                emitter.onNext(read());
            }
            log.debug("microphone stop...");
            emitter.onComplete();
        } catch (Exception e) {
            log.debug("microphone error...");
            emitter.onError(e);
        }
    }

    /**
     * 写入数据
     *
     * @param bytes the bytes
     */
    public synchronized void write(byte... bytes) {
        if (buffer == null) {
            return;
        }
        if (bytes == null || bytes.length < 1) {
            return;
        }
        while (buffer.remaining() < bytes.length && !Thread.currentThread().isInterrupted()) {
            // 限速: 录音速率有限，防止cpu占用过高，休眠一小会儿
            ExceptionUtil.suppress(() -> super.wait(10));
        }
        buffer.put(bytes);
    }

    /**
     * 读取数据
     *
     * @return the byte buffer
     */
    private synchronized ByteBuffer read() {
        while (buffer.position() <= 0 && !Thread.currentThread().isInterrupted()) {
            // 限速: 录音速率有限，防止cpu占用过高，休眠一小会儿
            ExceptionUtil.suppress(() -> super.wait(10));
        }
        buffer.flip(); // 切换读模式
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        buffer.clear(); // 切换写模式
        return ByteBuffer.wrap(bytes);
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
     * Exit.
     */
    public synchronized void exit() {
        if (buffer != null) {
            buffer.clear();
            buffer = null;
        }
        // 防止内存泄漏
        this.emitter = null;
        // 唤醒等待线程
        notifyAll();
    }
}
