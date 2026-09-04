package cn.gmlee.tools.im.resume;

import cn.gmlee.tools.im.conf.SseProperties;
import cn.gmlee.tools.im.model.ConnectionMetadata;
import cn.gmlee.tools.im.model.Msg;
import cn.gmlee.tools.im.model.ResumeSignal;
import cn.gmlee.tools.im.model.TopicMessage;
import cn.gmlee.tools.im.sse.metrics.SseMetrics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.io.Serializable;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link ResumeSupport} 续传引擎专项测试.
 * <p>
 * 覆盖：门控降级、零间隙续接（回放 + 待缓冲排空 + 水位线去重）、
 * resync 降级路径（间隙/抛错/超时/超限/溢出）与资源释放（下游取消级联）。
 * </p>
 *
 * @since 5.7.0
 */
@DisplayName("ResumeSupport 续传引擎")
class ResumeSupportTest {

    private static final String TOPIC = "im.chat";

    private SseProperties.ResumeConfig config;
    private RecordingMetrics metrics;
    private ScriptedStore store;
    private ResumeSupport support;

    @BeforeEach
    void setUp() {
        config = new SseProperties.ResumeConfig();
        metrics = new RecordingMetrics();
        store = new ScriptedStore();
        support = new ResumeSupport(config, EventIdCodec.DEFAULT, EventIdComparator.DEFAULT,
                List.of(store), metrics);
    }

    // ==================== 门控（不满足条件时原样返回） ====================

    @Nested
    @DisplayName("门控降级 - 原样返回实时流")
    class Gating {

        @Test
        @DisplayName("功能关闭时不包装")
        void disabledReturnsLiveUnchanged() {
            config.setEnabled(false);
            Flux<TopicMessage<?, ?>> live = Flux.never();
            assertSame(live, support.wrapIfResuming(live, TOPIC, metadata("1")));
        }

        @Test
        @DisplayName("无 Last-Event-ID 时不包装")
        void noLastEventIdReturnsLiveUnchanged() {
            Flux<TopicMessage<?, ?>> live = Flux.never();
            assertSame(live, support.wrapIfResuming(live, TOPIC, metadata(null)));
            assertSame(live, support.wrapIfResuming(live, TOPIC, metadata("   ")));
            assertSame(live, support.wrapIfResuming(live, TOPIC, null));
        }

        @Test
        @DisplayName("无匹配存储时不包装")
        void noStoreReturnsLiveUnchanged() {
            Flux<TopicMessage<?, ?>> live = Flux.never();
            ResumeSupport noStore = new ResumeSupport(config, null, null, null, metrics);
            assertSame(live, noStore.wrapIfResuming(live, TOPIC, metadata("1")));

            store.supportsResult = false;
            assertSame(live, support.wrapIfResuming(live, TOPIC, metadata("1")));
        }

        @Test
        @DisplayName("解码失败时降级为仅实时流并记录指标")
        void decodeFailureDegrades() {
            EventIdCodec broken = new EventIdCodec() {
                @Override
                public String encode(Serializable id) {
                    return String.valueOf(id);
                }

                @Override
                public Serializable decode(String raw) {
                    throw new IllegalStateException("decode broken");
                }
            };
            ResumeSupport brokenSupport = new ResumeSupport(config, broken, null,
                    List.of(store), metrics);
            Flux<TopicMessage<?, ?>> live = Flux.never();
            assertSame(live, brokenSupport.wrapIfResuming(live, TOPIC, metadata("bad")));
            assertEquals(1, metrics.errors("resume_decode"));
        }

        @Test
        @DisplayName("解码结果为 null 时不包装")
        void decodeNullReturnsLiveUnchanged() {
            EventIdCodec nullCodec = new EventIdCodec() {
                @Override
                public String encode(Serializable id) {
                    return null;
                }

                @Override
                public Serializable decode(String raw) {
                    return null;
                }
            };
            ResumeSupport nullSupport = new ResumeSupport(config, nullCodec, null,
                    List.of(store), metrics);
            Flux<TopicMessage<?, ?>> live = Flux.never();
            assertSame(live, nullSupport.wrapIfResuming(live, TOPIC, metadata("1")));
        }
    }

