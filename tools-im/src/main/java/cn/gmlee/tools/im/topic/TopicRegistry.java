package cn.gmlee.tools.im.topic;

import cn.gmlee.tools.im.core.Publisher;
import cn.gmlee.tools.im.core.Repeater;
import cn.gmlee.tools.im.core.Subscriber;
import cn.gmlee.tools.im.core.Topic;
import cn.gmlee.tools.im.ex.TopicNotFoundException;
import cn.gmlee.tools.im.sse.SseConnectionManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Topic 组件注册表.
 * <p>
 * 管理每个 Topic 的 {@link Publisher}、{@link Repeater}、{@link Subscriber} 组件。
 * 支持自定义实现（Spring Bean 自动发现）和默认实现（自动创建）。
 * </p>
 *
 * <h3>匹配规则</h3>
 * <ul>
 *   <li>自定义实现通过 {@code topic()} 方法匹配到对应 Topic</li>
 *   <li>如果某 Topic 无自定义实现，自动创建默认实现</li>
 *   <li>幂等：同一 Topic 的组件只创建/匹配一次</li>
 * </ul>
 *
 * <h3>线程安全</h3>
 * <p>
 * 所有操作通过 {@link ConcurrentHashMap} 保证原子性，支持运行时动态注册。
 * </p>
 *
 * @since 5.6.0
 */
@Slf4j
public class TopicRegistry {

    /**
     * 自定义 Publisher 索引（topic → Publisher，不可变）
     */
    private final Map<String, Publisher> customPublishers;

    /**
     * 自定义 Repeater 索引（topic → Repeater，不可变）
     */
    private final Map<String, Repeater> customRepeaters;

    /**
     * 自定义 Subscriber 索引（topic → Subscriber，不可变）
     */
    private final Map<String, Subscriber> customSubscribers;

    /**
     * 已确保的 Publisher（topic → Publisher，含默认和自定义）
     */
    private final ConcurrentHashMap<String, Publisher> publishers = new ConcurrentHashMap<>();

    /**
     * 已确保的 Repeater（topic → Repeater，含默认和自定义）
     */
    private final ConcurrentHashMap<String, Repeater> repeaters = new ConcurrentHashMap<>();

    /**
     * 已确保的 Subscriber（topic → Subscriber，含默认和自定义）
     */
    private final ConcurrentHashMap<String, Subscriber> subscribers = new ConcurrentHashMap<>();

    /**
     * Stream 桥接器（用于创建默认实现）
     */
    private final StreamBridge streamBridge;

    /**
     * SSE 连接管理器（用于创建默认实现）
     */
    private final SseConnectionManager sseConnectionManager;

    /**
     * 创建 Topic 组件注册表.
     *
     * @param customPublishers     自定义 Publisher 列表（Spring 注入，可为 null）
     * @param customRepeaters      自定义 Repeater 列表（Spring 注入，可为 null）
     * @param customSubscribers    自定义 Subscriber 列表（Spring 注入，可为 null）
     * @param streamBridge         Stream 桥接器
     * @param sseConnectionManager SSE 连接管理器
     */
    public TopicRegistry(List<Publisher> customPublishers,
                         List<Repeater> customRepeaters,
                         List<Subscriber> customSubscribers,
                         StreamBridge streamBridge,
                         SseConnectionManager sseConnectionManager) {
        this.customPublishers = indexByTopic(customPublishers);
        this.customRepeaters = indexByTopic(customRepeaters);
        this.customSubscribers = indexByTopic(customSubscribers);
        this.streamBridge = streamBridge;
        this.sseConnectionManager = sseConnectionManager;
    }

    // ==================== Ensure 方法（幂等，供框架内部使用） ====================

    /**
     * 确保 Topic 的 Publisher 已创建.
     * <p>
     * 幂等：如果存在自定义 Publisher 则使用自定义实现，否则创建默认实现。
     * </p>
     *
     * @param topic Topic 名称
     * @return Publisher 实例
     */
    public Publisher ensurePublisher(String topic) {
        return publishers.computeIfAbsent(topic, t -> {
            Publisher custom = customPublishers.get(t);
            if (custom != null) {
                log.info("[TopicRegistry] 使用自定义 Publisher: topic={}, class={}",
                        t, custom.getClass().getSimpleName());
                return custom;
            }
            Publisher def = new DefaultPublisher(t, this);
            log.info("[TopicRegistry] 创建默认 Publisher: topic={}", t);
            return def;
        });
    }

