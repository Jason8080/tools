package cn.gmlee.tools.im.topic;

import cn.gmlee.tools.im.conf.EndpointProperties;
import cn.gmlee.tools.im.util.BindingNames;
import cn.gmlee.tools.im.model.EndpointMode;
import cn.gmlee.tools.im.core.Repeater;
import cn.gmlee.tools.im.endpoint.EndpointRegistry;
import cn.gmlee.tools.im.spi.listener.EndpointChangeListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.cloud.stream.binding.BindingService;
import org.springframework.cloud.stream.config.BindingProperties;
import org.springframework.cloud.stream.config.BindingServiceProperties;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Topic 资源工厂.
 * <p>
 * 监听 {@link EndpointRegistry} 的变更事件，按需为每个 Topic 创建 Stream 资源：
 * </p>
 * <ul>
 *   <li><b>PUSH 端点</b>：注册输出 binding（{@code {topic}-out-0}）</li>
 *   <li><b>PULL 端点</b>：注册输入 binding（{@code {topic}.consumer-in-0}）+ Consumer Bean</li>
 * </ul>
 *
 * <h3>幂等保证</h3>
 * <p>
 * 同一 Topic 的相同资源只创建一次。多个端点共享同一 Topic 时（如同一 Topic 既有 PUSH 又有 PULL），
 * 各自的资源独立创建、互不干扰。
 * </p>
 *
 * <h3>创建时机</h3>
 * <p>
 * YAML 配置的端点在启动时批量注册，TopicFactory 在 {@code @PostConstruct} 阶段为它们创建资源。
 * 运行时通过 API 注册的端点触发 {@link EndpointChangeListener} 回调，实时创建资源。
 * </p>
 *
 * @since 5.6.0
 */
@Slf4j
@RequiredArgsConstructor
public class TopicFactory implements EndpointChangeListener {

