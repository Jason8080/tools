package cn.gmlee.tools.im.conf;

import cn.gmlee.tools.im.core.Msg;
import cn.gmlee.tools.im.core.Publisher;
import cn.gmlee.tools.im.definition.PublisherDefinition;
import cn.gmlee.tools.im.definition.TopicDefinition;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.io.Serializable;
import java.util.List;

/**
 * 发布者定义自动注册配置.
 * <p>
 * 扫描所有 {@link PublisherDefinition} Bean，自动创建并注册对应的 Publisher Bean。
 * 支持微服务场景下的发送端服务独立部署。
 * </p>
 *
 * <h3>处理范围</h3>
 * <ul>
 *   <li>直接实现 {@link PublisherDefinition} 的 Bean</li>
 *   <li>实现 {@link TopicDefinition} 的 Bean（因为它继承了 PublisherDefinition）</li>
 * </ul>
 *
 * @see PublisherDefinition
 * @see SubscriberDefinitionAutoConfiguration
 */
@Slf4j
@Configuration
@AutoConfiguration(after = ImAutoConfiguration.class)
@ConditionalOnBean(PublisherDefinition.class)
public class PublisherDefinitionAutoConfiguration {

    private final List<PublisherDefinition> publisherDefinitions;
    private final StreamBridge streamBridge;
    private final ConfigurableListableBeanFactory beanFactory;
    private final BeanDefinitionRegistry registry;

    public PublisherDefinitionAutoConfiguration(
            List<PublisherDefinition> publisherDefinitions,
            StreamBridge streamBridge,
            ConfigurableListableBeanFactory beanFactory,
            BeanDefinitionRegistry registry) {
        this.publisherDefinitions = publisherDefinitions;
        this.streamBridge = streamBridge;
        this.beanFactory = beanFactory;
        this.registry = registry;
    }

    @PostConstruct
    public void registerPublisherComponents() {
        for (PublisherDefinition definition : publisherDefinitions) {
            String topic = definition.topic();
            log.info("[PublisherDefinition] 注册发布者主题: {}", topic);
            registerPublisher(definition, topic);
        }
    }

    private void registerPublisher(PublisherDefinition definition, String topic) {
        Publisher<Serializable, Msg> publisher = definition.createPublisher(streamBridge);

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
