package cn.gmlee.tools.im.conf;

import cn.gmlee.tools.im.endpoint.EndpointRegistry;
import cn.gmlee.tools.im.endpoint.EndpointRouter;
import cn.gmlee.tools.im.endpoint.ImAdminController;
import cn.gmlee.tools.im.spi.access.AccessFilter;
import cn.gmlee.tools.im.spi.converter.PrincipalRoutingKeyConverter;
import cn.gmlee.tools.im.spi.routing.RoutingKeyComposer;
import cn.gmlee.tools.im.sse.SseConnectionManager;
import cn.gmlee.tools.im.sse.cleanup.ConnectionReaper;
import cn.gmlee.tools.im.sse.metrics.SseMetrics;
import cn.gmlee.tools.im.topic.DefaultTopicLifecycleManager;
import cn.gmlee.tools.im.topic.TopicLifecycleManager;
import cn.gmlee.tools.im.topic.TopicRegistry;
import cn.gmlee.tools.im.topic.TopicResourceFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import jakarta.annotation.PostConstruct;
import java.util.List;

/**
 * 端点自动配置.
 * <p>
 * 读取 {@code im.endpoints} YAML 配置，自动注册端点、创建 Topic 资源、构建动态路由。
 * 开发者只需配置 YAML，无需编写任何 Java 代码。
 * </p>
 *
 * <h3>自动创建链路</h3>
 * <pre>
 * YAML im.endpoints
 *   → EndpointRegistry 注册端点配置
 *   → TopicResourceFactory 按需创建资源（CLUSTER: Stream binding / STANDALONE: 内存资源）
 *   → EndpointRouter 构建 RouterFunction
 * </pre>
 *
 * <h3>双模式支持</h3>
 * <p>
 * 此配置类适用于 CLUSTER 和 STANDALONE 两种部署模式。具体的 TopicRegistry 和 TopicResourceFactory
 * 实现由 {@link ClusterAutoConfiguration} 或 {@link StandaloneAutoConfiguration} 创建。
 * </p>
 *
 * @since 5.6.0
 * @see ClusterAutoConfiguration
 * @see StandaloneAutoConfiguration
 */
@Slf4j
@AutoConfiguration(after = {ImAutoConfiguration.class, ClusterAutoConfiguration.class, StandaloneAutoConfiguration.class})
@Import(EndpointAutoConfiguration.Registrar.class)
public class EndpointAutoConfiguration {

    private final ImProperties imProperties;
    private final ConfigurableListableBeanFactory beanFactory;
    private final BeanDefinitionRegistry beanDefinitionRegistry;
    private final SseProperties sseProperties;
    private final EndpointRegistry endpointRegistry;

    public EndpointAutoConfiguration(ImProperties imProperties,
                                      ConfigurableListableBeanFactory beanFactory,
                                      BeanDefinitionRegistry beanDefinitionRegistry,
                                      SseProperties sseProperties,
                                      EndpointRegistry endpointRegistry) {
        this.imProperties = imProperties;
        this.beanFactory = beanFactory;
        this.beanDefinitionRegistry = beanDefinitionRegistry;
        this.sseProperties = sseProperties;
        this.endpointRegistry = endpointRegistry;
    }

