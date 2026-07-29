package cn.gmlee.tools.im.conf;

import cn.gmlee.tools.im.core.Msg;
import cn.gmlee.tools.im.core.Publisher;
import cn.gmlee.tools.im.core.Subscriber;
import cn.gmlee.tools.im.core.TopicMessage;
import cn.gmlee.tools.im.sse.SseConnectionManager;
import cn.gmlee.tools.im.stream.TopicDefinition;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Flux;

import jakarta.annotation.PostConstruct;
import java.io.Serializable;
import java.util.List;
import java.util.function.Consumer;

/**
 * 主题定义自动注册配置.
 * <p>
 * 扫描所有 {@link TopicDefinition} Bean，自动创建并注册对应的 Publisher/Consumer/Subscriber Bean。
 * 实现零样板代码的 Topic 定义方式。
 * </p>
 */
@Slf4j
@Configuration
@AutoConfiguration(after = ImAutoConfiguration.class)
@ConditionalOnBean(TopicDefinition.class)
public class TopicDefinitionAutoConfiguration {

    private final List<TopicDefinition> topicDefinitions;
    private final StreamBridge streamBridge;
    private final SseConnectionManager sseConnectionManager;
    private final ConfigurableListableBeanFactory beanFactory;
    private final BeanDefinitionRegistry registry;

    public TopicDefinitionAutoConfiguration(
            List<TopicDefinition> topicDefinitions,
            StreamBridge streamBridge,
            SseConnectionManager sseConnectionManager,
            ConfigurableListableBeanFactory beanFactory,
            BeanDefinitionRegistry registry) {
        this.topicDefinitions = topicDefinitions;
        this.streamBridge = streamBridge;
        this.sseConnectionManager = sseConnectionManager;
        this.beanFactory = beanFactory;
        this.registry = registry;
    }

    @PostConstruct
    public void registerTopicComponents() {
        for (TopicDefinition definition : topicDefinitions) {
            String topic = definition.topic();
            log.info("[TopicDefinition] 注册主题: {}", topic);

            // 注册 Publisher
            registerPublisher(definition, topic);

            // 注册 Consumer
            registerConsumer(definition, topic);

            // 注册 Subscriber
            registerSubscriber(definition, topic);
        }
    }

    private void registerPublisher(TopicDefinition definition, String topic) {
        Publisher<Serializable, Msg> publisher = definition.createPublisher(streamBridge);

        String beanName = topic + ".publisher";
        GenericBeanDefinition beanDef = new GenericBeanDefinition();
        beanDef.setBeanClass(publisher.getClass());
        beanDef.setInstanceSupplier(() -> publisher);
        registry.registerBeanDefinition(beanName, beanDef);

        log.debug("[TopicDefinition] 注册 Publisher: {} -> {}", topic, beanName);
    }

    private void registerConsumer(TopicDefinition definition, String topic) {
        Consumer<TopicMessage<Msg>> consumer = definition.createConsumer(sseConnectionManager);

        String beanName = topic + ".consumer";
        GenericBeanDefinition beanDef = new GenericBeanDefinition();
        beanDef.setBeanClass(consumer.getClass());
        beanDef.setInstanceSupplier(() -> consumer);
        registry.registerBeanDefinition(beanName, beanDef);

        log.debug("[TopicDefinition] 注册 Consumer: {} -> {}", topic, beanName);
    }

    private void registerSubscriber(TopicDefinition definition, String topic) {
        Subscriber<Flux<Msg>> subscriber = definition.createSubscriber(sseConnectionManager);

        String beanName = topic + ".subscriber";
        GenericBeanDefinition beanDef = new GenericBeanDefinition();
        beanDef.setBeanClass(subscriber.getClass());
        beanDef.setInstanceSupplier(() -> subscriber);
        registry.registerBeanDefinition(beanName, beanDef);

        log.debug("[TopicDefinition] 注册 Subscriber: {} -> {}", topic, beanName);
    }
}
