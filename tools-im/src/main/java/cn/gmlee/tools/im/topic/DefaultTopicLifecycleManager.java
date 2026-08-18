package cn.gmlee.tools.im.topic;

import cn.gmlee.tools.im.conf.SseProperties;
import cn.gmlee.tools.im.model.TopicState;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Topic 生命周期管理器默认实现.
 * <p>
 * 管理 Topic 的完整生命周期，协调 {@link TopicRegistry} 和 {@link TopicResourceFactory} 的创建/销毁。
 * </p>
 *
 * <h3>并发安全</h3>
 * <ul>
 *   <li>使用 {@link ConcurrentHashMap} 存储 Topic 元数据</li>
 *   <li>使用 {@link AtomicInteger} 管理引用计数</li>
 *   <li>使用 {@code compute()} 保证状态转换的原子性</li>
 * </ul>
 *
 * <h3>资源清理流程</h3>
 * <ol>
 *   <li>标记 Topic 为 DESTROYING 状态（防止并发销毁）</li>
 *   <li>触发 {@link TopicLifecycleListener#onTopicDestroying} 回调</li>
 *   <li>清理 {@link TopicResourceFactory} 的物理资源（CLUSTER: binding/Consumer Bean / STANDALONE: 内存资源）</li>
 *   <li>清理 {@link TopicRegistry} 的逻辑组件（Publisher/Repeater/Subscriber）</li>
 *   <li>标记 Topic 为 DESTROYED 状态</li>
 *   <li>触发 {@link TopicLifecycleListener#onTopicDestroyed} 回调</li>
 *   <li>从元数据 Map 中移除 Topic 记录</li>
 * </ol>
 *
 * @since 5.6.0
 */
@Slf4j
public class DefaultTopicLifecycleManager implements TopicLifecycleManager {

    private final TopicRegistry topicRegistry;
    private final TopicResourceFactory topicResourceFactory;
    private final SseProperties sseProperties;

    /**
     * Topic 元数据（状态、引用计数、最后活跃时间）
     */
    private final ConcurrentHashMap<String, TopicMetadata> metadata = new ConcurrentHashMap<>();

    /**
     * 生命周期监听器列表
     */
    private final List<TopicLifecycleListener> listeners = new CopyOnWriteArrayList<>();

    /**
     * Topic 空闲 TTL（引用计数为 0 后保留时间）
     */
    private final Duration idleTtl;

    /**
     * 创建 Topic 生命周期管理器.
     *
     * @param topicRegistry        Topic 组件注册表
     * @param topicResourceFactory Topic 资源工厂（CLUSTER 或 STANDALONE 模式实现）
     * @param sseProperties        SSE 配置
     */
    public DefaultTopicLifecycleManager(TopicRegistry topicRegistry,
                                        TopicResourceFactory topicResourceFactory,
                                        SseProperties sseProperties) {
        this.topicRegistry = topicRegistry;
        this.topicResourceFactory = topicResourceFactory;
        this.sseProperties = sseProperties;
        // 使用 cleanup.emptyTopicTtl 作为 Topic 空闲 TTL
        this.idleTtl = sseProperties.getCleanup().getEmptyTopicTtl();
    }

    @Override
    public void acquire(String topic) {
        TopicMetadata meta = metadata.compute(topic, (k, v) -> {
            if (v == null) {
                // 新 Topic：初始化为 CREATED 状态，引用计数 1
                return new TopicMetadata(TopicState.CREATED, 1);
            } else if (v.state == TopicState.DESTROYED || v.state == TopicState.DESTROYING) {
                // 已销毁或销毁中：重新激活。
                // 引用计数重置为 1（而非 incrementAndGet）：
                // destroy 已清理所有物理资源（Publisher/Repeater/Subscriber、Stream binding），
                // 旧 refCount 失去资源语义。re-acquire 等价于全新开始，从 1 计数。
                v.state = TopicState.CREATED;
                v.refCount.set(1);
                v.lastActivityTime = Instant.now();
                return v;
            } else {
                // 已存在：增加引用计数
                v.refCount.incrementAndGet();
                v.lastActivityTime = Instant.now();
                return v;
            }
        });

        // 触发监听器回调（在 compute 外执行，避免持锁）
        if (meta.refCount.get() == 1 && meta.state == TopicState.CREATED) {
            fireTopicCreated(topic);
        }
    }

    @Override
    public void release(String topic) {
        TopicMetadata meta = metadata.get(topic);
        if (meta == null) {
            return;
        }

        int newRefCount = meta.refCount.decrementAndGet();
        meta.lastActivityTime = Instant.now();

        if (newRefCount < 0) {
            log.warn("[TopicLifecycle] 引用计数为负: topic={}, refCount={}", topic, newRefCount);
            meta.refCount.set(0);
        } else if (newRefCount == 0) {
            log.debug("[TopicLifecycle] Topic 引用计数归零: topic={}", topic);
            // 不立即销毁，等待 TTL 过期后由 cleanup() 处理
        }
    }

    @Override
    public boolean destroy(String topic) {
        TopicMetadata meta = metadata.get(topic);
        if (meta == null) {
            return false;
        }

        // 原子性标记为 DESTROYING（防止并发销毁）
        boolean marked = metadata.computeIfPresent(topic, (k, v) -> {
            if (v.state == TopicState.DESTROYING || v.state == TopicState.DESTROYED) {
                return v; // 已在销毁中或已销毁
            }
            v.state = TopicState.DESTROYING;
            return v;
        }) == meta && meta.state == TopicState.DESTROYING;

        if (!marked) {
            return false;
        }

        try {
            // 1. 触发销毁前回调
            fireTopicDestroying(topic);

            // 2. 清理物理资源（Spring Cloud Stream binding）
            topicResourceFactory.cleanupResources(topic);

            // 3. 清理逻辑组件（Publisher/Repeater/Subscriber）
            topicRegistry.destroyTopic(topic);

            // 4. 标记为 DESTROYED
            meta.state = TopicState.DESTROYED;

            // 5. 触发销毁后回调
            fireTopicDestroyed(topic);

            log.info("[TopicLifecycle] Topic 已销毁: topic={}", topic);
            return true;
        } catch (Exception e) {
            log.error("[TopicLifecycle] Topic 销毁失败: topic={}", topic, e);
            // 回滚状态
            meta.state = TopicState.ACTIVE;
            return false;
        } finally {
            // 此处不移除 metadata 条目，保留 DESTROYED 状态。
            // 目的：可观测性 — 管理员可通过 getState() 区分"从未存在"（null）与"最近被销毁"（DESTROYED）。
            // 条目由下一次 cleanup() 的阶段一（DESTROYED → remove）延迟移除。
            //
            // 注意：此处的延迟移除是监控设计，不是防抖动。
            // 防止资源频繁创建/销毁的机制是 cleanup 中的 idleTtl 检查：
            // Topic 空闲必须超过 idleTtl（默认 60s）才会触发 destroy，
            // 期间有新连接到来会复用已有资源，无需重建。
        }
    }

    @Override
    public TopicState getState(String topic) {
        TopicMetadata meta = metadata.get(topic);
        return meta != null ? meta.state : null;
    }

    @Override
    public int getRefCount(String topic) {
        TopicMetadata meta = metadata.get(topic);
        return meta != null ? meta.refCount.get() : 0;
    }

    @Override
    public Set<String> getAllTopics() {
        return Collections.unmodifiableSet(metadata.keySet());
    }

    @Override
    public Set<String> getTopicsByState(TopicState state) {
        return metadata.entrySet().stream()
                .filter(e -> e.getValue().state == state)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    @Override
    public void addListener(TopicLifecycleListener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    @Override
    public void removeListener(TopicLifecycleListener listener) {
        listeners.remove(listener);
    }

    /**
     * 执行一次自动清理.
     * <p>
     * 两阶段清理，每阶段处理不同状态的 Topic：
     * </p>
     * <ol>
     *   <li><b>阶段一</b>：移除已处于 DESTROYED 状态的条目（由上一次 cleanup 标记）。
     *       延迟移除是<b>可观测性设计</b>：DESTROYED 状态在两次 cleanup 之间可通过
     *       {@link #getState} 查询，区分"从未存在"与"最近被销毁"。</li>
     *   <li><b>阶段二</b>：检测空闲超时的 Topic（refCount = 0 且超过 idleTtl），
     *       调用 {@link #destroy} 标记为 DESTROYED。
     *       <b>防抖动机制是 idleTtl</b>：Topic 必须空闲超过 TTL 才会触发销毁，
     *       期间有新连接到来会复用已有资源，避免频繁创建/销毁。</li>
     * </ol>
     *
     * @return 被清理（销毁）的 Topic 数量
     */
    @Override
    public int cleanup() {
        if (metadata.isEmpty()) {
            return 0;
        }

        Instant now = Instant.now();
        int cleaned = 0;

        for (Map.Entry<String, TopicMetadata> entry : metadata.entrySet()) {
            String topic = entry.getKey();
            TopicMetadata meta = entry.getValue();

            // 清理已销毁的 Topic（延迟移除）
            if (meta.state == TopicState.DESTROYED) {
                metadata.remove(topic);
                continue;
            }

            // 清理空闲超时的 Topic（引用计数为 0 且超过 TTL）
            if (meta.state != TopicState.DESTROYING && meta.refCount.get() == 0) {
                Duration idleDuration = Duration.between(meta.lastActivityTime, now);
                if (idleDuration.compareTo(idleTtl) > 0) {
                    log.debug("[TopicLifecycle] 清理空闲 Topic: topic={}, idle={}", topic, idleDuration);
                    if (destroy(topic)) {
                        cleaned++;
                        // 注意：此处不立即移除 metadata 条目，由下一次 cleanup 的阶段一移除。
                        // 这样 DESTROYED 状态在两次 cleanup 之间可通过 getState() 查询，便于监控和调试。
                    }
                }
            }
        }

        if (cleaned > 0) {
            log.info("[TopicLifecycle] 自动清理完成: cleaned={}", cleaned);
        }

        return cleaned;
    }

    // ==================== 监听器回调 ====================

    private void fireTopicCreated(String topic) {
        for (TopicLifecycleListener listener : listeners) {
            try {
                listener.onTopicCreated(topic);
            } catch (Exception e) {
                log.warn("[TopicLifecycle] onTopicCreated 回调异常: listener={}, topic={}",
                        listener.getClass().getSimpleName(), topic, e);
            }
        }
    }

    private void fireTopicDestroying(String topic) {
        for (TopicLifecycleListener listener : listeners) {
            try {
                listener.onTopicDestroying(topic);
            } catch (Exception e) {
                log.warn("[TopicLifecycle] onTopicDestroying 回调异常: listener={}, topic={}",
                        listener.getClass().getSimpleName(), topic, e);
            }
        }
    }

    private void fireTopicDestroyed(String topic) {
        for (TopicLifecycleListener listener : listeners) {
            try {
                listener.onTopicDestroyed(topic);
            } catch (Exception e) {
                log.warn("[TopicLifecycle] onTopicDestroyed 回调异常: listener={}, topic={}",
                        listener.getClass().getSimpleName(), topic, e);
            }
        }
    }

    // ==================== 内部类 ====================

    /**
     * Topic 元数据（状态、引用计数、最后活跃时间）
     */
    private static class TopicMetadata {
        volatile TopicState state;
        final AtomicInteger refCount;
        volatile Instant lastActivityTime;

        TopicMetadata(TopicState state, int initialRefCount) {
            this.state = state;
            this.refCount = new AtomicInteger(initialRefCount);
            this.lastActivityTime = Instant.now();
        }
    }
}
