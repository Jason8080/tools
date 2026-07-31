package cn.gmlee.tools.im.topic;

import cn.gmlee.tools.im.conf.SseProperties;
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
 * 管理 Topic 的完整生命周期，协调 {@link TopicRegistry} 和 {@link TopicFactory} 的创建/销毁。
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
 *   <li>清理 {@link TopicFactory} 的物理资源（binding、Consumer Bean）</li>
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
    private final TopicFactory topicFactory;
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
     * @param topicRegistry Topic 组件注册表
     * @param topicFactory  Topic 资源工厂
     * @param sseProperties SSE 配置
     */
    public DefaultTopicLifecycleManager(TopicRegistry topicRegistry,
                                        TopicFactory topicFactory,
                                        SseProperties sseProperties) {
        this.topicRegistry = topicRegistry;
        this.topicFactory = topicFactory;
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
                // 已销毁或销毁中：重新激活
                v.state = TopicState.CREATED;
                v.refCount.incrementAndGet();
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
            topicFactory.cleanupTopicResources(topic);

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
            // 6. 从元数据 Map 中移除（延迟到下次 cleanup 或显式调用）
            // 这里不移除，保留 DESTROYED 状态一段时间，便于监控和调试
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
