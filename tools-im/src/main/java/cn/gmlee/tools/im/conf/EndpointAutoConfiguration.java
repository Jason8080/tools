package cn.gmlee.tools.im.conf;

import cn.gmlee.tools.im.spi.factory.PublisherFactory;
import cn.gmlee.tools.im.spi.factory.RepeaterFactory;
import cn.gmlee.tools.im.spi.interceptor.RepeaterInterceptor;
import cn.gmlee.tools.im.spi.factory.SubscriberFactory;
import cn.gmlee.tools.im.endpoint.EndpointRegistry;
import cn.gmlee.tools.im.endpoint.EndpointRouter;
import cn.gmlee.tools.im.endpoint.ImAdminController;
import cn.gmlee.tools.im.spi.access.AccessFilter;
import cn.gmlee.tools.im.spi.converter.PrincipalRoutingKeyConverter;
import cn.gmlee.tools.im.sse.SseConnectionManager;
import cn.gmlee.tools.im.sse.metrics.SseMetrics;
import cn.gmlee.tools.im.topic.TopicFactory;
import cn.gmlee.tools.im.topic.TopicRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.stream.binding.BindingService;
import org.springframework.cloud.stream.config.BindingServiceProperties;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import jakarta.annotation.PostConstruct;
import java.util.List;

/**
 * 端点自动配置.
 * <p>
 * 读取 {@code im.endpoints} YAML 配置，自动注册端点、创建 Stream 资源、构建动态路由。
 * 开发者只需配置 YAML，无需编写任何 Java 代码。
 * </p>
 *
 * <h3>自动创建链路</h3>
 * <pre>
 * YAML im.endpoints
 *   → EndpointRegistry 注册端点配置
 *   → TopicFactory 按需创建 Stream binding + Consumer Bean
 *   → EndpointRouter 构建 RouterFunction
 * </pre>
 *
 * @since 5.6.0
 */
@Slf4j
@AutoConfiguration(after = ImAutoConfiguration.class)
@ConditionalOnClass({StreamBridge.class, BindingServiceProperties.class})
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
     * Topic 组件注册表 Bean.
     * <p>
     * 自动发现自定义 {@link PublisherFactory}、{@link RepeaterFactory}、{@link SubscriberFactory}，
     * 并为未自定义的 Topic 创建默认实现。
     * </p>
     */
    @SuppressWarnings("rawtypes")
    @Bean
    @ConditionalOnMissingBean
    public TopicRegistry topicRegistry(
            @Autowired(required = false) List<PublisherFactory> publisherFactories,
            @Autowired(required = false) List<RepeaterFactory> repeaterFactories,
            @Autowired(required = false) List<SubscriberFactory> subscriberFactories,
            StreamBridge streamBridge,
            SseConnectionManager sseConnectionManager,
            @Autowired(required = false) List<RepeaterInterceptor> interceptors) {
        return new TopicRegistry(publisherFactories, repeaterFactories, subscriberFactories,
                streamBridge, sseConnectionManager, interceptors, sseProperties);
    }

    /**
     * Topic 资源工厂 Bean.
     * <p>
     * 自动注册为 {@link EndpointRegistry} 的监听器，端点注册时按需创建 Stream 资源。
     * 支持运行时动态注册端点，无需重启应用。
     * </p>
     */
    @Bean
    @ConditionalOnMissingBean
    public TopicFactory topicFactory(EndpointRegistry endpointRegistry,
                                      BindingServiceProperties bindingServiceProperties,
                                      BindingService bindingService,
                                      TopicRegistry topicRegistry) {
        TopicFactory factory = new TopicFactory(
                bindingServiceProperties, bindingService, beanDefinitionRegistry, topicRegistry);
        endpointRegistry.addListener(factory);
        return factory;
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
                                          @Autowired(required = false) List<PrincipalRoutingKeyConverter> converters) {
        return new EndpointRouter(endpointRegistry, topicRegistry, sseProperties, filters, converters);
    }

    /**
     * IM 管理控制器 Bean.
     * <p>
     * 提供运行时查询 API（端点、Topic、连接、统计信息）。
     * </p>
     */
    @Bean
    @ConditionalOnMissingBean
    public ImAdminController imAdminController(EndpointRegistry endpointRegistry,
                                                SseConnectionManager connectionManager,
                                                TopicRegistry topicRegistry,
                                                @Autowired(required = false) SseMetrics metrics) {
        return new ImAdminController(endpointRegistry, connectionManager, topicRegistry, metrics);
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
                // 监听器 TopicFactory.onEndpointRegistered 已触发 ensureResources，无需重复调用
            } catch (Exception e) {
                log.error("[EndpointAutoConfiguration] 加载端点失败: {}", props, e);
            }
        }
    }
}