    // ==================== 零间隙续接 ====================

    @Nested
    @DisplayName("零间隙续接 - 回放 + 待缓冲排空")
    class GaplessHandover {

        @Test
        @DisplayName("回放期间实时消息进入待缓冲，切换后按序排空")
        void pendingBufferDrainedAfterCutover() {
            AtomicReference<reactor.core.publisher.FluxSink<TopicMessage<?, ?>>> historySink =
                    new AtomicReference<>();
            store.loadFn = topic -> HistoryLoadResult.of(Flux.create(historySink::set));
            Sinks.Many<TopicMessage<?, ?>> live = Sinks.many().unicast().onBackpressureBuffer();

            Recording rec = record(support.wrapIfResuming(live.asFlux(), TOPIC, metadata("1")));
            assertTrue(store.loadInvoked, "会话启动后应立即加载历史");

            // 回放窗口内到达的实时消息 → 待缓冲
            live.emitNext(env(4L, "live-4"), Sinks.EmitFailureHandler.FAIL_FAST);
            assertEquals(0, rec.received.size(), "回放阶段实时消息不应直接下发");

            // 回放历史（水位线 1 → 2 → 3）
            historySink.get().next(env(2L, "his-2"));
            historySink.get().next(env(3L, "his-3"));
            assertEquals(List.of(2L, 3L), ids(rec));

            // 回放完成 → 原子切换 → 排空待缓冲
            historySink.get().complete();
            assertEquals(List.of(2L, 3L, 4L), ids(rec));

            // 切换后实时消息直接经水位线过滤下发
            live.emitNext(env(5L, "live-5"), Sinks.EmitFailureHandler.FAIL_FAST);
            assertEquals(List.of(2L, 3L, 4L, 5L), ids(rec));

            assertEquals(1, metrics.resumes(TOPIC, "success"));
            assertEquals(2, metrics.resumedMessages(TOPIC));
        }

        @Test
        @DisplayName("衔接处水位线去重：重复消息只下发一次")
        void watermarkDeduplicatesAtBoundary() {
            AtomicReference<reactor.core.publisher.FluxSink<TopicMessage<?, ?>>> historySink =
                    new AtomicReference<>();
            store.loadFn = topic -> HistoryLoadResult.of(Flux.create(historySink::set));
            Sinks.Many<TopicMessage<?, ?>> live = Sinks.many().unicast().onBackpressureBuffer();

            Recording rec = record(support.wrapIfResuming(live.asFlux(), TOPIC, metadata("1")));

            // 回放窗口内实时收到与历史重叠的消息（如 MQ 重投）
            live.emitNext(env(3L, "dup-3"), Sinks.EmitFailureHandler.FAIL_FAST);
            live.emitNext(env(4L, "live-4"), Sinks.EmitFailureHandler.FAIL_FAST);

            historySink.get().next(env(2L, "his-2"));
            historySink.get().next(env(3L, "his-3"));
            historySink.get().complete();

            // 3 在回放中已下发（水位线推进到 3），待缓冲中的重复 3 被丢弃
            assertEquals(List.of(2L, 3L, 4L), ids(rec), "边界重复消息应被水位线去重");
        }

        @Test
        @DisplayName("无 ID 消息透传且不推进水位线")
        void nullIdPassesThroughWithoutAdvancingWatermark() {
            store.loadFn = topic -> HistoryLoadResult.of(Flux.just(env(2L, "his-2")));
            Sinks.Many<TopicMessage<?, ?>> live = Sinks.many().unicast().onBackpressureBuffer();

            Recording rec = record(support.wrapIfResuming(live.asFlux(), TOPIC, metadata("1")));

            TopicMessage<Serializable, Msg> noId = env(null, "no-id");
            live.emitNext(noId, Sinks.EmitFailureHandler.FAIL_FAST);
            live.emitNext(env(3L, "live-3"), Sinks.EmitFailureHandler.FAIL_FAST);
            live.emitNext(env(2L, "dup-2"), Sinks.EmitFailureHandler.FAIL_FAST);

            // 回放完成（同步）→ cutover 排空：无 ID 透传，3 下发，重复 2 丢弃
            assertEquals(2L, rec.received.get(0).getId());
            assertNull(rec.received.get(1).getId());
            assertEquals(3L, rec.received.get(2).getId());
            assertEquals(3, rec.received.size());
        }

