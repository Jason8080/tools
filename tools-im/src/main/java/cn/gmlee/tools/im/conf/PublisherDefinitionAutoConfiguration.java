package cn.gmlee.tools.im.conf;

import cn.gmlee.tools.im.core.Publisher;
import cn.gmlee.tools.im.definition.PublisherDefinition;
import cn.gmlee.tools.im.definition.TopicDefinition;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.cloud.stream.config.BindingServiceProperties;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.util.List;

/**
 * 发布者定义自动注册配置.
 * <p>
 * 扫描所有 {@link PublisherDefinition} Bean，自动创建并注册对应的 Publisher Bean，
 * 并自动配置 Spring Cloud Stream 的输出 binding。
 * </p>
 *
 * <h3>处理范围</h3>
 * <ul>
 *   <li>直接实现 {@link PublisherDefinition} 的 Bean</li>
 *   <li>实现 {@link TopicDefinition} 的 Bean（因为它继承了 PublisherDefinition）</li>
 * </ul>
 *
 * <h3>自动 Binding 配置</h3>
 * <p>
 * 对于每个 {@link PublisherDefinition}，自动创建输出 binding 配置：
 * </p>
 * <ul>
 *   <li>Binding 名称：{@code {topic}-out-0}</li>
 *   <li>Destination：与 topic 同名</li>
 * </ul>
 *
 * @see PublisherDefinition
 * @see SubscriberDefinitionAutoConfiguration
 */
@SuppressWarnings({"rawtypes", "unchecked"})
@Slf4j
@Configuration
@AutoConfiguration(after = ImAutoConfiguration.class)
@ConditionalOnBean(PublisherDefinition.class)
public class PublisherDefinitionAutoConfiguration {

    private final List<PublisherDefinition> publisherDefinitions;
    private final StreamBridge streamBridge;
    private final BindingServiceProperties bindingServiceProperties;
    private final ConfigurableListableBeanFactory beanFactory;
    private final BeanDefinitionRegistry registry;

    public PublisherDefinitionAutoConfiguration(
            List<PublisherDefinition> publisherDefinitions,
            StreamBridge streamBridge,
            BindingServiceProperties bindingServiceProperties,
            ConfigurableListableBeanFactory beanFactory,
            BeanDefinitionRegistry registry) {
        this.publisherDefinitions = publisherDefinitions;
        this.streamBridge = streamBridge;
        this.bindingServiceProperties = bindingServiceProperties;
        this.beanFactory = beanFactory;
        this.registry = registry;
    }

    @PostConstruct
    public void registerPublisherComponents() {
        for (PublisherDefinition definition : publisherDefinitions) {
            String topic = definition.topic();
            log.info("[PublisherDefinition] 注册发布者主题: {}", topic);
            registerBinding(topic);
            registerPublisher(definition, topic);
        }
    }

    private void registerBinding(String topic) {
        String bindingName = topic + "-out-0";

        // 检查是否已配置
        if (bindingServiceProperties.getBindings().containsKey(bindingName)) {
            log.debug("[PublisherDefinition] Binding 已存在，跳过: {}", bindingName);
            return;
        }

        // 创建 binding 配置，destination 与 topic 同名
        var bindingProperties = new org.springframework.cloud.stream.config.BindingProperties();
        bindingProperties.setDestination(topic);
        bindingServiceProperties.getBindings().put(bindingName, bindingProperties);

        log.debug("[PublisherDefinition] 注册 Binding: {} -> destination={}", bindingName, topic);
    }

    private void registerPublisher(PublisherDefinition definition, String topic) {
        Publisher publisher = definition.createPublisher(streamBridge);

        String beanName = topic + ".publisher";

        // 检查是否已注册（避免与 TopicDefinition 重复）
        if (registry.containsBeanDefinition(beanName)) {
            log.debug("[PublisherDefinition] Publisher 已存在，跳过: {}", beanName);
            return;
        }

        GenericBeanDefinition beanDef = new GenericBeanDefinition();
        beanDef.setBeanClass(publisher.getClass());
        beanDef.setInstanceSupplier(() -> publisher);
        registry.registerBeanDefinition(beanName, beanDef);

        log.debug("[PublisherDefinition] 注册 Publisher: {} -> {}", topic, beanName);
    }
}