    /**
     * 确保 Topic 的 Repeater 已创建.
     *
     * @param topic Topic 名称
     * @return Repeater 实例
     */
    public Repeater ensureRepeater(String topic) {
        return repeaters.computeIfAbsent(topic, t -> {
            Repeater custom = customRepeaters.get(t);
            if (custom != null) {
                log.info("[TopicRegistry] 使用自定义 Repeater: topic={}, class={}",
                        t, custom.getClass().getSimpleName());
                return custom;
            }
            Repeater def = new DefaultRepeater(t, streamBridge, sseConnectionManager);
            log.info("[TopicRegistry] 创建默认 Repeater: topic={}", t);
            return def;
        });
    }

    /**
     * 确保 Topic 的 Subscriber 已创建.
     *
     * @param topic Topic 名称
     * @return Subscriber 实例
     */
    public Subscriber ensureSubscriber(String topic) {
        return subscribers.computeIfAbsent(topic, t -> {
            Subscriber custom = customSubscribers.get(t);
            if (custom != null) {
                log.info("[TopicRegistry] 使用自定义 Subscriber: topic={}, class={}",
                        t, custom.getClass().getSimpleName());
                return custom;
            }
            Subscriber def = new DefaultSubscriber(t, sseConnectionManager);
            log.info("[TopicRegistry] 创建默认 Subscriber: topic={}", t);
            return def;
        });
    }

    // ==================== Get 方法（严格模式，供路由器使用） ====================

    /**
     * 获取 Topic 的 Publisher.
     *
     * @param topic Topic 名称
     * @return Publisher 实例
     * @throws TopicNotFoundException 如果 Publisher 未创建
     */
    public Publisher getPublisher(String topic) {
        Publisher publisher = publishers.get(topic);
        if (publisher == null) {
            throw new TopicNotFoundException(topic);
        }
        return publisher;
    }

    /**
     * 获取 Topic 的 Repeater.
     *
     * @param topic Topic 名称
     * @return Repeater 实例
     * @throws TopicNotFoundException 如果 Repeater 未创建
     */
    public Repeater getRepeater(String topic) {
        Repeater repeater = repeaters.get(topic);
        if (repeater == null) {
            throw new TopicNotFoundException(topic);
        }
        return repeater;
    }

    /**
     * 获取 Topic 的 Subscriber.
     *
     * @param topic Topic 名称
     * @return Subscriber 实例
     * @throws TopicNotFoundException 如果 Subscriber 未创建
     */
    public Subscriber getSubscriber(String topic) {
        Subscriber subscriber = subscribers.get(topic);
        if (subscriber == null) {
            throw new TopicNotFoundException(topic);
        }
        return subscriber;
    }

    // ==================== 工厂方法（供自定义实现委托默认行为） ====================

    /**
     * 创建默认 Publisher 实例.
     * <p>
     * 供自定义 Publisher 实现委托默认行为时使用。
     * </p>
     *
     * @param topic Topic 名称
     * @return 默认 Publisher
     */
    public Publisher createDefaultPublisher(String topic) {
        return new DefaultPublisher(topic, this);
    }

    /**
     * 创建默认 Repeater 实例.
     *
     * @param topic Topic 名称
     * @return 默认 Repeater
     */
    public Repeater createDefaultRepeater(String topic) {
        return new DefaultRepeater(topic, streamBridge, sseConnectionManager);
    }

    /**
     * 创建默认 Subscriber 实例.
     *
     * @param topic Topic 名称
     * @return 默认 Subscriber
     */
    public Subscriber createDefaultSubscriber(String topic) {
        return new DefaultSubscriber(topic, sseConnectionManager);
    }

    // ==================== 内部方法 ====================

    @SuppressWarnings("unchecked")
    private static <T extends Topic> Map<String, T> indexByTopic(List<T> list) {
        if (list == null || list.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, T> map = new HashMap<>();
        for (T component : list) {
            String topic = component.topic();
            if (topic != null) {
                map.put(topic, component);
            }
        }
        return map;
    }
}
