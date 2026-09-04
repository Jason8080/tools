package cn.gmlee.tools.im.resume;

import cn.gmlee.tools.im.conf.SseProperties;
import cn.gmlee.tools.im.model.ConnectionMetadata;
import cn.gmlee.tools.im.model.ResumeSignal;
import cn.gmlee.tools.im.model.TopicMessage;
import cn.gmlee.tools.im.sse.metrics.SseMetrics;
import lombok.extern.slf4j.Slf4j;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.Serializable;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.List;

/**
 * 断点续传支持门面（写入侧挂点 + 读取侧零间隙续接引擎）.
 * <p>
 * 框架自身不持久化消息；本组件把 {@link MessageHistoryStore} SPI、
 * ID 编解码（{@link EventIdCodec}）与顺序比较（{@link EventIdComparator}）
 * 组装为完整的续传能力：
 * </p>
 * <ul>
 *   <li><b>写入侧</b>：{@link #storeAsync} 由 {@code ImRepeater.send} 在发送成功后
 *       fire-and-forget 调用，落盘失败只记录指标，不影响发布</li>
 *   <li><b>读取侧</b>：{@link #wrapIfResuming} 在客户端携带 {@code Last-Event-ID}
 *       重连时，把实时流包装为「历史回放 + 实时续接」的零间隙流</li>
 * </ul>
 *
 * <h3>零间隙续接算法</h3>
 * <pre>
 * 1. 先订阅实时流（消息进入待缓冲队列，宁可积压不可遗漏）
 * 2. 异步加载历史（{@code loadSince}），按 ID 升序回放，水位线随之下移
 * 3. 回放完成 → 原子切换（cutover）：排空待缓冲队列，
 *    以水位线比较器去重（ID ≤ 水位线的消息已在回放中出现，丢弃）
 * 4. 此后实时消息经水位线过滤直接下发
 * </pre>
 * <p>
 * 「先订阅、后回放」保证回放窗口内发布的消息不丢；
 * 「水位线去重」保证衔接处不重（ID 可比时），不可比时宁重勿漏。
 * </p>
 *
 * <h3>降级原则</h3>
 * <ul>
 *   <li>无存储 / 解码失败 / 位点为空 → 原样返回实时流</li>
 *   <li>历史加载抛错或超时 → 发送 {@code event: resync} 信号后切换实时流</li>
 *   <li>历史已知间隙（{@code gapKnown}）/ 回放超限 / 待缓冲溢出 → 同上</li>
 * </ul>
 *
 * <h3>资源安全</h3>
 * <p>
 * 续传会话（{@link ResumeSession}）通过 {@code Flux.create + onDispose}
 * 绑定下游生命周期：下游取消时同时释放实时流与历史流两个内部订阅，
 * 并清空待缓冲队列，无泄漏路径。
 * </p>
 *
 * @since 5.7.0
 * @see MessageHistoryStore
 */
@Slf4j
public class ResumeSupport {

    private final SseProperties.ResumeConfig config;
    private final EventIdCodec codec;
    private final EventIdComparator comparator;
    private final List<MessageHistoryStore> stores;
    private final SseMetrics metrics;

    /**
     * 创建续传支持组件.
     *
     * @param config     续传配置（{@code im.sse.resume}）
     * @param codec      事件 ID 编解码器
     * @param comparator 事件 ID 顺序比较器
     * @param stores     历史存储列表（按 {@link MessageHistoryStore#getOrder()} 排序后使用）
     * @param metrics    指标收集器
     */
    public ResumeSupport(SseProperties.ResumeConfig config,
                         EventIdCodec codec,
                         EventIdComparator comparator,
                         List<MessageHistoryStore> stores,
                         SseMetrics metrics) {
        this.config = config;
        this.codec = codec != null ? codec : EventIdCodec.DEFAULT;
        this.comparator = comparator != null ? comparator : EventIdComparator.DEFAULT;
        this.stores = stores == null
                ? List.of()
                : stores.stream()
                        .sorted(Comparator.comparingInt(MessageHistoryStore::getOrder))
                        .toList();
        this.metrics = metrics;
    }

