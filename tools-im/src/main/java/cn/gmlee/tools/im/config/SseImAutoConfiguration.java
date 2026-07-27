package cn.gmlee.tools.im.config;

import cn.gmlee.tools.im.annotation.TopicSubscription;
import cn.gmlee.tools.im.core.*;
import cn.gmlee.tools.im.event.TopicMessage;
import cn.gmlee.tools.im.sse.SseConnectionManager;
import cn.gmlee.tools.im.sse.SseController;
import cn.gmlee.tools.im.sse.SseProperties;
import cn.gmlee.tools.im.sse.SseTopicSubscriber;
import cn.gmlee.tools.im.stream.StreamBridgeBroadcaster;
import cn.gmlee.tools.im.stream.StreamBroadcastConsumer;
import cn.gmlee.tools.im.stream.StreamProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * SseIm 自动配置
 * <p>
 * 核心组件自动装配：
 * <ul>
 *   <li>{@link TopicRouter} — Topic 路由器（通配符匹配）</li>
 *   <li>{@link TopicPublisher} — 消息发布器</li>
 *   <li>{@link SseConnectionManager} — SSE 连接管理</li>
 *   <li>{@link SseController} — 动态 SSE Controller</li>
 *   <li>{@link StreamBridgeBroadcaster} — Spring Cloud Stream 广播（可选）</li>
 *   <li>订阅者自动扫描注册（接口 + 注解两种方式）</li>
 * </ul>
 *
 * @author SseIm Framework
 * @since 1.0.0
 */
@Slf4j
@Configuration
@EnableConfigurationProperties({SseProperties.class, StreamProperties.class})
public class SseImAutoConfiguration {

    /**
     * 实例唯一 ID（用于多实例环境下的消息去重）
     */
    static final String INSTANCE_ID = UUID.randomUUID().toString().substring(0, 8);

    // ==================== 核心组件 ====================

    @Bean
    @ConditionalOnMissingBean
    public TopicRouter topicRouter() {
        log.info("[SseIm] Creating DefaultTopicRouter");
        return new DefaultTopicRouter();
    }

    @Bean
    @ConditionalOnMissingBean
    public DefaultTopicPublisher topicPublisher(TopicRouter router) {
        log.info("[SseIm] Creating DefaultTopicPublisher, instanceId={}", INSTANCE_ID);
        return new DefaultTopicPublisher(router, INSTANCE_ID);
    }

