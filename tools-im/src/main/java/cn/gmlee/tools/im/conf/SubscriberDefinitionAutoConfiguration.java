package cn.gmlee.tools.im.conf;

import cn.gmlee.tools.im.core.BindingNames;
import cn.gmlee.tools.im.core.Subscriber;
import cn.gmlee.tools.im.definition.TopicDefinition;
import cn.gmlee.tools.im.sse.SseConnectionManager;
import cn.gmlee.tools.im.definition.SubscriberDefinition;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.cloud.stream.config.BindingServiceProperties;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.function.Consumer;

/**
 * 订阅者定义自动注册配置.
 * <p>
 * 扫描所有 {@link SubscriberDefinition} Bean，自动创建并注册对应的 Consumer 和 Subscriber Bean，
 * 并自动配置 Spring Cloud Stream 的输入 binding。
 * </p>
 *
 * <h3>处理范围</h3>
 * <ul>
 *   <li>直接实现 {@link SubscriberDefinition} 的 Bean</li>
 *   <li>实现 {@link TopicDefinition} 的 Bean（因为它继承了 SubscriberDefinition）</li>
 * </ul>
 *
 * <h3>自动 Binding 配置</h3>
 * <p>
 * 对于每个 {@link SubscriberDefinition}，自动创建输入 binding 配置：
 * </p>
 * <ul>
 *   <li>Binding 名称：{@link cn.gmlee.tools.im.core.BindingNames#inputBinding(String)} 生成（与 Consumer Bean 名称对应）</li>
 *   <li>Destination：与 topic 同名</li>
 *   <li>Group：{@link SubscriberDefinition#group()}（默认为 null，使用应用名称）</li>
 * </ul>
 *
 * @see SubscriberDefinition
 * @see PublisherDefinitionAutoConfiguration
 */
@SuppressWarnings({"rawtypes", "unchecked"})
@Slf4j
@Configuration
@AutoConfiguration(after = ImAutoConfiguration.class)
@ConditionalOnBean(SubscriberDefinition.class)
public class SubscriberDefinitionAutoConfiguration {

    private final List<SubscriberDefinition> subscriberDefinitions;
    private final SseConnectionManager sseConnectionManager;
    private final BindingServiceProperties bindingServiceProperties;
    private final ConfigurableListableBeanFactory beanFactory;
    private final BeanDefinitionRegistry registry;

    public SubscriberDefinitionAutoConfiguration(
            List<SubscriberDefinition> subscriberDefinitions,
            SseConnectionManager sseConnectionManager,
            BindingServiceProperties bindingServiceProperties,
            ConfigurableListableBeanFactory beanFactory,
            BeanDefinitionRegistry registry) {
        this.subscriberDefinitions = subscriberDefinitions;
        this.sseConnectionManager = sseConnectionManager;
        this.bindingServiceProperties = bindingServiceProperties;
        this.beanFactory = beanFactory;
        this.registry = registry;
    }

    @PostConstruct
    public void registerSubscriberComponents() {
        for (SubscriberDefinition definition : subscriberDefinitions) {
            String topic = definition.topic();
            String group = definition.group();
            log.info("[SubscriberDefinition] 注册订阅者主题: {}, group: {}", topic, group);
            registerBinding(topic, group);
            registerConsumer(definition, topic);
            registerSubscriber(definition, topic);
        }
    }

    private void registerBinding(String topic, String group) {
        // 输入 binding 名称由 Consumer Bean 名称派生（{beanName}-in-0）
        String bindingName = BindingNames.inputBinding(topic);

        // 检查是否已配置
        if (bindingServiceProperties.getBindings().containsKey(bindingName)) {
            log.debug("[SubscriberDefinition] Binding 已存在，跳过: {}", bindingName);
            return;
        }

        // 创建 binding 配置，destination 与 topic 同名
        var bindingProperties = new org.springframework.cloud.stream.config.BindingProperties();
        bindingProperties.setDestination(topic);
        if (group != null && !group.isEmpty()) {
            bindingProperties.setGroup(group);
        }
        bindingServiceProperties.getBindings().put(bindingName, bindingProperties);

        log.debug("[SubscriberDefinition] 注册 Binding: {} -> destination={}, group={}", bindingName, topic, group);
    }

    private void registerConsumer(SubscriberDefinition definition, String topic) {
        Consumer consumer = definition.createConsumer(sseConnectionManager);

        String beanName = BindingNames.consumerBean(topic);

        // 检查是否已注册（避免与 TopicDefinition 重复）
        if (registry.containsBeanDefinition(beanName)) {
            log.debug("[SubscriberDefinition] Consumer 已存在，跳过: {}", beanName);
            return;
        }

        GenericBeanDefinition beanDef = new GenericBeanDefinition();
        beanDef.setBeanClass(consumer.getClass());
        beanDef.setInstanceSupplier(() -> consumer);
        registry.registerBeanDefinition(beanName, beanDef);

        log.debug("[SubscriberDefinition] 注册 Consumer: {} -> {}", topic, beanName);
    }

    private void registerSubscriber(SubscriberDefinition definition, String topic) {
        Subscriber subscriber = definition.createSubscriber(sseConnectionManager);

        String beanName = BindingNames.subscriberBean(topic);

        // 检查是否已注册（避免与 TopicDefinition 重复）
        if (registry.containsBeanDefinition(beanName)) {
            log.debug("[SubscriberDefinition] Subscriber 已存在，跳过: {}", beanName);
            return;
        }

        GenericBeanDefinition beanDef = new GenericBeanDefinition();
        beanDef.setBeanClass(subscriber.getClass());
        beanDef.setInstanceSupplier(() -> subscriber);
        registry.registerBeanDefinition(beanName, beanDef);

        log.debug("[SubscriberDefinition] 注册 Subscriber: {} -> {}", topic, beanName);
    }
}
