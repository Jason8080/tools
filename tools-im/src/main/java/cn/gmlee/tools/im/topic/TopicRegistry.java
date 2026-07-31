package cn.gmlee.tools.im.topic;

import cn.gmlee.tools.im.conf.SseProperties;
import cn.gmlee.tools.im.core.Publisher;
import cn.gmlee.tools.im.core.Repeater;
import cn.gmlee.tools.im.spi.factory.PublisherFactory;
import cn.gmlee.tools.im.spi.factory.RepeaterContext;
import cn.gmlee.tools.im.spi.factory.RepeaterFactory;
import cn.gmlee.tools.im.spi.interceptor.RepeaterInterceptor;
import cn.gmlee.tools.im.spi.factory.SubscriberFactory;
import cn.gmlee.tools.im.core.Subscriber;
import cn.gmlee.tools.im.ex.TopicNotFoundException;
import cn.gmlee.tools.im.sse.SseConnectionManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Topic 组件注册表.
 * <p>
 * 管理每个 Topic 的 {@link Publisher}、{@link Repeater}、{@link Subscriber} 组件。
 * 支持通过工厂模式创建自定义实现，否则使用默认实现。
 * </p>
 *
 * <h3>工厂匹配规则</h3>
 * <ul>
 *   <li>遍历所有 {@link PublisherFactory} / {@link RepeaterFactory} / {@link SubscriberFactory}</li>
 *   <li>调用工厂的 {@code create()} 方法，返回非 {@code null} 即采用</li>
 *   <li>所有工厂均返回 {@code null}，使用框架默认工厂</li>
 *   <li>幂等：同一 Topic 的组件只创建一次</li>
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
     * Publisher 工厂列表（不可变）
     */
    private final List<PublisherFactory> publisherFactories;

    /**
     * Repeater 工厂列表（不可变）
     */
    private final List<RepeaterFactory> repeaterFactories;

    /**
     * Subscriber 工厂列表（不可变）
     */
    private final List<SubscriberFactory> subscriberFactories;

    /**
     * 已确保的 Publisher（topic → Publisher）
     */
    private final ConcurrentHashMap<String, Publisher> publishers = new ConcurrentHashMap<>();

    /**
     * 已确保的 Repeater（topic → Repeater）
     */
    private final ConcurrentHashMap<String, Repeater> repeaters = new ConcurrentHashMap<>();

    /**
     * 已确保的 Subscriber（topic → Subscriber）
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
     * Repeater 拦截器列表（用于创建默认实现）
     */
    private final List<RepeaterInterceptor> interceptors;

    /**
     * SSE 配置（用于传递 deliveryKeys 等配置到默认组件）
     */
    private final SseProperties sseProperties;

    /**
     * 创建 Topic 组件注册表.
     *
     * @param publisherFactories   Publisher 工厂列表（Spring 注入，可为 null）
     * @param repeaterFactories    Repeater 工厂列表（Spring 注入，可为 null）
     * @param subscriberFactories  Subscriber 工厂列表（Spring 注入，可为 null）
     * @param streamBridge         Stream 桥接器
     * @param sseConnectionManager SSE 连接管理器
     * @param interceptors         Repeater 拦截器列表（Spring 注入，可为 null）
     */
    public TopicRegistry(List<PublisherFactory> publisherFactories,
                         List<RepeaterFactory> repeaterFactories,
                         List<SubscriberFactory> subscriberFactories,
                         StreamBridge streamBridge,
                         SseConnectionManager sseConnectionManager,
                         List<RepeaterInterceptor> interceptors) {
        this(publisherFactories, repeaterFactories, subscriberFactories,
                streamBridge, sseConnectionManager, interceptors, null);
    }

    /**
     * 创建 Topic 组件注册表.
     *
     * @param publisherFactories   Publisher 工厂列表（Spring 注入，可为 null）
     * @param repeaterFactories    Repeater 工厂列表（Spring 注入，可为 null）
     * @param subscriberFactories  Subscriber 工厂列表（Spring 注入，可为 null）
     * @param streamBridge         Stream 桥接器
     * @param sseConnectionManager SSE 连接管理器
     * @param interceptors         Repeater 拦截器列表（Spring 注入，可为 null）
     * @param sseProperties        SSE 配置（用于传递 deliveryKeys 等配置到默认组件，可为 null）
     */
    public TopicRegistry(List<PublisherFactory> publisherFactories,
                         List<RepeaterFactory> repeaterFactories,
                         List<SubscriberFactory> subscriberFactories,
                         StreamBridge streamBridge,
                         SseConnectionManager sseConnectionManager,
                         List<RepeaterInterceptor> interceptors,
                         SseProperties sseProperties) {
        this.publisherFactories = publisherFactories != null ? publisherFactories : Collections.emptyList();
        this.repeaterFactories = repeaterFactories != null ? repeaterFactories : Collections.emptyList();
        this.subscriberFactories = subscriberFactories != null ? subscriberFactories : Collections.emptyList();
        this.streamBridge = streamBridge;
        this.sseConnectionManager = sseConnectionManager;
        this.interceptors = interceptors != null ? interceptors : Collections.emptyList();
        this.sseProperties = sseProperties;
    }

    // ==================== Ensure 方法（幂等，供框架内部使用） ====================

    /**
     * 确保 Topic 的 Publisher 已创建.
     * <p>
     * 幂等：遍历工厂列表，首个返回非 {@code null} 的工厂创建实例；
     * 所有工厂均返回 {@code null}，使用默认实现。
     * </p>
     * <p>
     * <b>延迟解析机制</b>：Publisher 通过 {@code Supplier<Repeater>} 延迟获取 Repeater，
     * 避免构造时的循环依赖（Publisher → Repeater → Publisher）。首次调用
     * {@link cn.gmlee.tools.im.core.AbstractTopic#resolveRepeater()} 时才解析 Repeater 实例。
     * </p>
     *
     * @param topic Topic 名称
     * @return Publisher 实例
     */
    public Publisher ensurePublisher(String topic) {
        List<String> deliveryKeys = sseProperties != null ? sseProperties.getDeliveryKeys() : null;
        return ensureComponent(
                topic,
                publishers,
                publisherFactories,
                t -> new DefaultPublisher(t, () -> getRepeater(t), deliveryKeys),
                "Publisher"
        );
    }

    /**
     * 确保 Topic 的 Repeater 已创建.
     *
     * @param topic Topic 名称
     * @return Repeater 实例
     */
    public Repeater ensureRepeater(String topic) {
        return repeaters.computeIfAbsent(topic, t -> {
            // 创建 Repeater 上下文（封装 SSE 函数）
            RepeaterContext context = new RepeaterContext(
                    sseConnectionManager::publish,
                    sseConnectionManager::subscribe
            );

            for (RepeaterFactory factory : repeaterFactories) {
                Repeater repeater = factory.create(t, context);
                if (repeater != null) {
                    log.info("[TopicRegistry] 使用自定义 Repeater: topic={}, factory={}",
                            t, factory.getClass().getSimpleName());
                    return repeater;
                }
            }
            Repeater def = new DefaultRepeater(t, streamBridge, sseConnectionManager, interceptors);
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
        return ensureComponent(
                topic,
                subscribers,
                subscriberFactories,
                t -> new DefaultSubscriber(t, () -> getRepeater(t)),
                "Subscriber"
        );
    }

    /**
     * 通用组件创建方法（消除重复代码）.
     * <p>
     * 遍历工厂列表，首个返回非 null 的工厂创建实例；所有工厂均返回 null，使用默认实现。
     * </p>
     *
     * @param topic           Topic 名称
     * @param cache           组件缓存
     * @param factories       工厂列表（可为空）
     * @param defaultFactory  默认工厂函数
     * @param componentName   组件名称（用于日志）
     * @param <T>             组件类型
     * @return 组件实例
     */
    @SuppressWarnings("unchecked")
    private <T> T ensureComponent(String topic,
                                   ConcurrentHashMap<String, T> cache,
                                   List<?> factories,
                                   java.util.function.Function<String, T> defaultFactory,
                                   String componentName) {
        return cache.computeIfAbsent(topic, t -> {
            for (Object factory : factories) {
                Object component = null;

                if (factory instanceof PublisherFactory) {
                    component = ((PublisherFactory) factory).create(t, () -> getRepeater(t));
                } else if (factory instanceof SubscriberFactory) {
                    component = ((SubscriberFactory) factory).create(t, () -> getRepeater(t));
                }

                if (component != null) {
                    log.info("[TopicRegistry] 使用自定义 {}: topic={}, factory={}",
                            componentName, t, factory.getClass().getSimpleName());
                    return (T) component;
                }
            }

            T def = defaultFactory.apply(t);
            log.info("[TopicRegistry] 创建默认 {}: topic={}", componentName, t);
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
        return new DefaultPublisher(topic, () -> getRepeater(topic));
    }

    /**
     * 创建默认 Repeater 实例.
     *
     * @param topic Topic 名称
     * @return 默认 Repeater
     */
    public Repeater createDefaultRepeater(String topic) {
        return new DefaultRepeater(topic, streamBridge, sseConnectionManager, interceptors);
    }

    /**
     * 创建默认 Subscriber 实例.
     *
     * @param topic Topic 名称
     * @return 默认 Subscriber
     */
    public Subscriber createDefaultSubscriber(String topic) {
        return new DefaultSubscriber(topic, () -> getRepeater(topic));
    }
}
