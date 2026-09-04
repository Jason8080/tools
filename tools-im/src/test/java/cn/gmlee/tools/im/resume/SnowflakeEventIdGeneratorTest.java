package cn.gmlee.tools.im.resume;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link SnowflakeEventIdGenerator} 雪花算法事件 ID 生成器测试.
 * <p>
 * 覆盖：参数校验、单线程单调性、多线程唯一性、workerId 位段隔离。
 * </p>
 *
 * @since 5.7.0
 */
@DisplayName("SnowflakeEventIdGenerator 雪花算法")
class SnowflakeEventIdGeneratorTest {

    private static final int WORKER_BITS = 10;
    private static final int SEQ_BITS = 12;
    private static final long WORKER_MASK = (1L << WORKER_BITS) - 1;

    @Test
    @DisplayName("workerId 超出 [0, 1023] 时拒绝创建")
    void workerIdBoundsValidation() {
        assertThrows(IllegalArgumentException.class, () -> new SnowflakeEventIdGenerator(-1));
        assertThrows(IllegalArgumentException.class, () -> new SnowflakeEventIdGenerator(1024));
        new SnowflakeEventIdGenerator(0);
        new SnowflakeEventIdGenerator(1023);
    }

    @Test
    @DisplayName("单线程严格单调递增")
    void singleThreadMonotonic() {
        SnowflakeEventIdGenerator generator = new SnowflakeEventIdGenerator(1);
        long prev = Long.MIN_VALUE;
        for (int i = 0; i < 100_000; i++) {
            long id = generator.nextLong();
            assertTrue(id > prev, "第 " + i + " 个 ID 未严格递增: " + id + " <= " + prev);
            prev = id;
        }
    }

    @Test
    @DisplayName("序列号耗尽时逻辑时钟前进，仍保持单调")
    void sequenceExhaustionKeepsMonotonic() {
        SnowflakeEventIdGenerator generator = new SnowflakeEventIdGenerator(1);
        // 快速生成远超单毫秒 4096 序列号容量，迫使逻辑时钟前进
        long prev = Long.MIN_VALUE;
        for (int i = 0; i < 50_000; i++) {
            long id = generator.nextLong();
            assertTrue(id > prev);
            prev = id;
        }
    }

    @Test
    @DisplayName("多线程并发生成全局唯一")
    void multiThreadUniqueness() throws InterruptedException {
        int threads = 8;
        int perThread = 25_000;
        SnowflakeEventIdGenerator generator = new SnowflakeEventIdGenerator(7);
        Set<Long> all = java.util.Collections.newSetFromMap(new java.util.concurrent.ConcurrentHashMap<>());
        AtomicBoolean duplicate = new AtomicBoolean();
        CountDownLatch ready = new CountDownLatch(threads);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threads);
        ExecutorService pool = Executors.newFixedThreadPool(threads);

        try {
            for (int t = 0; t < threads; t++) {
                pool.submit(() -> {
                    ready.countDown();
                    try {
                        start.await();
                        for (int i = 0; i < perThread; i++) {
                            if (!all.add(generator.nextLong())) {
                                duplicate.set(true);
                            }
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        done.countDown();
                    }
                });
            }
            assertTrue(ready.await(5, TimeUnit.SECONDS));
            start.countDown();
            assertTrue(done.await(30, TimeUnit.SECONDS));
        } finally {
            pool.shutdownNow();
        }

        assertFalse(duplicate.get(), "并发场景出现重复雪花 ID");
        assertEquals(threads * perThread, all.size());
    }

    @Test
    @DisplayName("workerId 正确编码在 ID 位段中")
    void workerIdEncodedInBitField() {
        for (long workerId : new long[]{0, 1, 512, 1023}) {
            SnowflakeEventIdGenerator generator = new SnowflakeEventIdGenerator(workerId);
            for (int i = 0; i < 100; i++) {
                long id = generator.nextLong();
                assertEquals(workerId, (id >>> SEQ_BITS) & WORKER_MASK,
                        "workerId 位段解码不一致: workerId=" + workerId + ", id=" + id);
            }
        }
    }

    @Test
    @DisplayName("不同 workerId 实例生成互不冲突的 ID")
    void distinctWorkersProduceDisjointIds() {
        SnowflakeEventIdGenerator workerA = new SnowflakeEventIdGenerator(1);
        SnowflakeEventIdGenerator workerB = new SnowflakeEventIdGenerator(2);
        int count = 5_000;
        Set<Long> idsA = new HashSet<>(count * 2);
        Set<Long> idsB = new HashSet<>(count * 2);
        for (int i = 0; i < count; i++) {
            idsA.add(workerA.nextLong());
            idsB.add(workerB.nextLong());
        }
        assertEquals(count, idsA.size());
        assertEquals(count, idsB.size());

        List<Long> intersection = new ArrayList<>(idsA);
        intersection.retainAll(idsB);
        assertTrue(intersection.isEmpty(), "不同 workerId 出现冲突: " + intersection.size());
    }

    @Test
    @DisplayName("nextId 返回 Long 类型（与默认编解码器匹配）")
    void nextIdReturnsLong() {
        SnowflakeEventIdGenerator generator = new SnowflakeEventIdGenerator(3);
        Serializable id = generator.nextId("im.chat");
        assertTrue(id instanceof Long);
    }
}
