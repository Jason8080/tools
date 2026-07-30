package cn.gmlee.tools.im.conf;

import cn.gmlee.tools.im.core.Publisher;
import cn.gmlee.tools.im.core.Repeater;
import cn.gmlee.tools.im.core.Subscriber;
import cn.gmlee.tools.im.endpoint.EndpointRegistry;
import cn.gmlee.tools.im.endpoint.EndpointRouter;
import cn.gmlee.tools.im.sse.SseConnectionManager;
import cn.gmlee.tools.im.topic.TopicFactory;
import cn.gmlee.tools.im.topic.TopicRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
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

    @Autowired
    private TopicFactory topicFactory;

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
     * 端点注册表 Bean.
     */
    @Bean
    @ConditionalOnMissingBean
    public EndpointRegistry endpointRegistry() {
        return new EndpointRegistry();
    }

    /**
     * Topic 组件注册表 Bean.
     * <p>
     * 自动发现自定义 {@link Publisher}、{@link Repeater}、{@link Subscriber} 实现，
     * 并为未自定义的 Topic 创建默认实现。
     * </p>
     */
    @Bean
    @ConditionalOnMissingBean
    public TopicRegistry topicRegistry(
            @org.springframework.beans.factory.annotation.Autowired(required = false) List<Publisher> publishers,
            @org.springframework.beans.factory.annotation.Autowired(required = false) List<Repeater> repeaters,
            @org.springframework.beans.factory.annotation.Autowired(required = false) List<Subscriber> subscribers,
            StreamBridge streamBridge,
            SseConnectionManager sseConnectionManager) {
        return new TopicRegistry(publishers, repeaters, subscribers, streamBridge, sseConnectionManager);
    }

    /**
     * Topic 资源工厂 Bean.
     * <p>
     * 自动注册为 {@link EndpointRegistry} 的监听器，端点注册时按需创建 Stream 资源。
     * </p>
     */
    @Bean
    @ConditionalOnMissingBean
    public TopicFactory topicFactory(EndpointRegistry endpointRegistry,
                                      BindingServiceProperties bindingServiceProperties,
                                      TopicRegistry topicRegistry) {
        TopicFactory factory = new TopicFactory(
                bindingServiceProperties, beanDefinitionRegistry, topicRegistry);
        endpointRegistry.addListener(factory);
        return factory;
    }

    /**
     * 动态路由器 Bean.
     */
    @Bean
    @ConditionalOnMissingBean
    public EndpointRouter endpointRouter(EndpointRegistry endpointRegistry,
                                          TopicRegistry topicRegistry) {
        return new EndpointRouter(endpointRegistry, topicRegistry, sseProperties);
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
                topicFactory.ensureResources(props);
                log.info("[EndpointAutoConfiguration] 加载端点: {} → topic={}, mode={}",
                        props.getPath(), props.getTopic(), props.getMode());
            } catch (Exception e) {
                log.error("[EndpointAutoConfiguration] 加载端点失败: {}", props, e);
            }
        }
    }
}
