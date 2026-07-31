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
            // 回滚：移除已注册的资源
            inputBindingTopics.remove(topic);
            throw new RuntimeException("启动 Consumer binding 失败: " + bindingName, e);
        }
    }
}