        @Test
        @DisplayName("实时流在回放期间完成：切换后补发 complete")
        void liveCompleteDuringReplayCompletesAfterCutover() throws InterruptedException {
            store.loadFn = topic -> HistoryLoadResult.of(Flux.just(env(2L, "his-2"), env(3L, "his-3")));
            Flux<TopicMessage<?, ?>> live = Flux.just(env(4L, "live-4"));

            Recording rec = record(support.wrapIfResuming(live, TOPIC, metadata("1")));

            assertTrue(rec.completed.await(2, TimeUnit.SECONDS), "cutover 后应补发 complete");
            assertNull(rec.error.get());
            assertEquals(List.of(2L, 3L, 4L), ids(rec));
        }

        @Test
        @DisplayName("实时流出错：错误传播并级联取消历史订阅")
        void liveErrorPropagatesAndCancelsHistory() throws InterruptedException {
            AtomicBoolean historyCancelled = new AtomicBoolean();
            store.loadFn = topic -> HistoryLoadResult.of(
                    Flux.<TopicMessage<?, ?>>never().doOnCancel(() -> historyCancelled.set(true)));
            Sinks.Many<TopicMessage<?, ?>> live = Sinks.many().unicast().onBackpressureBuffer();

            Recording rec = record(support.wrapIfResuming(live.asFlux(), TOPIC, metadata("1")));
            live.emitNext(env(2L, "live-2"), Sinks.EmitFailureHandler.FAIL_FAST);

            IllegalStateException boom = new IllegalStateException("live boom");
            live.emitError(boom, Sinks.EmitFailureHandler.FAIL_FAST);

            assertTrue(rec.completed.await(2, TimeUnit.SECONDS));
            assertSame(boom, rec.error.get());
            assertTrue(historyCancelled.get(), "下游错误应级联取消历史订阅");
        }
    }

    // ==================== resync 降级路径 ====================

    @Nested
    @DisplayName("resync 降级 - 间隙/失败/超限/溢出")
    class ResyncDegradation {

        @Test
        @DisplayName("历史已知间隙：先发 resync 再转实时")
        void gapKnownEmitsResyncThenLive() {
            store.loadFn = topic -> HistoryLoadResult.gap();
            Sinks.Many<TopicMessage<?, ?>> live = Sinks.many().unicast().onBackpressureBuffer();

            Recording rec = record(support.wrapIfResuming(live.asFlux(), TOPIC, metadata("1")));

            assertEquals(1, rec.received.size());
            assertResync(rec.received.get(0));

            live.emitNext(env(2L, "live-2"), Sinks.EmitFailureHandler.FAIL_FAST);
            assertEquals(2, rec.received.size());
            assertEquals(2L, rec.received.get(1).getId());

            assertEquals(1, metrics.resumes(TOPIC, "gap"));
        }

        @Test
        @DisplayName("loadSince 抛异常：降级实时流 + resync + failed 指标")
        void loadSinceThrowsDegradesWithResync() {
            store.loadFn = topic -> {
                throw new IllegalStateException("store broken");
            };
            Sinks.Many<TopicMessage<?, ?>> live = Sinks.many().unicast().onBackpressureBuffer();

            Recording rec = record(support.wrapIfResuming(live.asFlux(), TOPIC, metadata("1")));

            assertEquals(1, rec.received.size());
            assertResync(rec.received.get(0));

            live.emitNext(env(2L, "live-2"), Sinks.EmitFailureHandler.FAIL_FAST);
            assertEquals(2L, rec.received.get(1).getId());

            assertTrue(metrics.errors("resume_replay") >= 1);
            assertEquals(1, metrics.resumes(TOPIC, "failed"));
        }