    /**
     * Topic 生命周期管理器 Bean.
     * <p>
     * 统一管理 Topic 的完整生命周期：状态管理、引用计数、资源协调、自动清理。
     * 解决 Topic 资源泄露问题（Publisher/Repeater/Subscriber 和 Stream binding）。
     * </p>
     *
     * <h3>核心职责</h3>
     * <ul>
     *   <li>跟踪每个 Topic 的状态（CREATED → ACTIVE → DESTROYING → DESTROYED）</li>
     *   <li>管理 Topic 引用计数（端点注册时递增，注销时递减）</li>
     *   <li>协调 {@link TopicRegistry} 和 {@link TopicResourceFactory} 的创建/销毁</li>
     *   <li>定期清理空闲 Topic（引用计数为 0 且超过 TTL）</li>
     * </ul>
     */
    @Bean
    @ConditionalOnMissingBean
    public TopicLifecycleManager topicLifecycleManager(TopicRegistry topicRegistry,
                                                        TopicResourceFactory topicResourceFactory,
                                                        SseProperties sseProperties,
                                                        EndpointRegistry endpointRegistry,
                                                        ConnectionReaper connectionReaper) {
        DefaultTopicLifecycleManager manager = new DefaultTopicLifecycleManager(
                topicRegistry, topicResourceFactory, sseProperties);

        // 注入到 EndpointRegistry（用于管理 Topic 引用计数）
        endpointRegistry.setTopicLifecycleManager(manager);

        // 注入到 ConnectionReaper（用于定期清理空闲 Topic）
        connectionReaper.setTopicLifecycleManager(manager);

        // 注册 TopicResourceFactory 为端点监听器
        endpointRegistry.addListener(topicResourceFactory);

        // 修复初始化顺序问题：为启动时注册的端点创建资源（监听器错过了这些端点）
        for (EndpointProperties props : endpointRegistry.listAll()) {
            try {
                topicResourceFactory.ensureResources(props);
            } catch (Exception e) {
                log.error("[EndpointAutoConfiguration] 为已有端点创建资源失败: {}", props.getPath(), e);
            }
        }

        log.info("[EndpointAutoConfiguration] TopicLifecycleManager 已初始化");
        return manager;
    }

    /**
     * 动态路由器 Bean.
     * <p>
     * 自动发现所有 {@link AccessFilter} 和 {@link PrincipalRoutingKeyConverter} Bean，
     * 按 Order 排序后注入到路由器。
     * </p>
     */
    @Bean
    @ConditionalOnMissingBean
    public EndpointRouter endpointRouter(EndpointRegistry endpointRegistry,
                                          TopicRegistry topicRegistry,
                                          @Autowired(required = false) List<AccessFilter> filters,
                                          @Autowired(required = false) List<PrincipalRoutingKeyConverter> converters,
                                          @Autowired(required = false) RoutingKeyComposer composer) {
        return new EndpointRouter(endpointRegistry, topicRegistry, sseProperties, filters, converters, composer);
    }

    /**
     * IM 管理控制器 Bean.
     * <p>
     * 提供运行时查询 API（端点、Topic、连接、统计信息、Topic 生命周期）。
     * </p>
     */
    @Bean
    @ConditionalOnMissingBean
    public ImAdminController imAdminController(EndpointRegistry endpointRegistry,
                                                SseConnectionManager connectionManager,
                                                TopicRegistry topicRegistry,
                                                @Autowired(required = false) SseMetrics metrics,
                                                @Autowired(required = false) TopicLifecycleManager topicLifecycleManager) {
        return new ImAdminController(endpointRegistry, connectionManager, topicRegistry, metrics, topicLifecycleManager);
    }

    /**
     * 注册 RouterFunction 到 Spring 容器.
     * <p>
     * WebFlux 自动发现所有 {@code RouterFunction<ServerResponse>} Bean 并加入路由链。
     * </p>
     */
    @Bean
    public RouterFunction<ServerResponse> imRouterFunction(EndpointRouter router) {
        return router.build();
    }

    /**
     * 启动时加载 YAML 配置的端点.
     */
    @PostConstruct
    public void registerYamlEndpoints() {
        List<EndpointProperties> endpoints = imProperties.getEndpoints();
        if (endpoints == null || endpoints.isEmpty()) {
            log.debug("[EndpointAutoConfiguration] 无 YAML 端点配置");
            return;
        }
        for (EndpointProperties props : endpoints) {
            try {
                endpointRegistry.register(props);
                // 监听器 TopicResourceFactory.onEndpointRegistered 已触发 ensureResources，无需重复调用
            } catch (Exception e) {
                log.error("[EndpointAutoConfiguration] 加载端点失败: {}", props, e);
            }
        }
    }

    /**
     * 通过 {@code @Import} 将 {@link BeanDefinitionRegistry} 注册为 Spring Bean，
     * 解决 Spring Boot 4.x 不再直接暴露 {@code BeanDefinitionRegistry} 的问题。
     */
    @Configuration(proxyBeanMethods = false)
    static class Registrar implements BeanDefinitionRegistryPostProcessor {

        @Override
        public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) {
            registry.registerBeanDefinition("imBeanDefinitionRegistry",
                    BeanDefinitionBuilder.genericBeanDefinition(BeanDefinitionRegistry.class, () -> registry)
                            .getBeanDefinition());
        }

        @Override
        public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
        }
    }
}
