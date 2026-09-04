package cn.gmlee.tools.im.resume;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 事件 ID 基础件测试：{@link DefaultEventIdCodec}（编解码）、
 * {@link DefaultEventIdComparator}（顺序比较）、
 * {@link AtomicSequenceEventIdGenerator}（单 JVM 自增）.
 *
 * @since 5.7.0
 */
@DisplayName("事件 ID 编解码器 / 比较器 / 自增生成器")
class EventIdPrimitivesTest {

    @Nested
    @DisplayName("DefaultEventIdCodec 编解码")
    class Codec {

        @Test
        @DisplayName("数字往返一致：Long → 字符串 → Long")
        void numericRoundTrip() {
            assertEquals("123", EventIdCodec.DEFAULT.encode(123L));
            assertEquals(123L, EventIdCodec.DEFAULT.decode("123"));
            assertEquals(-5L, EventIdCodec.DEFAULT.decode("-5"));
            assertEquals(42L, EventIdCodec.DEFAULT.decode(" 42 "), "解码前应去除首尾空白");
        }

        @Test
        @DisplayName("null / 空白输入解码为 null")
        void nullAndBlankDecodeToNull() {
            assertNull(EventIdCodec.DEFAULT.encode(null));
            assertNull(EventIdCodec.DEFAULT.decode(null));
            assertNull(EventIdCodec.DEFAULT.decode(""));
            assertNull(EventIdCodec.DEFAULT.decode("   "));
        }

        @Test
        @DisplayName("非数字保留为字符串")
        void nonNumericKeptAsString() {
            assertEquals("abc-123", EventIdCodec.DEFAULT.decode("abc-123"));
            assertEquals("-", EventIdCodec.DEFAULT.decode("-"));
        }

        @Test
        @DisplayName("超出 Long 范围的数字按字符串处理")
        void overflowNumberKeptAsString() {
            String huge = "99999999999999999999999";
            assertEquals(huge, EventIdCodec.DEFAULT.decode(huge));
        }

        @Test
        @DisplayName("编码后再解码：语义等价")
        void encodeDecodeEquivalence() {
            for (Serializable id : new Serializable[]{0L, 1L, Long.MAX_VALUE, Long.MIN_VALUE}) {
                Serializable decoded = EventIdCodec.DEFAULT.decode(EventIdCodec.DEFAULT.encode(id));
                assertEquals(id, decoded);
            }
        }
    }

    @Nested
    @DisplayName("DefaultEventIdComparator 顺序比较")
    class ComparatorRules {

        @Test
        @DisplayName("水位线为 null：任何候选都通过")
        void nullWatermarkPasses() {
            assertTrue(EventIdComparator.DEFAULT.isAfter(1L, null));
            assertTrue(EventIdComparator.DEFAULT.isAfter(null, null));
        }

        @Test
        @DisplayName("候选为 null：通过（宁重勿漏，无 ID 消息不被误丢）")
        void nullCandidatePasses() {
            assertTrue(EventIdComparator.DEFAULT.isAfter(null, 5L));
        }

        @Test
        @DisplayName("数字按 longValue 比较（支持跨数字类型）")
        void numbersComparedByLongValue() {
            assertTrue(EventIdComparator.DEFAULT.isAfter(5L, 3L));
            assertFalse(EventIdComparator.DEFAULT.isAfter(3L, 5L));
            assertFalse(EventIdComparator.DEFAULT.isAfter(5L, 5L));
            assertTrue(EventIdComparator.DEFAULT.isAfter(10L, 9), "Long 与 Integer 可比较");
            assertFalse(EventIdComparator.DEFAULT.isAfter(9, 10L));
        }

        @Test
        @DisplayName("同类型 Comparable 按 compareTo 比较")
        void sameTypeComparable() {
            assertTrue(EventIdComparator.DEFAULT.isAfter("b", "a"));
            assertFalse(EventIdComparator.DEFAULT.isAfter("a", "b"));
            assertFalse(EventIdComparator.DEFAULT.isAfter("a", "a"));
        }

        @Test
        @DisplayName("类型不一致 / 不可比较：通过（宁重勿漏）")
        void incomparablePasses() {
            assertTrue(EventIdComparator.DEFAULT.isAfter(1L, "a"));
            assertTrue(EventIdComparator.DEFAULT.isAfter("a", 1L));
        }
    }

    @Nested
    @DisplayName("AtomicSequenceEventIdGenerator 单 JVM 自增")
    class AtomicSequence {

        @Test
        @DisplayName("单线程严格单调递增")
        void monotonic() {
            AtomicSequenceEventIdGenerator generator = new AtomicSequenceEventIdGenerator();
            long prev = (Long) generator.nextId("t");
            for (int i = 0; i < 10_000; i++) {
                long id = (Long) generator.nextId("t");
                assertTrue(id > prev);
                prev = id;
            }
        }

        @Test
        @DisplayName("多线程生成全局唯一")
        void multiThreadUniqueness() throws InterruptedException {
            int threads = 4;
            int perThread = 10_000;
            AtomicSequenceEventIdGenerator generator = new AtomicSequenceEventIdGenerator();
            Set<Long> all = Collections.newSetFromMap(new java.util.concurrent.ConcurrentHashMap<>());
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
                                all.add((Long) generator.nextId("t"));
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

            assertEquals(threads * perThread, all.size(), "自增序列并发下不应重复");
        }
    }

    @Nested
    @DisplayName("雪花算法与自增序列的协同")
    class IdCompatibility {

        @Test
        @DisplayName("雪花 ID 经默认编解码器往返后仍可正确比较")
        void snowflakeIdSurvivesCodecRoundTrip() {
            SnowflakeEventIdGenerator generator = new SnowflakeEventIdGenerator(1);
            long id = generator.nextLong();
            Serializable decoded = EventIdCodec.DEFAULT.decode(EventIdCodec.DEFAULT.encode(id));
            assertTrue(EventIdComparator.DEFAULT.isAfter(generator.nextLong(), decoded));
            assertFalse(EventIdComparator.DEFAULT.isAfter(decoded, id));
        }
    }
}