    /**
     * 查找服务指定 Topic 的历史存储（按 Order 排序，首个 {@code supports} 命中者获胜）.
     *
     * @param topic Topic 名称
     * @return 匹配的存储；无匹配返回 null
     */
    public MessageHistoryStore findStore(String topic) {
        for (MessageHistoryStore store : stores) {
            if (store.supports(topic)) {
                return store;
            }
        }
        return null;
    }

    /**
     * 异步持久化消息（写入侧，fire-and-forget）.
     * <p>
     * 无 ID 的消息（如 {@link ResumeSignal} 信号）不入库。
     * 存储异常仅记录指标与日志，绝不向调用方传播。
     * </p>
     *
     * @param message 消息信封
     */
    public void storeAsync(TopicMessage<?, ?> message) {
        if (message == null || message.getId() == null || message.getTopic() == null) {
            return;
        }
        MessageHistoryStore store = findStore(message.getTopic());
        if (store == null) {
            return;
        }
        Mono<Void> mono;
        try {
            mono = store.store(message);
        } catch (Throwable t) {
            metrics.recordError("history_store");
            log.warn("[ResumeSupport] 历史存储 store() 抛出异常: topic={}, id={}",
                    message.getTopic(), message.getId(), t);
            return;
        }
        if (mono == null) {
            return;
        }
        mono.subscribe(
                unused -> { },
                error -> {
                    metrics.recordError("history_store");
                    log.warn("[ResumeSupport] 历史存储写入失败: topic={}, id={}",
                            message.getTopic(), message.getId(), error);
                }
        );
    }

    /**
     * 若客户端携带续传位点，则把实时流包装为零间隙「回放 + 续接」流.
     * <p>
     * 任一前置条件不满足（功能未启用 / 无位点 / 无存储 / 解码失败）时原样返回实时流。
     * </p>
     *
     * @param live     实时消息流（尚未被订阅）
     * @param topic    Topic 名称
     * @param metadata 连接元数据（含 Last-Event-ID）
     * @return 包装后的流（或原流）
     */
    public Flux<TopicMessage<?, ?>> wrapIfResuming(Flux<TopicMessage<?, ?>> live,
                                                    String topic,
                                                    ConnectionMetadata metadata) {
        if (config == null || !config.isEnabled()) {
            return live;
        }
        String raw = metadata != null ? metadata.getLastEventId() : null;
        if (raw == null || raw.isBlank()) {
            return live;
        }
        MessageHistoryStore store = findStore(topic);
        if (store == null) {
            log.debug("[ResumeSupport] 无历史存储服务该 Topic，跳过续传: topic={}", topic);
            return live;
        }
        Serializable lastEventId;
        try {
            lastEventId = codec.decode(raw);
        } catch (Exception e) {
            metrics.recordError("resume_decode");
            log.warn("[ResumeSupport] Last-Event-ID 解码失败，降级为仅实时流: topic={}, raw={}",
                    topic, raw, e);
            return live;
        }
        if (lastEventId == null) {
            return live;
        }
        log.debug("[ResumeSupport] 启动断点续传: topic={}, lastEventId={}", topic, lastEventId);
        return Flux.create(sink -> {
            ResumeSession session = new ResumeSession(sink, live, topic, store, lastEventId);
            sink.onDispose(session::dispose);
            session.start();
        });
    }

    // ==================== 续传会话（单次重连的生命周期） ====================

    /**
     * 单次续传会话.
     * <p>
     * 全部状态变更与消息下发都在 {@code gate} 监视器内完成，
     * 消除「回放/实时」两路之间的竞态；下游取消经 {@link #dispose()} 级联释放。
     * </p>
     */
    private final class ResumeSession {

        private final reactor.core.publisher.FluxSink<TopicMessage<?, ?>> sink;
        private final Flux<TopicMessage<?, ?>> live;
        private final String topic;
        private final MessageHistoryStore store;
        private final Serializable lastEventId;

        /** 状态门（所有可变状态 + 下发的唯一锁） */
        private final Object gate = new Object();
        /** 待缓冲队列：cutover 前到达的实时消息 */
        private final ArrayDeque<TopicMessage<?, ?>> pending = new ArrayDeque<>();

        private volatile Disposable liveSub;
        private volatile Disposable historySub;
        private volatile boolean disposed;

