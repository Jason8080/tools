package cn.gmlee.tools.im.conf;

import cn.gmlee.tools.im.core.Msg;
import cn.gmlee.tools.im.core.Subscriber;
import cn.gmlee.tools.im.core.TopicMessage;
import cn.gmlee.tools.im.definition.TopicDefinition;
import cn.gmlee.tools.im.sse.SseConnectionManager;
import cn.gmlee.tools.im.definition.SubscriberDefinition;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Flux;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.function.Consumer;

/**
 * 订阅者定义自动注册配置.
 * <p>
 * 扫描所有 {@link SubscriberDefinition} Bean，自动创建并注册对应的 Consumer 和 Subscriber Bean。
 * 支持微服务场景下的拉取端服务独立部署。
 * </p>
 *
 * <h3>处理范围</h3>
 * <ul>
 *   <li>直接实现 {@link SubscriberDefinition} 的 Bean</li>
 *   <li>实现 {@link TopicDefinition} 的 Bean（因为它继承了 SubscriberDefinition）</li>
 * </ul>
 *
 * @see SubscriberDefinition
 * @see PublisherDefinitionAutoConfiguration
 */
@Slf4j
@Configuration
@AutoConfiguration(after = ImAutoConfiguration.class)
@ConditionalOnBean(SubscriberDefinition.class)
public class SubscriberDefinitionAutoConfiguration {

    private final List<SubscriberDefinition> subscriberDefinitions;
    private final SseConnectionManager sseConnectionManager;
    private final ConfigurableListableBeanFactory beanFactory;
    private final BeanDefinitionRegistry registry;

    public SubscriberDefinitionAutoConfiguration(
            List<SubscriberDefinition> subscriberDefinitions,
            SseConnectionManager sseConnectionManager,
            ConfigurableListableBeanFactory beanFactory,
            BeanDefinitionRegistry registry) {
        this.subscriberDefinitions = subscriberDefinitions;
        this.sseConnectionManager = sseConnectionManager;
        this.beanFactory = beanFactory;
        this.registry = registry;
    }

    @PostConstruct
    public void registerSubscriberComponents() {
        for (SubscriberDefinition definition : subscriberDefinitions) {
            String topic = definition.topic();
            log.info("[SubscriberDefinition] 注册订阅者主题: {}", topic);
            registerConsumer(definition, topic);
            registerSubscriber(definition, topic);
        }
    }

    private void registerConsumer(SubscriberDefinition definition, String topic) {
        Consumer<TopicMessage<Msg>> consumer = definition.createConsumer(sseConnectionManager);

        String beanName = topic + ".consumer";

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
        Subscriber<Flux<Msg>> subscriber = definition.createSubscriber(sseConnectionManager);

        String beanName = topic + ".subscriber";

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
