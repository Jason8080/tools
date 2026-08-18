package cn.gmlee.tools.im.topic;

import cn.gmlee.tools.im.conf.EndpointProperties;
import cn.gmlee.tools.im.util.BindingNames;
import cn.gmlee.tools.im.model.EndpointMode;
import cn.gmlee.tools.im.core.Repeater;
import cn.gmlee.tools.im.endpoint.EndpointRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.cloud.stream.config.BindingProperties;
import org.springframework.cloud.stream.config.BindingServiceProperties;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 集群模式 Topic 资源工厂.
 * <p>
 * CLUSTER 部署模式下的 {@link TopicResourceFactory} 实现。
 * 监听 {@link EndpointRegistry} 的变更事件，按需为每个 Topic 创建 Spring Cloud Stream 资源：
 * </p>
 * <ul>
 *   <li><b>PUSH 端点</b>：注册输出 binding（{@code {topic}-out-0}）</li>
 *   <li><b>PULL 端点</b>：注册输入 binding（{@code {topic}.consumer-in-0}）+ Consumer Bean</li>
 * </ul>
 *
 * <h3>Consumer binding 启动</h3>
 * <p>
 * 不手动调用 {@code BindingService.bindConsumer()}（该 API 不适用于函数式编程模型，
 * 会把 Consumer 类名误识别为 binder 名称）。
 * 只需注册 Consumer Bean 和 binding 配置，Spring Cloud Stream 的自动发现机制
 * 会在上下文初始化时自动创建 binding。
 * </p>
 *
 * <h3>幂等保证</h3>
 * <p>
 * 同一 Topic 的相同资源只创建一次。多个端点共享同一 Topic 时（如同一 Topic 既有 PUSH 又有 PULL），
 * 各自的资源独立创建、互不干扰。
 * </p>
 *
 * <h3>SPI 行为一致性</h3>
 * <p>
 * 除创建 Stream binding 外，其他行为与 {@link StandaloneTopicResourceFactory} 完全一致：
 * </p>
 * <ul>
 *   <li>✅ 监听端点注册/注销事件</li>
 *   <li>✅ 确保 Repeater 组件创建</li>
 *   <li>✅ 幂等性保证</li>
 * </ul>
 *
 * @since 5.6.0
 * @see TopicResourceFactory
 * @see StandaloneTopicResourceFactory
 * @see cn.gmlee.tools.im.conf.DeploymentMode#CLUSTER
 */
@Slf4j
@RequiredArgsConstructor
public class ClusterTopicResourceFactory implements TopicResourceFactory {

    private final BindingServiceProperties bindingServiceProperties;
    private final BeanDefinitionRegistry beanDefinitionRegistry;
    private final TopicRegistry topicRegistry;

    /**
     * 已创建输出 binding 的 Topic 集合
     */
    private final Set<String> outputBindingTopics = ConcurrentHashMap.newKeySet();

    /**
     * 已创建输入 binding + Consumer Bean 的 Topic 集合
     */
    private final Set<String> inputBindingTopics = ConcurrentHashMap.newKeySet();

    /**
     * 确保 Topic 的输出 binding 已创建.
     * <p>
     * 幂等：同一 Topic 多次调用只创建一次。
     * </p>
     *
     * @param topic Topic 名称
     */
    public void ensureOutputBinding(String topic) {
        if (!outputBindingTopics.add(topic)) {
            return;
        }
        String bindingName = BindingNames.outputBinding(topic);
        if (bindingServiceProperties.getBindings().containsKey(bindingName)) {
            log.debug("[ClusterTopicResourceFactory] 输出 binding 已存在，跳过: {}", bindingName);
            return;
        }
        BindingProperties props = new BindingProperties();
        props.setDestination(topic);
        bindingServiceProperties.getBindings().put(bindingName, props);
        log.info("[ClusterTopicResourceFactory] 注册输出 binding: {} → destination={}", bindingName, topic);
    }

    /**
     * 确保 Topic 的输入 binding 和 Consumer Bean 已创建.
     * <p>
     * 幂等：同一 Topic 多次调用只创建一次。Consumer Bean 为 {@link ConsumerBridge}，
     * 内部委托 {@link Repeater} 处理消息（桥接泛型 Repeater 与 SSE 管道的
     * {@code Consumer<TopicMessage<Msg>>} 接口）。
     * </p>
     * <p>
     * <b>注意</b>：不手动调用 {@code BindingService.bindConsumer()}。
     * Spring Cloud Stream 在上下文初始化时会自动发现注册的 Consumer Bean 并创建 binding。
     * </p>
     *
     * @param topic Topic 名称
     */
    public void ensureInputBinding(String topic) {
        if (!inputBindingTopics.add(topic)) {
            return;
        }
        topicRegistry.ensureRepeater(topic);
        registerInputBinding(topic);
        registerConsumerBean(topic);
        // Spring Cloud Stream 自动发现 Consumer Bean 并创建 binding，无需手动调用 bindConsumer()
    }

    // ==================== TopicResourceFactory 接口实现 ====================