        @Test
        @DisplayName("回放超时：降级实时流 + resync")
        void replayTimeoutDegradesWithResync() throws InterruptedException {
            config.setReplayTimeout(Duration.ofMillis(100));
            store.loadFn = topic -> HistoryLoadResult.of(Flux.never());
            Sinks.Many<TopicMessage<?, ?>> live = Sinks.many().unicast().onBackpressureBuffer();

            Recording rec = record(support.wrapIfResuming(live.asFlux(), TOPIC, metadata("1")));

            awaitSize(rec, 1, 3000);
            assertResync(rec.received.get(0));

            live.emitNext(env(2L, "live-2"), Sinks.EmitFailureHandler.FAIL_FAST);
            awaitSize(rec, 2, 2000);
            assertEquals(2L, rec.received.get(1).getId());
            assertEquals(1, metrics.resumes(TOPIC, "failed"));
        }

        @Test
        @DisplayName("回放总时长预算：缓慢持续到达（元素间隔超时未触发）也降级 + resync")
        void replayTotalBudgetDegradesWithResync() throws InterruptedException {
            config.setReplayTimeout(Duration.ofMillis(500));
            AtomicBoolean historyCancelled = new AtomicBoolean();
            // 每 20ms 一个元素（< 500ms 元素间隔超时）：间隔超时永不触发，
            // 但总预算 500ms 内无法回放完 1000 条 —— 验证会话级截止时间生效
            store.loadFn = topic -> {
                Flux<TopicMessage<?, ?>> history = Flux.interval(Duration.ofMillis(20))
                        .map(i -> env(i + 2, "his-" + i));
                return HistoryLoadResult.of(history
                        .take(1000)
                        .doOnCancel(() -> historyCancelled.set(true)));
            };
            Sinks.Many<TopicMessage<?, ?>> live = Sinks.many().unicast().onBackpressureBuffer();

            Recording rec = record(support.wrapIfResuming(live.asFlux(), TOPIC, metadata("1")));

            // 等待总预算耗尽 → resync 降级
            long deadline = System.currentTimeMillis() + 3000;
            while (countResync(rec) == 0 && System.currentTimeMillis() < deadline) {
                Thread.sleep(5);
            }
            assertEquals(1, countResync(rec), "总预算耗尽应发送 resync");
            int replayedBeforeCutover = idsOfNonResync(rec).size();
            assertTrue(replayedBeforeCutover > 0, "预算耗尽前应有部分回放消息下发");

            // 慢存储读取应被终止（级联取消历史订阅）
            awaitFlag(historyCancelled, "预算耗尽后历史订阅应被取消");

            // 切换后实时消息照常下发（id 远大于回放水位线）
            live.emitNext(env(5000L, "live-5000"), Sinks.EmitFailureHandler.FAIL_FAST);
            deadline = System.currentTimeMillis() + 2000;
            boolean liveReceived = false;
            while (System.currentTimeMillis() < deadline) {
                int n = rec.received.size();
                if (n > 0 && Long.valueOf(5000L).equals(rec.received.get(n - 1).getId())) {
                    liveReceived = true;
                    break;
                }
                Thread.sleep(5);
            }
            assertTrue(liveReceived, "切换后实时消息应照常下发");
            Thread.sleep(100);
            assertEquals(replayedBeforeCutover, idsOfNonResync(rec).size() - 1,
                    "预算耗尽后剩余历史不应继续下发（-1 为切换后的实时消息）");

            assertTrue(metrics.errors("resume_replay") >= 1);
            assertEquals(1, metrics.resumes(TOPIC, "failed"));
        }

        @Test
        @DisplayName("回放超限（> maxReplay）：停止回放 + resync")
        void replayOverflowEmitsResync() {
            config.setMaxReplay(2);
            store.loadFn = topic -> HistoryLoadResult.of(
                    Flux.just(env(2L, "h2"), env(3L, "h3"), env(4L, "h4")));
            Sinks.Many<TopicMessage<?, ?>> live = Sinks.many().unicast().onBackpressureBuffer();

            Recording rec = record(support.wrapIfResuming(live.asFlux(), TOPIC, metadata("1")));

            // 前 2 条正常回放，第 3 条触发超限 → resync → 切换实时
            assertEquals(List.of(2L, 3L), idsOfNonResync(rec));
            assertEquals(1, countResync(rec));

            live.emitNext(env(5L, "live-5"), Sinks.EmitFailureHandler.FAIL_FAST);
            assertEquals(5L, rec.received.get(rec.received.size() - 1).getId());
            assertEquals(1, metrics.resumes(TOPIC, "gap"));
        }