        /** 回放阶段（true）→ 实时阶段（false），单向切换 */
        private boolean replaying = true;
        /** 会话级回放总预算截止时刻（毫秒时间戳；{@link Long#MAX_VALUE} 表示不限制） */
        private long deadlineMillis = Long.MAX_VALUE;
        /** 实时流在回放期间已结束 */
        private boolean liveCompleted;
        /** 会话已终结（complete/error 仅一次） */
        private boolean terminated;
        /** resync 信号至多一次 */
        private boolean resyncSent;
        /** 水位线：已确认送达客户端的最大 ID */
        private Serializable watermark;
        /** 回放消息计数（含超限判定用） */
        private long replayed;
        /** 实际回放给客户端的消息数（指标） */
        private long replayEmitted;
        /** 指标结果：success / gap / failed */
        private String result = "success";

        ResumeSession(reactor.core.publisher.FluxSink<TopicMessage<?, ?>> sink,
                      Flux<TopicMessage<?, ?>> live,
                      String topic,
                      MessageHistoryStore store,
                      Serializable lastEventId) {
            this.sink = sink;
            this.live = live;
            this.topic = topic;
            this.store = store;
            this.lastEventId = lastEventId;
            this.watermark = lastEventId;
        }

        /**
         * 启动会话：先订阅实时流，再加载回放历史.
         */
        void start() {
            // 1) 先订阅实时流 —— 零间隙的关键：回放窗口内发布的消息全部进入 pending
            liveSub = live.subscribe(this::onLiveNext, this::onLiveError, this::onLiveComplete);
            if (disposed) {
                disposeSubs();
                return;
            }

            // 2) 加载历史（同步抛错 → 降级 + resync）
            HistoryLoadResult loadResult;
            try {
                loadResult = store.loadSince(topic, lastEventId);
            } catch (Throwable t) {
                metrics.recordError("resume_replay");
                log.warn("[ResumeSupport] 历史加载抛出异常，降级为实时流并通知客户端: topic={}", topic, t);
                synchronized (gate) {
                    if (replaying) {
                        result = "failed";
                        emitResyncLocked();
                        cutoverLocked();
                    }
                }
                return;
            }
            if (loadResult == null || loadResult.messages() == null || loadResult.gapKnown()) {
                log.debug("[ResumeSupport] 历史存在已知间隙，发送 resync: topic={}", topic);
                onGap();
                return;
            }

            // 3) 回放：总时长预算（会话级截止时间，在 onReplayNext 检查）+ 回放上限
            //    （超限 1 条用于触发 overflow 判定）；元素间隔超时作为挂起兜底保留
            Duration replayTimeout = config.getReplayTimeout();
            boolean hasBudget = replayTimeout != null
                    && !replayTimeout.isZero() && !replayTimeout.isNegative();
            deadlineMillis = hasBudget
                    ? System.currentTimeMillis() + replayTimeout.toMillis()
                    : Long.MAX_VALUE;
            Flux<TopicMessage<?, ?>> replayFlux = loadResult.messages();
            if (hasBudget) {
                replayFlux = replayFlux.timeout(replayTimeout);
            }
            Disposable sub = replayFlux
                    .take((long) config.getMaxReplay() + 1)
                    .subscribe(this::onReplayNext, this::onReplayFailed, this::onReplayComplete);
            historySub = sub;
            if (disposed) {
                sub.dispose();
            }
        }

        // ---------- 实时流回调 ----------

        private void onLiveNext(TopicMessage<?, ?> message) {
            synchronized (gate) {
                if (terminated) {
                    return;
                }
                if (replaying) {
                    if (pending.size() >= config.getMaxPending()) {
                        // 待缓冲溢出：通知客户端全量刷新，丢弃积压（后续消息继续缓冲）
                        metrics.recordError("resume_overflow");
                        log.warn("[ResumeSupport] 续传待缓冲溢出（>{}），发送 resync: topic={}",
                                config.getMaxPending(), topic);
                        emitResyncLocked();
                        pending.clear();
                    }
                    pending.addLast(message);
                } else {
                    emitIfAfterWatermarkLocked(message);
                }
            }
        }

        private void onLiveError(Throwable error) {
            synchronized (gate) {
                if (terminated) {
                    return;
                }
                terminated = true;
                sink.error(error);  // 触发 onDispose → dispose() 级联取消历史订阅
            }
        }