    /**
     * 根据端点模式确保资源已创建.
     *
     * @param props 端点配置
     */
    @Override
    public void ensureResources(EndpointProperties props) {
        if (props.getMode() == EndpointMode.PUSH) {
            ensureOutputBinding(props.getTopic());
        } else if (props.getMode() == EndpointMode.PULL) {
            ensureInputBinding(props.getTopic());
        }
    }

    @Override
    public void onEndpointRegistered(EndpointProperties props) {
        ensureResources(props);
    }

    @Override
    public void onEndpointUnregistered(EndpointProperties props) {
        // 资源不随端点注销而销毁，避免影响其他共享同一 Topic 的端点。
        // Sink 由 SseConnectionManager 的空 Topic TTL 机制自动清理。
        log.debug("[ClusterTopicResourceFactory] 端点注销，保留 Topic 资源: topic={}", props.getTopic());
    }

    @Override
    public void cleanupResources(String topic) {
        cleanupTopicResources(topic);
    }

    @Override
    public boolean hasResources(String topic) {
        return inputBindingTopics.contains(topic) || outputBindingTopics.contains(topic);
    }

    // ==================== 内部方法 ====================

    private void registerInputBinding(String topic) {
        String bindingName = BindingNames.inputBinding(topic);
        if (bindingServiceProperties.getBindings().containsKey(bindingName)) {
            log.debug("[ClusterTopicResourceFactory] 输入 binding 已存在，跳过: {}", bindingName);
            return;
        }
        BindingProperties props = new BindingProperties();
        props.setDestination(topic);
        bindingServiceProperties.getBindings().put(bindingName, props);
        log.info("[ClusterTopicResourceFactory] 注册输入 binding: {} → destination={}", bindingName, topic);
    }

    private void registerConsumerBean(String topic) {
        String beanName = BindingNames.consumerBean(topic);
        if (beanDefinitionRegistry.containsBeanDefinition(beanName)) {
            log.debug("[ClusterTopicResourceFactory] Consumer Bean 已存在，跳过: {}", beanName);
            return;
        }

        // ConsumerBridge 实现 Consumer<TopicMessage<Msg>>，桥接泛型 Repeater 与 SSE 管道。
        // 使用非泛型类保证 Spring Cloud Stream 能通过 GenericTypeResolver 正确解析类型参数。
        ConsumerBridge bridge = topicRegistry.createConsumerBridge(topic);

        GenericBeanDefinition beanDef = new GenericBeanDefinition();
        beanDef.setBeanClass(ConsumerBridge.class);
        beanDef.setInstanceSupplier(() -> bridge);
        beanDefinitionRegistry.registerBeanDefinition(beanName, beanDef);
        log.info("[ClusterTopicResourceFactory] 注册 Consumer Bean: {} → ConsumerBridge", beanName);
    }

    // ==================== 生命周期管理（供 TopicLifecycleManager 调用） ====================

    /**
     * 清理 Topic 的物理资源（Spring Cloud Stream binding）.
     * <p>
     * 执行完整的资源清理流程：
     * </p>
     * <ol>
     *   <li>移除 binding 配置</li>
     *   <li>移除 Consumer Bean 定义</li>
     *   <li>清理内部记录</li>
     * </ol>
     *
     * <h3>注意</h3>
     * <p>
     * 此方法不会检查引用计数，调用方需确保 Topic 不再被使用。
     * 并发安全：使用 {@code remove()} 原子操作。
     * </p>
     *
     * <h3>Spring Cloud Stream 限制</h3>
     * <p>
     * Spring Cloud Stream 的 {@code BindingService} 不提供 {@code unbind()} API，
     * 已创建的 Consumer/Producer binding 无法通过编程方式销毁。
     * 因此此方法仅清理配置和 Bean 定义，实际的 binding 连接会保留到应用重启。
     * 这是 Spring Cloud Stream 的设计限制，适用于大多数场景（Topic 通常是长期存在的）。
     * </p>
     *
     * @param topic Topic 名称
     */
    public void cleanupTopicResources(String topic) {
        String inputBindingName = BindingNames.inputBinding(topic);
        String outputBindingName = BindingNames.outputBinding(topic);
        String beanName = BindingNames.consumerBean(topic);

        // 1. 清理输入 binding 相关资源
        if (inputBindingTopics.remove(topic)) {
            // 移除 binding 配置
            bindingServiceProperties.getBindings().remove(inputBindingName);

            // 移除 Bean 定义
            if (beanDefinitionRegistry.containsBeanDefinition(beanName)) {
                try {
                    beanDefinitionRegistry.removeBeanDefinition(beanName);
                    log.debug("[ClusterTopicResourceFactory] 移除 Consumer Bean: {}", beanName);
                } catch (Exception e) {
                    log.warn("[ClusterTopicResourceFactory] 移除 Consumer Bean 失败: {}", beanName, e);
                }
            }

            log.debug("[ClusterTopicResourceFactory] 清理 input binding 配置: {}", inputBindingName);
        }

        // 2. 清理输出 binding 相关资源
        if (outputBindingTopics.remove(topic)) {
            // 移除 binding 配置
            bindingServiceProperties.getBindings().remove(outputBindingName);
            log.debug("[ClusterTopicResourceFactory] 清理 output binding 配置: {}", outputBindingName);
        }

        log.info("[ClusterTopicResourceFactory] Topic 物理资源已清理: topic={}", topic);
    }
}
