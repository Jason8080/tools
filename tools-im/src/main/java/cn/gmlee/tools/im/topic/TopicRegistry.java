package cn.gmlee.tools.im.topic;

import cn.gmlee.tools.im.conf.DeploymentMode;
import cn.gmlee.tools.im.conf.SseProperties;
import cn.gmlee.tools.im.core.Publisher;
import cn.gmlee.tools.im.core.Repeater;
import cn.gmlee.tools.im.spi.factory.PublisherFactory;
import cn.gmlee.tools.im.spi.factory.RepeaterContext;
import cn.gmlee.tools.im.spi.factory.RepeaterFactory;
import cn.gmlee.tools.im.spi.interceptor.RepeaterInterceptor;
import cn.gmlee.tools.im.spi.factory.SubscriberFactory;
import cn.gmlee.tools.im.spi.routing.RoutingKeyComposer;
import cn.gmlee.tools.im.core.Subscriber;
import cn.gmlee.tools.im.ex.TopicNotFoundException;
import cn.gmlee.tools.im.sse.SseConnectionManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.lang.Nullable;
import tools.jackson.databind.ObjectMapper;

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
 * <h3>泛型设计</h3>
 * <p>
 * 内部使用 wildcard 存储（{@code Publisher<?, ?>}、{@code Repeater<?, ?>}、{@code Subscriber<?>}），
 * 公共 API 返回 wildcard 类型。同一 Topic 的 Publisher/Repeater/Subscriber 类型一致性
 * 由工厂匹配顺序保证：自定义工厂决定 ID/MSG 类型，默认实现使用 {@code Serializable/Msg}。
 * </p>
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
     * 已确保的 Publisher（topic → Publisher，wildcard 存储）
     */
    private final ConcurrentHashMap<String, Publisher<?, ?>> publishers = new ConcurrentHashMap<>();

    /**
     * 已确保的 Repeater（topic → Repeater，wildcard 存储）
     */
    private final ConcurrentHashMap<String, Repeater<?, ?>> repeaters = new ConcurrentHashMap<>();

    /**
     * 已确保的 Subscriber（topic → Subscriber，wildcard 存储）
     */
    private final ConcurrentHashMap<String, Subscriber<?>> subscribers = new ConcurrentHashMap<>();

    /**
     * Stream 桥接器（用于创建默认实现，CLUSTER 模式必需，STANDALONE 模式为 null）.
     */
    @Nullable
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
     * SSE 配置（用于传递 routingKeys 等配置到默认组件）
     */
    private final SseProperties sseProperties;

    /**
     * 路由键组合器（用于传递给默认 Publisher）
     */
    private final RoutingKeyComposer composer;

    /**
     * JSON 序列化器（用于 ConsumerBridge 反序列化消息）
     */
    private final ObjectMapper objectMapper;

    /**
     * 部署模式（决定使用哪种 Repeater 实现）
     */
    private final DeploymentMode deploymentMode;

    /**
     * 创建 Topic 组件注册表.
     *
     * @param publisherFactories   Publisher 工厂列表（Spring 注入，可为 null）
     * @param repeaterFactories    Repeater 工厂列表（Spring 注入，可为 null）
     * @param subscriberFactories  Subscriber 工厂列表（Spring 注入，可为 null）
     * @param streamBridge         Stream 桥接器（CLUSTER 模式必需，STANDALONE 模式可为 null）
     * @param sseConnectionManager SSE 连接管理器
     * @param interceptors         Repeater 拦截器列表（Spring 注入，可为 null）
     * @deprecated 使用包含 {@link DeploymentMode} 参数的完整构造器
     */
    @Deprecated
    public TopicRegistry(List<PublisherFactory> publisherFactories,
                         List<RepeaterFactory> repeaterFactories,
                         List<SubscriberFactory> subscriberFactories,
                         @Nullable StreamBridge streamBridge,
                         SseConnectionManager sseConnectionManager,
                         List<RepeaterInterceptor> interceptors) {
        this(publisherFactories, repeaterFactories, subscriberFactories,
                streamBridge, sseConnectionManager, interceptors, null, null, null,
                streamBridge != null ? DeploymentMode.CLUSTER : DeploymentMode.STANDALONE);
    }

    /**
     * 创建 Topic 组件注册表.
     *
     * @param publisherFactories   Publisher 工厂列表（Spring 注入，可为 null）
     * @param repeaterFactories    Repeater 工厂列表（Spring 注入，可为 null）
     * @param subscriberFactories  Subscriber 工厂列表（Spring 注入，可为 null）
     * @param streamBridge         Stream 桥接器（CLUSTER 模式必需，STANDALONE 模式可为 null）
     * @param sseConnectionManager SSE 连接管理器
     * @param interceptors         Repeater 拦截器列表（Spring 注入，可为 null）
     * @param sseProperties        SSE 配置（用于传递 routingKeys 等配置到默认组件，可为 null）
     * @deprecated 使用包含 {@link DeploymentMode} 参数的完整构造器
     */
    @Deprecated
    public TopicRegistry(List<PublisherFactory> publisherFactories,
                         List<RepeaterFactory> repeaterFactories,
                         List<SubscriberFactory> subscriberFactories,
                         @Nullable StreamBridge streamBridge,
                         SseConnectionManager sseConnectionManager,
                         List<RepeaterInterceptor> interceptors,
                         SseProperties sseProperties) {
        this(publisherFactories, repeaterFactories, subscriberFactories,
                streamBridge, sseConnectionManager, interceptors, sseProperties, null, null,
                streamBridge != null ? DeploymentMode.CLUSTER : DeploymentMode.STANDALONE);
    }

    /**
     * 创建 Topic 组件注册表（含路由键组合器）.
     *
     * @param publisherFactories   Publisher 工厂列表（Spring 注入，可为 null）
     * @param repeaterFactories    Repeater 工厂列表（Spring 注入，可为 null）
     * @param subscriberFactories  Subscriber 工厂列表（Spring 注入，可为 null）
     * @param streamBridge         Stream 桥接器（CLUSTER 模式必需，STANDALONE 模式可为 null）
     * @param sseConnectionManager SSE 连接管理器
     * @param interceptors         Repeater 拦截器列表（Spring 注入，可为 null）
     * @param sseProperties        SSE 配置（用于传递 routingKeys 等配置到默认组件，可为 null）
     * @param composer             路由键组合器（用于传递给默认 Publisher，可为 null 使用默认实现）
     * @deprecated 使用包含 {@link DeploymentMode} 参数的完整构造器
     */
    @Deprecated
    public TopicRegistry(List<PublisherFactory> publisherFactories,
                         List<RepeaterFactory> repeaterFactories,
                         List<SubscriberFactory> subscriberFactories,
                         @Nullable StreamBridge streamBridge,
                         SseConnectionManager sseConnectionManager,
                         List<RepeaterInterceptor> interceptors,
                         SseProperties sseProperties,
                         RoutingKeyComposer composer) {
        this(publisherFactories, repeaterFactories, subscriberFactories,
                streamBridge, sseConnectionManager, interceptors, sseProperties, composer, null,
                streamBridge != null ? DeploymentMode.CLUSTER : DeploymentMode.STANDALONE);
    }

    /**
     * 创建 Topic 组件注册表（完整参数）.
     *
     * @param publisherFactories   Publisher 工厂列表（Spring 注入，可为 null）
     * @param repeaterFactories    Repeater 工厂列表（Spring 注入，可为 null）
     * @param subscriberFactories  Subscriber 工厂列表（Spring 注入，可为 null）
     * @param streamBridge         Stream 桥接器（CLUSTER 模式必需，STANDALONE 模式可为 null）
     * @param sseConnectionManager SSE 连接管理器
     * @param interceptors         Repeater 拦截器列表（Spring 注入，可为 null）
     * @param sseProperties        SSE 配置（用于传递 routingKeys 等配置到默认组件，可为 null）
     * @param composer             路由键组合器（用于传递给默认 Publisher，可为 null 使用默认实现）
     * @param objectMapper         JSON 序列化器（用于 ConsumerBridge 反序列化消息，可为 null 使用默认实例）
     * @param deploymentMode       部署模式（决定使用哪种 Repeater 实现）
     */
    public TopicRegistry(List<PublisherFactory> publisherFactories,
                         List<RepeaterFactory> repeaterFactories,
                         List<SubscriberFactory> subscriberFactories,
                         @Nullable StreamBridge streamBridge,
                         SseConnectionManager sseConnectionManager,
                         List<RepeaterInterceptor> interceptors,
                         SseProperties sseProperties,
                         RoutingKeyComposer composer,
                         ObjectMapper objectMapper,
                         DeploymentMode deploymentMode) {
        this.publisherFactories = publisherFactories != null ? publisherFactories : Collections.emptyList();
        this.repeaterFactories = repeaterFactories != null ? repeaterFactories : Collections.emptyList();
        this.subscriberFactories = subscriberFactories != null ? subscriberFactories : Collections.emptyList();
        this.streamBridge = streamBridge;
        this.sseConnectionManager = sseConnectionManager;
        this.interceptors = interceptors != null ? interceptors : Collections.emptyList();
        this.sseProperties = sseProperties;
        this.composer = composer;
        this.objectMapper = objectMapper != null ? objectMapper : new ObjectMapper();
        this.deploymentMode = deploymentMode != null ? deploymentMode : DeploymentMode.CLUSTER;
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
    public Publisher<?, ?> ensurePublisher(String topic) {
        return ensurePublisherTyped(topic);
    }

    /**
     * 确保 Topic 的 Repeater 已创建.
     *
     * @param topic Topic 名称
     * @return Repeater 实例
     */
    public Repeater<?, ?> ensureRepeater(String topic) {
        return ensureRepeaterTyped(topic);
    }

    /**
     * 确保 Topic 的 Subscriber 已创建.
     *
     * @param topic Topic 名称
     * @return Subscriber 实例
     */
    public Subscriber<?> ensureSubscriber(String topic) {
        return ensureSubscriberTyped(topic);
    }

    // ==================== Get 方法（严格模式，供路由器使用） ====================

    /**
     * 获取 Topic 的 Publisher.
     *
     * @param topic Topic 名称
     * @return Publisher 实例
     * @throws TopicNotFoundException 如果 Publisher 未创建
     */
    public Publisher<?, ?> getPublisher(String topic) {
        Publisher<?, ?> publisher = publishers.get(topic);
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
    public Repeater<?, ?> getRepeater(String topic) {
        Repeater<?, ?> repeater = repeaters.get(topic);
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
    public Subscriber<?> getSubscriber(String topic) {
        Subscriber<?> subscriber = subscribers.get(topic);
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
    public Publisher<?, ?> createDefaultPublisher(String topic) {
        List<String> routingKeys = sseProperties != null ? sseProperties.getRoutingKeys() : null;
        return new DefaultPublisher(topic, () -> ensureRepeater(topic), routingKeys, composer);
    }

    /**
     * 创建默认 Repeater 实例.
     * <p>
     * 根据部署模式选择不同的实现：
     * </p>
     * <ul>
     *   <li><b>CLUSTER 模式</b>：{@link ClusterRepeater} - 使用 StreamBridge 发送到 MQ</li>
     *   <li><b>STANDALONE 模式</b>：{@link StandaloneRepeater} - 直接内存传递，绕过 MQ</li>
     * </ul>
     *
     * @param topic Topic 名称
     * @return 默认 Repeater
     */
    public Repeater<?, ?> createDefaultRepeater(String topic) {
        return switch (deploymentMode) {
            case CLUSTER -> new ClusterRepeater(topic, streamBridge, sseConnectionManager, interceptors);
            case STANDALONE -> new StandaloneRepeater(topic, sseConnectionManager, interceptors);
        };
    }

    /**
     * 创建默认 Subscriber 实例.
     *
     * @param topic Topic 名称
     * @return 默认 Subscriber
     */
    public Subscriber<?> createDefaultSubscriber(String topic) {
        return new DefaultSubscriber(topic, () -> ensureRepeater(topic));
    }

    // ==================== Consumer Bridge ====================

    /**
     * 创建 ConsumerBridge（包级私有，供 TopicResourceFactory 使用）.
     * <p>
     * ConsumerBridge 将泛型 Repeater 桥接到 SSE 管道所需的
     * {@code Consumer<TopicMessage<Msg>>} 接口，同时保证 Spring Cloud Stream
     * 能通过 GenericTypeResolver 正确解析 Consumer 类型参数。
     * </p>
     *
     * @param topic Topic 名称
     * @return ConsumerBridge 实例
     */
    ConsumerBridge createConsumerBridge(String topic) {
        return new ConsumerBridge(ensureRepeater(topic), objectMapper);
    }

    // ==================== 内部泛型辅助方法 ====================

    @SuppressWarnings("unchecked")
    private Repeater<?, ?> ensureRepeaterTyped(String topic) {
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
            // 根据部署模式选择默认实现
            Repeater def = createDefaultRepeater(t);
            log.info("[TopicRegistry] 创建默认 Repeater: topic={}, mode={}", t, deploymentMode);
            return def;
        });
    }

    @SuppressWarnings("unchecked")
    private Publisher<?, ?> ensurePublisherTyped(String topic) {
        List<String> routingKeys = sseProperties != null ? sseProperties.getRoutingKeys() : null;
        return publishers.computeIfAbsent(topic, t -> {
            // Supplier 延迟解析 Repeater（避免构造时循环依赖）
            // raw Supplier 传给 raw PublisherFactory.create()，运行时类型安全
            Publisher<?, ?> component = null;
            for (PublisherFactory factory : publisherFactories) {
                component = factory.create(t, () -> ensureRepeater(t));
                if (component != null) {
                    log.info("[TopicRegistry] 使用自定义 Publisher: topic={}, factory={}",
                            t, factory.getClass().getSimpleName());
                    return component;
                }
            }
            Publisher<?, ?> def = new DefaultPublisher(t, () -> ensureRepeater(t), routingKeys, composer);
            log.info("[TopicRegistry] 创建默认 Publisher: topic={}", t);
            return def;
        });
    }

    @SuppressWarnings("unchecked")
    private Subscriber<?> ensureSubscriberTyped(String topic) {
        return subscribers.computeIfAbsent(topic, t -> {
            Subscriber<?> component = null;
            for (SubscriberFactory factory : subscriberFactories) {
                component = factory.create(t, () -> ensureRepeater(t));
                if (component != null) {
                    log.info("[TopicRegistry] 使用自定义 Subscriber: topic={}, factory={}",
                            t, factory.getClass().getSimpleName());
                    return component;
                }
            }
            Subscriber<?> def = new DefaultSubscriber(t, () -> ensureRepeater(t));
            log.info("[TopicRegistry] 创建默认 Subscriber: topic={}", t);
            return def;
        });
    }

    // ==================== 生命周期管理（供 TopicLifecycleManager 调用） ====================

    /**
     * 销毁 Topic 的逻辑组件（Publisher/Repeater/Subscriber）.
     * <p>
     * 从三个注册表 Map 中移除 Topic 对应的组件实例。
     * 此方法由 {@link TopicLifecycleManager} 在 Topic 销毁时调用。
     * </p>
     *
     * <h3>注意</h3>
     * <p>
     * 此方法不会检查引用计数，调用方需确保 Topic 不再被使用。
     * 并发安全：使用 {@code remove()} 原子操作，不会与其他线程的 {@code get()} 冲突。
     * </p>
     *
     * @param topic Topic 名称
     */
    public void destroyTopic(String topic) {
        Publisher<?, ?> publisher = publishers.remove(topic);
        Repeater<?, ?> repeater = repeaters.remove(topic);
        Subscriber<?> subscriber = subscribers.remove(topic);

        if (publisher != null || repeater != null || subscriber != null) {
            log.info("[TopicRegistry] Topic 组件已销毁: topic={}, publisher={}, repeater={}, subscriber={}",
                    topic, publisher != null, repeater != null, subscriber != null);
        }
    }

    /**
     * 检查 Topic 是否存在逻辑组件.
     *
     * @param topic Topic 名称
     * @return 存在任意组件返回 true
     */
    public boolean hasComponents(String topic) {
        return publishers.containsKey(topic) || repeaters.containsKey(topic) || subscribers.containsKey(topic);
    }
}