        @Test
        @DisplayName("待缓冲溢出（> maxPending）：resync + 丢弃积压")
        void pendingOverflowEmitsResyncAndClearsBacklog() {
            config.setMaxPending(2);
            AtomicReference<reactor.core.publisher.FluxSink<TopicMessage<?, ?>>> historySink =
                    new AtomicReference<>();
            store.loadFn = topic -> HistoryLoadResult.of(Flux.create(historySink::set));
            Sinks.Many<TopicMessage<?, ?>> live = Sinks.many().unicast().onBackpressureBuffer();

            Recording rec = record(support.wrapIfResuming(live.asFlux(), TOPIC, metadata("1")));

            live.emitNext(env(10L, "m10"), Sinks.EmitFailureHandler.FAIL_FAST);
            live.emitNext(env(11L, "m11"), Sinks.EmitFailureHandler.FAIL_FAST);
            assertEquals(0, countResync(rec));

            // 第 3 条触发溢出：resync + 清空积压，仅保留最新
            live.emitNext(env(12L, "m12"), Sinks.EmitFailureHandler.FAIL_FAST);
            assertEquals(1, countResync(rec));

            historySink.get().complete();
            // 排空仅含溢出后保留的 12（10/11 已随积压丢弃）
            assertEquals(List.of(12L), idsOfNonResync(rec));
            assertTrue(metrics.errors("resume_overflow") >= 1);
        }

        @Test
        @DisplayName("resync 至多发送一次（多次溢出幂等）")
        void resyncSentAtMostOnce() {
            config.setMaxPending(1);
            AtomicReference<reactor.core.publisher.FluxSink<TopicMessage<?, ?>>> historySink =
                    new AtomicReference<>();
            store.loadFn = topic -> HistoryLoadResult.of(Flux.create(historySink::set));
            Sinks.Many<TopicMessage<?, ?>> live = Sinks.many().unicast().onBackpressureBuffer();

            Recording rec = record(support.wrapIfResuming(live.asFlux(), TOPIC, metadata("1")));

            live.emitNext(env(10L, "m10"), Sinks.EmitFailureHandler.FAIL_FAST);
            live.emitNext(env(11L, "m11"), Sinks.EmitFailureHandler.FAIL_FAST);  // 溢出 1
            live.emitNext(env(12L, "m12"), Sinks.EmitFailureHandler.FAIL_FAST);  // 溢出 2

            assertEquals(1, countResync(rec), "resync 信号至多一次");
            historySink.get().complete();
            assertEquals(List.of(12L), idsOfNonResync(rec));
        }
    }

    // ==================== 资源安全 ====================

    @Nested
    @DisplayName("资源安全 - 级联释放与写入侧挂点")
    class ResourceSafety {

        @Test
        @DisplayName("下游取消级联释放实时流与历史流两个内部订阅")
        void downstreamCancelDisposesBothSubscriptions() throws InterruptedException {
            AtomicBoolean liveCancelled = new AtomicBoolean();
            AtomicBoolean historyCancelled = new AtomicBoolean();
            store.loadFn = topic -> HistoryLoadResult.of(
                    Flux.<TopicMessage<?, ?>>never().doOnCancel(() -> historyCancelled.set(true)));
            Flux<TopicMessage<?, ?>> live = Flux.<TopicMessage<?, ?>>never()
                    .doOnCancel(() -> liveCancelled.set(true));

            Recording rec = record(support.wrapIfResuming(live, TOPIC, metadata("1")));
            assertTrue(store.loadInvoked, "会话应已进入回放阶段");

            rec.subscription.dispose();

            awaitFlag(liveCancelled, "实时流订阅应被取消");
            awaitFlag(historyCancelled, "历史流订阅应被取消");
        }