    private final BindingServiceProperties bindingServiceProperties;
    private final BindingService bindingService;
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
            log.debug("[TopicFactory] 输出 binding 已存在，跳过: {}", bindingName);
            return;
        }
        BindingProperties props = new BindingProperties();
        props.setDestination(topic);
        bindingServiceProperties.getBindings().put(bindingName, props);
        log.info("[TopicFactory] 注册输出 binding: {} → destination={}", bindingName, topic);
    }

    /**
     * 确保 Topic 的输入 binding 和 Consumer Bean 已创建.
     * <p>
     * 幂等：同一 Topic 多次调用只创建一次。Consumer Bean 为 {@link ConsumerBridge}，
     * 内部委托 {@link Repeater} 处理消息（桥接泛型 Repeater 与 SSE 管道的
     * {@code Consumer<TopicMessage<Msg>>} 接口）。
     * </p>
     * <p>
     * 运行时注册的端点会立即启动 Consumer binding，无需重启应用。
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

        // 运行时启动 Consumer binding（Spring Cloud Stream 不会自动发现运行时注册的 Bean）
        startConsumerBinding(topic);
    }

    /**
     * 根据端点模式确保资源已创建.
     *
     * @param props 端点配置
     */
    public void ensureResources(EndpointProperties props) {
        if (props.getMode() == EndpointMode.PUSH) {
            ensureOutputBinding(props.getTopic());
        } else if (props.getMode() == EndpointMode.PULL) {
            ensureInputBinding(props.getTopic());
        }
    }

    // ==================== EndpointChangeListener ====================

    @Override
    public void onEndpointRegistered(EndpointProperties props) {
        ensureResources(props);
    }

    @Override
    public void onEndpointUnregistered(EndpointProperties props) {
        // 资源不随端点注销而销毁，避免影响其他共享同一 Topic 的端点。
        // Sink 由 SseConnectionManager 的空 Topic TTL 机制自动清理。
        log.debug("[TopicFactory] 端点注销，保留 Topic 资源: topic={}", props.getTopic());
    }

    // ==================== 内部方法 ====================

    private void registerInputBinding(String topic) {
        String bindingName = BindingNames.inputBinding(topic);
        if (bindingServiceProperties.getBindings().containsKey(bindingName)) {
            log.debug("[TopicFactory] 输入 binding 已存在，跳过: {}", bindingName);
            return;
        }
        BindingProperties props = new BindingProperties();
        props.setDestination(topic);
        bindingServiceProperties.getBindings().put(bindingName, props);
        log.info("[TopicFactory] 注册输入 binding: {} → destination={}", bindingName, topic);
    }

    private void registerConsumerBean(String topic) {
        String beanName = BindingNames.consumerBean(topic);
        if (beanDefinitionRegistry.containsBeanDefinition(beanName)) {
            log.debug("[TopicFactory] Consumer Bean 已存在，跳过: {}", beanName);
            return;
        }

        // ConsumerBridge 实现 Consumer<TopicMessage<Msg>>，桥接泛型 Repeater 与 SSE 管道。
        // 使用非泛型类保证 Spring Cloud Stream 能通过 GenericTypeResolver 正确解析类型参数。
        ConsumerBridge bridge = topicRegistry.createConsumerBridge(topic);

        GenericBeanDefinition beanDef = new GenericBeanDefinition();
        beanDef.setBeanClass(ConsumerBridge.class);
        beanDef.setInstanceSupplier(() -> bridge);
        beanDefinitionRegistry.registerBeanDefinition(beanName, beanDef);
        log.info("[TopicFactory] 注册 Consumer Bean: {} → ConsumerBridge", beanName);
    }

    /**
     * 启动 Consumer binding（运行时动态注册场景）.
     * <p>
     * Spring Cloud Stream 在启动时自动发现 Consumer Bean 并创建 binding。
     * 但运行时注册的 Consumer Bean 不会被自动发现，需要手动调用 {@link BindingService} 启动。
     * </p>
     * <p>
     * 注意：此方法仅在运行时注册端点时调用。启动时注册的端点由 Spring Cloud Stream 自动处理。
     * </p>
     *
     * @param topic Topic 名称
     */
    private void startConsumerBinding(String topic) {
        String bindingName = BindingNames.inputBinding(topic);
        try {
            // 创建 ConsumerBridge 桥接泛型 Repeater 与 SSE 管道
            ConsumerBridge bridge = topicRegistry.createConsumerBridge(topic);
            // BindingService.bindConsumer 需要 Consumer 实例和 binding 名称
            bindingService.bindConsumer(bridge, bindingName);
            log.info("[TopicFactory] 启动 Consumer binding: {}", bindingName);
        } catch (Exception e) {
            log.error("[TopicFactory] 启动 Consumer binding 失败: {}", bindingName, e);
            // 完整回滚：清理已注册的资源
            rollbackInputBinding(topic);
            throw new RuntimeException("启动 Consumer binding 失败: " + bindingName, e);
        }
    }

    /**
     * 回滚输入 binding 注册（失败时调用）.
     *
     * @param topic Topic 名称
     */
    private void rollbackInputBinding(String topic) {
        inputBindingTopics.remove(topic);

        // 移除 Bean 定义
        String beanName = BindingNames.consumerBean(topic);
        if (beanDefinitionRegistry.containsBeanDefinition(beanName)) {
            try {
                beanDefinitionRegistry.removeBeanDefinition(beanName);
                log.debug("[TopicFactory] 回滚 Consumer Bean: {}", beanName);
            } catch (Exception e) {
                log.warn("[TopicFactory] 回滚 Consumer Bean 失败: {}", beanName, e);
            }
        }

        // 移除 binding 配置
        String bindingName = BindingNames.inputBinding(topic);
        bindingServiceProperties.getBindings().remove(bindingName);
        log.debug("[TopicFactory] 回滚 input binding: {}", bindingName);
    }

    // ==================== 生命周期管理（供 TopicLifecycleManager 调用） ====================

    /**
     * 清理 Topic 的物理资源（Spring Cloud Stream binding）.
     * <p>
     * 执行完整的资源清理流程：
     * <ol>
     *   <li>移除 binding 配置</li>
     *   <li>移除 Consumer Bean 定义</li>
     *   <li>清理内部记录</li>
     * </ol>
     * </p>
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
                    log.debug("[TopicFactory] 移除 Consumer Bean: {}", beanName);
                } catch (Exception e) {
                    log.warn("[TopicFactory] 移除 Consumer Bean 失败: {}", beanName, e);
                }
            }

            log.debug("[TopicFactory] 清理 input binding 配置: {}", inputBindingName);
        }

        // 2. 清理输出 binding 相关资源
        if (outputBindingTopics.remove(topic)) {
            // 移除 binding 配置
            bindingServiceProperties.getBindings().remove(outputBindingName);
            log.debug("[TopicFactory] 清理 output binding 配置: {}", outputBindingName);
        }

        log.info("[TopicFactory] Topic 物理资源已清理: topic={}", topic);
    }

    /**
     * 检查 Topic 是否存在物理资源.
     *
     * @param topic Topic 名称
     * @return 存在任意资源返回 true
     */
    public boolean hasResources(String topic) {
        return inputBindingTopics.contains(topic) || outputBindingTopics.contains(topic);
    }
}
