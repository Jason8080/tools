package cn.gmlee.tools.im.conf;

import cn.gmlee.tools.im.core.MessageSender;
import cn.gmlee.tools.im.model.DeploymentMode;
import cn.gmlee.tools.im.resume.EventIdGenerator;
import cn.gmlee.tools.im.resume.MessageHistoryStore;
import cn.gmlee.tools.im.resume.ResumeSupport;
import cn.gmlee.tools.im.spi.factory.PublisherFactory;
import cn.gmlee.tools.im.spi.factory.RepeaterFactory;
import cn.gmlee.tools.im.spi.factory.SubscriberFactory;
import cn.gmlee.tools.im.spi.interceptor.RepeaterInterceptor;
import cn.gmlee.tools.im.spi.routing.RoutingKeyComposer;
import cn.gmlee.tools.im.sse.SseConnectionManager;
import cn.gmlee.tools.im.topic.ClusterTopicResourceFactory;
import cn.gmlee.tools.im.topic.TopicRegistry;
import cn.gmlee.tools.im.topic.TopicResourceFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.stream.config.BindingServiceProperties;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;

import java.util.List;

/**
 * 集群模式自动配置.
 * <p>
 * CLUSTER 部署模式下的自动配置类，创建依赖 Spring Cloud Stream 的组件。
 * </p>
 *
 * <h3>激活条件</h3>
 * <ul>
 *   <li>{@code im.mode=cluster}（默认值，可通过 {@code matchIfMissing=true} 省略）</li>
 *   <li>classpath 中存在 {@link StreamBridge} 和 {@link BindingServiceProperties}</li>
 * </ul>
 *
 * <h3>创建的 Bean</h3>
 * <ul>
 *   <li>{@link TopicRegistry} - 配置为 CLUSTER 模式，通过 {@link MessageSender} 发送消息</li>
 *   <li>{@link ClusterTopicResourceFactory} - 创建 Spring Cloud Stream binding</li>
 * </ul>
 *
 * <h3>与 STANDALONE 模式的差异</h3>
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
 *     <td>❌ 不注入</td>
 *   </tr>
 *   <tr>
 *     <td>TopicResourceFactory</td>
 *     <td>{@link ClusterTopicResourceFactory}</td>
 *     <td>{@link cn.gmlee.tools.im.topic.StandaloneTopicResourceFactory}</td>
 *   </tr>
 * </table>
 *
 * @since 5.6.0
 * @see StandaloneAutoConfiguration
 * @see DeploymentMode#CLUSTER
 */
@Slf4j
@AutoConfiguration(after = ImAutoConfiguration.class)
@ConditionalOnProperty(name = "im.mode", havingValue = "cluster", matchIfMissing = true)
@ConditionalOnClass({StreamBridge.class, BindingServiceProperties.class})
public class ClusterAutoConfiguration {

    /**
     * 创建集群模式的 TopicRegistry.
     * <p>
     * 配置为 CLUSTER 模式，将 {@link StreamBridge} 适配为 {@link MessageSender} 用于发送消息到 MQ。
     * </p>
     *
     * @param publisherFactories   Publisher 工厂列表
     * @param repeaterFactories    Repeater 工厂列表
     * @param subscriberFactories  Subscriber 工厂列表
     * @param streamBridge         Stream 桥接器（CLUSTER 模式必需）
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
            StreamBridge streamBridge,
            SseConnectionManager sseConnectionManager,
            @Autowired(required = false) List<RepeaterInterceptor> interceptors,
            SseProperties sseProperties,
            @Autowired(required = false) RoutingKeyComposer composer,
            ImProperties imProperties,
            ResumeSupport resumeSupport,
            EventIdGenerator eventIdGenerator,
            @Autowired(required = false) List<MessageHistoryStore> historyStores) {

        log.info("[ClusterAutoConfiguration] 创建 CLUSTER 模式 TopicRegistry");

        // CLUSTER + 续传场景要求全局有序 ID：注册了历史存储但未启用雪花算法时告警
        if (historyStores != null && !historyStores.isEmpty()
                && sseProperties.getResume().isEnabled()
                && !sseProperties.getResume().getSnowflake().isEnabled()) {
            log.warn("[ClusterAutoConfiguration] 检测到 CLUSTER 模式已注册 {} 个历史存储，"
                    + "但未启用雪花算法 ID（im.sse.resume.snowflake.enabled=false）。"
                    + "各实例自增序列交叉将导致断点续传回放乱序，请显式开启雪花算法"
                    + "并为每个实例配置唯一 worker-id", historyStores.size());
        }

        MessageSender messageSender = streamBridge::send;
        return new TopicRegistry(
                publisherFactories,
                repeaterFactories,
                subscriberFactories,
                messageSender,
                sseConnectionManager,
                interceptors,
                sseProperties,
                composer,
                null,  // objectMapper
                DeploymentMode.CLUSTER,
                resumeSupport,
                eventIdGenerator
        );
    }

    /**
     * 创建集群模式的 TopicResourceFactory.
     * <p>
     * 使用 {@link ClusterTopicResourceFactory} 创建 Spring Cloud Stream binding。
     * </p>
     *
     * @param bindingServiceProperties Stream binding 配置
     * @param beanDefinitionRegistry   Bean 定义注册表
     * @param topicRegistry            Topic 组件注册表
     * @return TopicResourceFactory 实例
     */
    @Bean
    @ConditionalOnMissingBean(TopicResourceFactory.class)
    public TopicResourceFactory clusterTopicResourceFactory(
            BindingServiceProperties bindingServiceProperties,
            BeanDefinitionRegistry beanDefinitionRegistry,
            TopicRegistry topicRegistry) {

        log.info("[ClusterAutoConfiguration] 创建 ClusterTopicResourceFactory");
        return new ClusterTopicResourceFactory(
                bindingServiceProperties,
                beanDefinitionRegistry,
                topicRegistry
        );
    }
}