    // ==================== SSE 组件 ====================

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "sse-im.sse", name = "enabled", havingValue = "true", matchIfMissing = true)
    public SseConnectionManager sseConnectionManager(SseProperties properties) {
        log.info("[SseIm] Creating SseConnectionManager");
        return new SseConnectionManager(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "sse-im.sse", name = "enabled", havingValue = "true", matchIfMissing = true)
    public SseTopicSubscriber sseTopicSubscriber(SseConnectionManager connectionManager) {
        log.info("[SseIm] Creating SseTopicSubscriber (bridge TopicRouter -> SSE)");
        return new SseTopicSubscriber(connectionManager);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "sse-im.sse", name = "enabled", havingValue = "true", matchIfMissing = true)
    public SseController sseController(SseConnectionManager connectionManager,
                                       SseProperties properties,
                                       TopicRouter router) {
        log.info("[SseIm] Creating SseController, basePath={}", properties.getBasePath());
        return new SseController(connectionManager, properties, router);
    }

    // ==================== Spring Cloud Stream 集成（可选） ====================

    /**
     * Spring Cloud Stream 集成配置
     * <p>
     * 当 classpath 中存在 StreamBridge（即引入了 spring-cloud-stream）且配置 sse-im.stream.enabled=true 时，
     * 自动创建广播发布者和消费者 Bean，实现多实例消息同步。
     * </p>
     * <p>
     * 不绑定任何具体 Broker —— 开发者通过引入不同的 binder 依赖来选择：
     * <ul>
     *   <li>RabbitMQ: spring-cloud-stream-binder-rabbit</li>
     *   <li>Kafka: spring-cloud-stream-binder-kafka</li>
     *   <li>其他: 任意 Spring Cloud Stream binder</li>
     * </ul>
     */
    @Configuration
    @ConditionalOnClass(StreamBridge.class)
    @ConditionalOnProperty(prefix = "sse-im.stream", name = "enabled", havingValue = "true")
    public static class StreamIntegrationConfiguration {

        /**
         * 广播发布者 — 通过 StreamBridge 将消息发送到 Stream
         */
        @Bean
        @ConditionalOnMissingBean
        public StreamBridgeBroadcaster streamBridgeBroadcaster(StreamBridge streamBridge,
                                                               StreamProperties properties) {
            log.info("[SseIm] Creating StreamBridgeBroadcaster, destination={}", properties.getBroadcastDestination());
            return new StreamBridgeBroadcaster(streamBridge, properties, INSTANCE_ID);
        }

        /**
         * 广播消费者 — 从 Stream 接收其他实例的消息
         * <p>
         * Bean 名称 {@code sseImBroadcastConsumer} 对应 Spring Cloud Stream 绑定
         * {@code sseImBroadcastConsumer-in-0}。
         * </p>
         */
        @Bean(name = "sseImBroadcastConsumer")
        @ConditionalOnMissingBean(name = "sseImBroadcastConsumer")
        public Consumer<TopicMessage<?>> sseImBroadcastConsumer(DefaultTopicPublisher publisher,
                                                                StreamProperties properties) {
            log.info("[SseIm] Creating StreamBroadcastConsumer (function bean)");
            return new StreamBroadcastConsumer(publisher, properties, INSTANCE_ID);
        }

        /**
         * 将 StreamBridgeBroadcaster 注册为 DefaultTopicPublisher 的广播回调
         */
        @Bean
        public StreamBroadcastCallbackRegistrar streamBroadcastCallbackRegistrar(
                DefaultTopicPublisher publisher,
                StreamBridgeBroadcaster broadcaster) {
            return new StreamBroadcastCallbackRegistrar(publisher, broadcaster);
        }
    }

    /**
     * 将广播回调注册到 Publisher
     */
    public static class StreamBroadcastCallbackRegistrar {
        public StreamBroadcastCallbackRegistrar(DefaultTopicPublisher publisher,
                                                StreamBridgeBroadcaster broadcaster) {
            publisher.setBroadcastCallback(broadcaster);
            log.info("[SseIm] Registered StreamBridgeBroadcaster as broadcast callback");
        }
    }

    // ==================== 订阅者自动注册 ====================

    @Bean
    public TopicSubscriberRegistrar topicSubscriberRegistrar(TopicRouter router,
                                                             ApplicationContext context) {
        return new TopicSubscriberRegistrar(router, context);
    }

    /**
     * 订阅者注册器
     * <p>
     * 启动时扫描并注册所有 {@link TopicSubscriber} 接口实现和 {@link TopicSubscription} 注解方法。
     * </p>
     */
    public static class TopicSubscriberRegistrar {

        public TopicSubscriberRegistrar(TopicRouter router, ApplicationContext context) {
            // 1. 注册所有 TopicSubscriber 接口实现
            Map<String, TopicSubscriber> subscriberBeans = context.getBeansOfType(TopicSubscriber.class);
            for (TopicSubscriber<?> subscriber : subscriberBeans.values()) {
                router.registerSubscriber(subscriber);
                log.info("[SseIm] Registered TopicSubscriber: {} -> {}",
                        subscriber.getSubscriberId(), subscriber.getTopic());
            }

            // 2. 扫描 @TopicSubscription 注解方法
            Map<String, Object> candidates = new ConcurrentHashMap<>();
            candidates.putAll(context.getBeansWithAnnotation(org.springframework.stereotype.Component.class));
            candidates.putAll(context.getBeansWithAnnotation(org.springframework.stereotype.Service.class));
            candidates.putAll(context.getBeansWithAnnotation(org.springframework.stereotype.Controller.class));

            int annotatedCount = 0;
            for (Object bean : candidates.values()) {
                annotatedCount += registerAnnotatedMethods(router, bean);
            }

            log.info("[SseIm] Startup complete: {} interface subscribers, {} annotated methods, instanceId={}",
                    subscriberBeans.size(), annotatedCount, INSTANCE_ID);
        }

        private int registerAnnotatedMethods(TopicRouter router, Object bean) {
            int count = 0;
            Class<?> clazz = bean.getClass();
            TopicSubscription classAnnotation = clazz.getAnnotation(TopicSubscription.class);

            for (Method method : clazz.getDeclaredMethods()) {
                TopicSubscription annotation = method.getAnnotation(TopicSubscription.class);
                if (annotation == null && classAnnotation != null) {
                    annotation = classAnnotation;
                }
                if (annotation == null) {
                    continue;
                }

                String topic = annotation.value().isEmpty() ? annotation.pattern() : annotation.value();
                if (topic.isEmpty()) {
                    continue;
                }

                method.setAccessible(true);
                MethodTopicSubscriber subscriber = new MethodTopicSubscriber(bean, method, topic, annotation.priority());
                router.registerSubscriber(subscriber);
                log.info("[SseIm] Registered @TopicSubscription: {}#{} -> {}",
                        clazz.getSimpleName(), method.getName(), topic);
                count++;
            }
            return count;
        }
    }

    /**
     * 方法级订阅者
     */
    private static class MethodTopicSubscriber implements TopicSubscriber<Object> {

        private final Object bean;
        private final Method method;
        private final String topic;
        private final int priority;

        public MethodTopicSubscriber(Object bean, Method method, String topic, int priority) {
            this.bean = bean;
            this.method = method;
            this.topic = topic;
            this.priority = priority;
        }

        @Override
        public String getTopic() {
            return topic;
        }

        @Override
        public void onMessage(String topic, Object message) {
            try {
                Class<?>[] paramTypes = method.getParameterTypes();
                if (paramTypes.length == 0) {
                    method.invoke(bean);
                } else if (paramTypes.length == 1) {
                    if (paramTypes[0] == String.class) {
                        method.invoke(bean, topic);
                    } else {
                        method.invoke(bean, message);
                    }
                } else if (paramTypes.length == 2) {
                    method.invoke(bean, topic, message);
                }
            } catch (Exception e) {
                log.error("[SseIm] Failed to invoke @TopicSubscription method: {}#{}",
                        bean.getClass().getSimpleName(), method.getName(), e);
            }
        }

        @Override
        public int getPriority() {
            return priority;
        }

        @Override
        public String getSubscriberId() {
            return bean.getClass().getSimpleName() + "#" + method.getName();
        }
    }
}
