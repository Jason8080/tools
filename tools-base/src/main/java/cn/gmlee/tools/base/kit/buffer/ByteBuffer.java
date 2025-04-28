package cn.gmlee.tools.base.kit.buffer;

import cn.gmlee.tools.base.util.ByteUtil;
import cn.gmlee.tools.base.util.ExceptionUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 缓冲区操作对象.
 */
@Slf4j
public class ByteBuffer {
    /**
     * The Buffer.
     */
    protected volatile java.nio.ByteBuffer buffer;

    /**
     * Instantiates a new Byte buffer.
     *
     * @param capacity the capacity
     */
    public ByteBuffer(int capacity) {
        this.buffer = java.nio.ByteBuffer.allocate(capacity);
    }

    /**
     * 自旋缓冲区操作对象.
     * <p>
     * 1. 读写线程安全
     * 2. 支持多读多写
     * 3. 但是需要外部自主切换读写模式,否则可能出现数据丢失。
     * </p>
     */
    public static class Spin extends ByteBuffer {
        /**
         * The Wait.
         */
        protected final int wait = 10;

        /**
         * Instantiates a new Byte buffer.
         *
         * @param capacity the capacity
         */
        public Spin(int capacity) {
            super(capacity);
        }

        /**
         * 切换写模式
         */
        public synchronized void clear() {
            if (buffer == null) {
                return;
            }
            log.debug("byte buffer clear...");
            buffer.clear();
        }

        /**
         * 切换读模式.
         */
        public synchronized void flip() {
            if (buffer == null) {
                return;
            }
            log.debug("byte buffer flip...");
            buffer.flip();
        }

        /**
         * 写入数据(自旋等待).
         *
         * @param bytes the bytes
         */
        @Override
        public void write(byte... bytes) {
            synchronized (this) {
                log.debug("byte buffer write start...");
                if (buffer == null) {
                    return;
                }
                if (bytes == null || bytes.length < 1) {
                    return;
                }
                while (buffer != null && buffer.remaining() < bytes.length && !Thread.currentThread().isInterrupted()) {
                    // 限速: 录音速率有限，防止cpu占用过高，休眠一小会儿
                    ExceptionUtil.suppress(() -> super.wait(wait));
                }
                buffer.put(bytes);
                log.debug("byte buffer write end...");
            }
        }

        /**
         * 读取数据(自旋等待).
         *
         * @return byte[] empty array
         */
        @Override
        public byte[] read(int length) {
            synchronized (this) {
                log.debug("byte buffer read start...");
                while (buffer != null && buffer.position() <= 0 && !Thread.currentThread().isInterrupted()) {
                    // 限速: 录音速率有限，防止cpu占用过高，休眠一小会儿
                    ExceptionUtil.suppress(() -> super.wait(wait));
                }
                if (buffer == null || !buffer.hasRemaining()) {
                    return ByteUtil.empty;
                }
                if (length == -1) {
                    length = buffer.remaining();
                }
                byte[] bytes = new byte[buffer.remaining() < length ? buffer.remaining() : length];
                buffer.get(bytes);
                log.debug("byte buffer read end...");
                return bytes;
            }
        }


        /**
         * 关闭缓冲区.
         */
        @Override
        public synchronized void close() {
            super.close();
            super.notifyAll();
        }
    }

    /**
     * Has remaining boolean.
     *
     * @return the boolean
     */
    public boolean hasRemaining() {
        return this.buffer.hasRemaining();
    }


    /**
     * Write.
     *
     * @param bytes the bytes
     */
    public synchronized void write(byte... bytes) {
        buffer.clear(); // 切换写模式
        this.buffer.put(bytes);
    }

    /**
     * Read byte [ ].
     *
     * @param length the length
     * @return the byte [ ]
     */
    public synchronized byte[] read(int length) {
        this.buffer.flip(); // 切换读模式
        byte[] bytes = new byte[length < 0 ? this.buffer.remaining() : length];
        this.buffer.get(bytes);
        return bytes;
    }

    /**
     * Close.
     */
    public synchronized void close() {
        buffer.clear();
        buffer = null;
    }
}
