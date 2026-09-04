package cn.gmlee.tools.im.conf;

import cn.gmlee.tools.im.model.DeploymentMode;
import cn.gmlee.tools.im.resume.EventIdGenerator;
import cn.gmlee.tools.im.resume.ResumeSupport;
import cn.gmlee.tools.im.spi.factory.PublisherFactory;
import cn.gmlee.tools.im.spi.factory.RepeaterFactory;
import cn.gmlee.tools.im.spi.factory.SubscriberFactory;
import cn.gmlee.tools.im.spi.interceptor.RepeaterInterceptor;
import cn.gmlee.tools.im.spi.routing.RoutingKeyComposer;
import cn.gmlee.tools.im.sse.SseConnectionManager;
import cn.gmlee.tools.im.topic.StandaloneTopicResourceFactory;
import cn.gmlee.tools.im.topic.TopicRegistry;
import cn.gmlee.tools.im.topic.TopicResourceFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

import java.util.List;

/**
 * 单机模式自动配置.
 * <p>
 * STANDALONE 部署模式下的自动配置类，不依赖 Spring Cloud Stream 和 MQ。
 * </p>
 *
 * <h3>激活条件</h3>
 * <ul>
 *   <li>{@code im.mode=standalone}（必须显式配置）</li>
 * </ul>
 *
 * <h3>创建的 Bean</h3>
 * <ul>
 *   <li>{@link TopicRegistry} - 配置为 STANDALONE 模式，不注入 {@code MessageSender}</li>
 *   <li>{@link StandaloneTopicResourceFactory} - 不创建 Stream binding，仅确保 Repeater 创建</li>
 * </ul>
 *
 * <h3>与 CLUSTER 模式的差异</h3>
 * <table border="1">
 *   <tr>
 *     <th>特性</th>
 *     <th>CLUSTER 模式</th>
 *     <th>STANDALONE 模式</th>
 *   </tr>
 *   <tr>
 *     <td>MQ 依赖</td>
 *     <td>✅ 需要 RabbitMQ</td>
 *     <td>❌ 不需要</td>
 *   </tr>
 *   <tr>
 *     <td>MessageSender</td>
 *     <td>✅ 注入（由 StreamBridge 适配）</td>
 *     <td>❌ 不注入（null）</td>
 *   </tr>
 *   <tr>
 *     <td>TopicResourceFactory</td>
 *     <td>{@link cn.gmlee.tools.im.topic.ClusterTopicResourceFactory}</td>
 *     <td>{@link StandaloneTopicResourceFactory}</td>
 *   </tr>
 *   <tr>
 *     <td>消息延迟</td>
 *     <td>~1-5ms（MQ 网络开销）</td>
 *     <td>&lt;0.1ms（直接调用）</td>
 *   </tr>
 * </table>
 *
 * <h3>使用场景</h3>
 * <ul>
 *   <li>单机部署，无需集群支持</li>
 *   <li>开发/测试环境，简化依赖</li>
 *   <li>对延迟极度敏感的场景</li>
 * </ul>
 *
 * @since 5.6.0
 * @see ClusterAutoConfiguration
 * @see DeploymentMode#STANDALONE
 */
@Slf4j
@AutoConfiguration(after = ImAutoConfiguration.class)
@ConditionalOnProperty(name = "im.mode", havingValue = "standalone")
public class StandaloneAutoConfiguration {

    /**
     * 创建单机模式的 TopicRegistry.
     * <p>
     * 配置为 STANDALONE 模式，不注入 {@code MessageSender}（传 null）。
     * </p>
     *
     * @param publisherFactories   Publisher 工厂列表
     * @param repeaterFactories    Repeater 工厂列表
     * @param subscriberFactories  Subscriber 工厂列表
     * @param sseConnectionManager SSE 连接管理器
     * @param interceptors         Repeater 拦截器列表
     * @param sseProperties        SSE 配置
     * @param composer             路由键组合器
     * @param imProperties         IM 配置
     * @return TopicRegistry 实例
     */
    @Bean
    @ConditionalOnMissingBean
    public TopicRegistry topicRegistry(
            @Autowired(required = false) List<PublisherFactory> publisherFactories,
            @Autowired(required = false) List<RepeaterFactory> repeaterFactories,
            @Autowired(required = false) List<SubscriberFactory> subscriberFactories,
            SseConnectionManager sseConnectionManager,
            @Autowired(required = false) List<RepeaterInterceptor> interceptors,
            SseProperties sseProperties,
            @Autowired(required = false) RoutingKeyComposer composer,
            ImProperties imProperties,
            ResumeSupport resumeSupport,
            EventIdGenerator eventIdGenerator) {

        log.info("[StandaloneAutoConfiguration] 创建 STANDALONE 模式 TopicRegistry（无 MQ）");
        return new TopicRegistry(
                publisherFactories,
                repeaterFactories,
                subscriberFactories,
                null,  // MessageSender = null（STANDALONE 模式不需要 MQ 传输）
                sseConnectionManager,
                interceptors,
                sseProperties,
                composer,
                null,  // objectMapper
                DeploymentMode.STANDALONE,
                resumeSupport,
                eventIdGenerator
        );
    }

    /**
     * 创建单机模式的 TopicResourceFactory.
     * <p>
     * 使用 {@link StandaloneTopicResourceFactory}，不创建 Stream binding。
     * </p>
     *
     * @param topicRegistry Topic 组件注册表
     * @return TopicResourceFactory 实例
     */
    @Bean
    @ConditionalOnMissingBean(TopicResourceFactory.class)
    public TopicResourceFactory standaloneTopicResourceFactory(TopicRegistry topicRegistry) {
        log.info("[StandaloneAutoConfiguration] 创建 StandaloneTopicResourceFactory（无 MQ binding）");
        return new StandaloneTopicResourceFactory(topicRegistry);
    }
}