        private void onLiveComplete() {
            synchronized (gate) {
                if (terminated) {
                    return;
                }
                if (replaying) {
                    liveCompleted = true;  // cutover 时补发 complete
                } else {
                    terminated = true;
                    sink.complete();
                }
            }
        }

        // ---------- 回放回调 ----------

        private void onReplayNext(TopicMessage<?, ?> message) {
            synchronized (gate) {
                if (!replaying || terminated) {
                    return;
                }
                if (System.currentTimeMillis() > deadlineMillis) {
                    // 总预算耗尽：元素虽在缓慢但持续到达（元素间隔超时未触发），
                    // 同样降级为实时流，避免慢存储拖住续传会话
                    metrics.recordError("resume_replay");
                    log.warn("[ResumeSupport] 历史回放超出总预算（{}），发送 resync: topic={}",
                            config.getReplayTimeout(), topic);
                    result = "failed";
                    emitResyncLocked();
                    cutoverLocked();
                    Disposable h = historySub;
                    if (h != null) {
                        h.dispose();  // 停止慢存储的继续读取
                    }
                    return;
                }
                replayed++;
                if (replayed > config.getMaxReplay()) {
                    // 回放超限：历史过多，提示客户端全量刷新（take 已保证不会再有更多回放项）
                    log.warn("[ResumeSupport] 回放超限（>{}），发送 resync: topic={}",
                            config.getMaxReplay(), topic);
                    result = "gap";
                    emitResyncLocked();
                    cutoverLocked();
                    return;
                }
                if (emitIfAfterWatermarkLocked(message)) {
                    replayEmitted++;
                }
            }
        }

        private void onReplayFailed(Throwable error) {
            metrics.recordError("resume_replay");
            log.warn("[ResumeSupport] 历史回放失败（含超时），降级为实时流并通知客户端: topic={}", topic, error);
            synchronized (gate) {
                if (!replaying || terminated) {
                    return;
                }
                result = "failed";
                emitResyncLocked();
                cutoverLocked();
            }
        }

        private void onReplayComplete() {
            synchronized (gate) {
                if (replaying && !terminated) {
                    cutoverLocked();
                }
            }
        }

        private void onGap() {
            synchronized (gate) {
                if (!replaying || terminated) {
                    return;
                }
                result = "gap";
                emitResyncLocked();
                cutoverLocked();
            }
        }

        // ---------- 门内操作（调用方必须持有 gate） ----------

        /**
         * 原子切换到实时阶段：排空待缓冲（水位线去重），收尾指标与完成信号.
         */
        private void cutoverLocked() {
            replaying = false;
            for (TopicMessage<?, ?> message : pending) {
                emitIfAfterWatermarkLocked(message);
            }
            pending.clear();
            metrics.recordResume(topic, result);
            metrics.recordResumedMessages(topic, replayEmitted);
            log.debug("[ResumeSupport] 续传切换完成: topic={}, result={}, replayEmitted={}",
                    topic, result, replayEmitted);
            if (liveCompleted) {
                terminated = true;
                sink.complete();
            }
        }

        /**
         * 水位线过滤下发：仅当候选 ID 晚于水位线时下发，并推进水位线.
         *
         * @return true 表示已下发
         */
        private boolean emitIfAfterWatermarkLocked(TopicMessage<?, ?> message) {
            Serializable id = message.getId();
            if (!comparator.isAfter(id, watermark)) {
                return false;
            }
            sink.next(message);
            if (id != null) {
                watermark = id;
            }
            return true;
        }

        /**
         * 发送 resync 信号（至多一次）.
         */
        private void emitResyncLocked() {
            if (!resyncSent && !terminated) {
                resyncSent = true;
                sink.next(ResumeSignal.envelope(topic));
            }
        }

        // ---------- 资源释放 ----------

        /**
         * 释放会话资源（下游取消 / 终结时由 onDispose 触发）.
         */
        void dispose() {
            disposed = true;
            disposeSubs();
            synchronized (gate) {
                pending.clear();
            }
        }

        private void disposeSubs() {
            Disposable l = liveSub;
            if (l != null) {
                l.dispose();
            }
            Disposable h = historySub;
            if (h != null) {
                h.dispose();
            }
        }
    }
}
