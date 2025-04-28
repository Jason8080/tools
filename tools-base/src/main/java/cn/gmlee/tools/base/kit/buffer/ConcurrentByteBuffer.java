package cn.gmlee.tools.base.kit.buffer;

import cn.gmlee.tools.base.util.ByteUtil;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 基于读写锁的高效缓冲区
 */
@Slf4j
public class ConcurrentByteBuffer {
    private final ByteBuffer buffer;
    private volatile boolean closed = false;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final Condition notEmpty = lock.writeLock().newCondition();
    private final Condition notFull = lock.writeLock().newCondition();

    public ConcurrentByteBuffer(int capacity) {
        this.buffer = ByteBuffer.allocate(capacity);
    }

    /**
     * 写入数据
     */
    public void write(byte[] bytes) throws InterruptedException {
        if (bytes == null || bytes.length == 0 || closed) {
            return;
        }

        lock.writeLock().lock();
        try {
            // 等待缓冲区有足够空间
            log.debug("ConcurrentByteBuffer write start...");
            while (buffer.remaining() < bytes.length && !closed) {
                log.info("write await...");
                notFull.await();
            }

            if (closed) {
                return;
            }

            buffer.put(bytes);

            // 通知读线程有数据可读
            notEmpty.signalAll();
        } finally {
            lock.writeLock().unlock();
            log.debug("ConcurrentByteBuffer write end...");
        }
    }

    /**
     * 读取数据
     */
    public byte[] read(int length) throws InterruptedException {
        if (closed) {
            return ByteUtil.empty;
        }

        // 整个读取操作使用写锁
        lock.writeLock().lock();
        try {
            // 等待有数据可读
            log.debug("ConcurrentByteBuffer read start...");
            while (buffer.position() == 0 && !closed) {
                log.info("read await...");
                notEmpty.await();
            }

            if (closed || buffer.position() == 0) {
                return ByteUtil.empty;
            }

            buffer.flip();
            try {
                int readLength = length == -1 ? buffer.remaining() : Math.min(length, buffer.remaining());
                byte[] result = new byte[readLength];
                buffer.get(result);
                return result;
            } finally {
                buffer.compact();
                notFull.signalAll();
            }
        } finally {
            lock.writeLock().unlock();
            log.debug("ConcurrentByteBuffer read end...");
        }
    }

    /**
     * 关闭缓冲区
     */
    public void close() {
        lock.writeLock().lock();
        try {
            closed = true;
            notFull.signalAll();
            notEmpty.signalAll();
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * 检查缓冲区是否关闭
     */
    public boolean isClosed() {
        return closed;
    }
}