        @Test
        @DisplayName("storeAsync：空消息/无 ID/无 Topic 直接跳过")
        void storeAsyncSkipsInvalidMessages() {
            support.storeAsync(null);
            support.storeAsync(env(null, "no-id"));
            TopicMessage<Serializable, Msg> noTopic = new TopicMessage<>();
            noTopic.setId(1L);
            noTopic.setTopic(null);
            support.storeAsync(noTopic);
            assertTrue(store.stored.isEmpty(), "非法消息不应触达存储");
        }

        @Test
        @DisplayName("storeAsync：store() 同步抛错不传播，仅记录指标")
        void storeAsyncSwallowsSyncErrors() {
            store.storeBehavior = msg -> {
                throw new IllegalStateException("store sync boom");
            };
            support.storeAsync(env(1L, "m1"));
            assertEquals(1, metrics.errors("history_store"));
        }

        @Test
        @DisplayName("storeAsync：store() 异步出错不传播，仅记录指标")
        void storeAsyncSwallowsAsyncErrors() {
            store.storeBehavior = msg -> Mono.error(new IllegalStateException("store async boom"));
            support.storeAsync(env(1L, "m1"));
            assertEquals(1, metrics.errors("history_store"));
        }
    }

    // ==================== 存储路由 ====================

    @Test
    @DisplayName("多存储按 Order 排序，首个 supports 命中者获胜")
    void findStoreRespectsOrderAndSupports() {
        ScriptedStore orderOne = new ScriptedStore();
        orderOne.order = 1;
        orderOne.supportedTopics = List.of("a");
        ScriptedStore orderZero = new ScriptedStore();
        orderZero.order = 0;
        orderZero.supportedTopics = List.of("b");
        ScriptedStore fallback = new ScriptedStore();
        fallback.order = 5;  // supports 全部

        ResumeSupport multi = new ResumeSupport(config, null, null,
                List.of(fallback, orderOne, orderZero), metrics);

        assertSame(orderOne, multi.findStore("a"), "order=0 不 supports(a)，应由 order=1 命中");
        assertSame(orderZero, multi.findStore("b"));
        assertSame(fallback, multi.findStore("c"), "兜底存储命中");
        assertNull(new ResumeSupport(config, null, null, null, metrics).findStore("a"));
    }

    // ==================== 测试基础设施 ====================

    /** 测试消息载荷 */
    private static final class TestMsg implements Msg {
        private final String text;

        TestMsg(String text) {
            this.text = text;
        }

        @Override
        public String toString() {
            return "TestMsg[" + text + "]";
        }
    }

    /** 可编程历史存储 */
    private static final class ScriptedStore implements MessageHistoryStore {
        volatile Function<String, HistoryLoadResult> loadFn = topic -> HistoryLoadResult.empty();
        volatile Function<TopicMessage<?, ?>, Mono<Void>> storeBehavior;
        volatile boolean supportsResult = true;
        volatile List<String> supportedTopics;
        volatile boolean loadInvoked;
        final List<TopicMessage<?, ?>> stored = new CopyOnWriteArrayList<>();
        int order = 0;

        @Override
        public boolean supports(String topic) {
            return supportedTopics != null ? supportedTopics.contains(topic) : supportsResult;
        }

        @Override
        public Mono<Void> store(TopicMessage<?, ?> message) {
            if (storeBehavior != null) {
                return storeBehavior.apply(message);
            }
            stored.add(message);
            return Mono.empty();
        }

        @Override
        public HistoryLoadResult loadSince(String topic, Serializable lastEventId) {
            loadInvoked = true;
            return loadFn.apply(topic);
        }

        @Override
        public int getOrder() {
            return order;
        }
    }

    /** 记录式指标收集器 */
    static final class RecordingMetrics implements SseMetrics {
        final Map<String, AtomicLong> errors = new ConcurrentHashMap<>();
        final Map<String, AtomicLong> resumes = new ConcurrentHashMap<>();
        final Map<String, AtomicLong> resumedMessages = new ConcurrentHashMap<>();

        long errors(String type) {
            AtomicLong c = errors.get(type);
            return c != null ? c.get() : 0;
        }

        long resumes(String topic, String result) {
            AtomicLong c = resumes.get(topic + ":" + result);
            return c != null ? c.get() : 0;
        }

        long resumedMessages(String topic) {
            AtomicLong c = resumedMessages.get(topic);
            return c != null ? c.get() : 0;
        }

        @Override
        public void recordSubscribe(String topic, String result) {
        }

        @Override
        public void recordPublish(String topic, String result) {
        }

        @Override
        public void recordReaperScan() {
        }

        @Override
        public void recordZombieReaped(int count) {
        }

        @Override
        public void recordError(String type) {
            errors.computeIfAbsent(type, k -> new AtomicLong()).incrementAndGet();
        }

        @Override
        public void recordTopicCompaction(int count) {
        }

        @Override
        public void recordSubscribeDuration(String topic, long durationMs) {
        }

        @Override
        public void recordPublishDuration(String topic, long durationMs) {
        }

        @Override
        public void recordResume(String topic, String result) {
            resumes.computeIfAbsent(topic + ":" + result, k -> new AtomicLong()).incrementAndGet();
        }

        @Override
        public void recordResumedMessages(String topic, long count) {
            if (count > 0) {
                resumedMessages.computeIfAbsent(topic, k -> new AtomicLong()).addAndGet(count);
            }
        }

        @Override
        public void cleanupTopic(String topic) {
        }

        @Override
        public void shutdownCleanup() {
        }

        @Override
        public boolean isEnabled() {
            return true;
        }
    }

    /** 订阅记录 */
    private static final class Recording {
        final List<TopicMessage<?, ?>> received = new CopyOnWriteArrayList<>();
        final CountDownLatch completed = new CountDownLatch(1);
        final AtomicReference<Throwable> error = new AtomicReference<>();
        volatile Disposable subscription;
    }

    private static Recording record(Flux<TopicMessage<?, ?>> flux) {
        Recording rec = new Recording();
        rec.subscription = flux.subscribe(
                rec.received::add,
                e -> {
                    rec.error.set(e);
                    rec.completed.countDown();
                },
                rec.completed::countDown);
        return rec;
    }

    private static TopicMessage<Serializable, Msg> env(Serializable id, String text) {
        TopicMessage<Serializable, Msg> m = new TopicMessage<>();
        m.setId(id);
        m.setTopic(TOPIC);
        m.setMsg(new TestMsg(text));
        return m;
    }

    private static ConnectionMetadata metadata(String lastEventId) {
        return ConnectionMetadata.builder()
                .topic(TOPIC)
                .lastEventId(lastEventId)
                .build();
    }

    private static void assertResync(TopicMessage<?, ?> message) {
        assertTrue(message.getMsg() instanceof ResumeSignal,
                "期望 resync 信号，实际: " + message);
        assertNull(message.getId(), "resync 信号不应携带 ID");
    }

    private static List<Object> ids(Recording rec) {
        return rec.received.stream().map(TopicMessage::getId).collect(Collectors.toList());
    }

    private static List<Object> idsOfNonResync(Recording rec) {
        return rec.received.stream()
                .filter(m -> !(m.getMsg() instanceof ResumeSignal))
                .map(TopicMessage::getId)
                .collect(Collectors.toList());
    }

    private static long countResync(Recording rec) {
        return rec.received.stream().filter(m -> m.getMsg() instanceof ResumeSignal).count();
    }

    private static void awaitSize(Recording rec, int size, long timeoutMs)
            throws InterruptedException {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (rec.received.size() < size && System.currentTimeMillis() < deadline) {
            Thread.sleep(5);
        }
        assertTrue(rec.received.size() >= size,
                "期望至少 " + size + " 条消息，实际 " + rec.received.size());
    }

    private static void awaitFlag(AtomicBoolean flag, String message) throws InterruptedException {
        long deadline = System.currentTimeMillis() + 2000;
        while (!flag.get() && System.currentTimeMillis() < deadline) {
            Thread.sleep(5);
        }
        assertTrue(flag.get(), message);
    }
}
