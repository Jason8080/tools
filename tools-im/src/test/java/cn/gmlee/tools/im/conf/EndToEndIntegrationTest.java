package cn.gmlee.tools.im.conf;

import cn.gmlee.tools.im.endpoint.EndpointRegistry;
import cn.gmlee.tools.im.model.EndpointMode;
import cn.gmlee.tools.im.resume.InMemoryMessageHistoryStore;
import cn.gmlee.tools.im.resume.MessageHistoryStore;
import cn.gmlee.tools.im.resume.ResumeSupport;
import cn.gmlee.tools.im.topic.TopicLifecycleListener;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.stream.binding.BindingService;
import org.springframework.cloud.stream.config.BindingServiceProperties;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.TestPropertySource;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 端到端集成测试.
 * <p>
 * 验证 YAML 配置 → 端点注册 → Stream 资源创建的完整链路（CLUSTER 模式）。
 * </p>
 */
@SpringBootTest(classes = {ImAutoConfiguration.class, ClusterAutoConfiguration.class,
        EndpointAutoConfiguration.class, EndToEndIntegrationTest.MockStreamBeans.class})
@TestPropertySource(properties = {
    "im.endpoints[0].path=/api/chat/pull",
    "im.endpoints[0].topic=im.chat",
    "im.endpoints[0].mode=pull",
    "im.endpoints[1].path=/api/chat/push",
    "im.endpoints[1].topic=im.chat",
    "im.endpoints[1].mode=push",
    "im.sse.resume.in-memory.enabled=true",
    "im.sse.resume.in-memory.capacity-per-topic=100",
    "spring.cloud.stream.bindings.im.chat-out-0.destination=im.chat",
    "spring.cloud.stream.bindings.im.chat.consumer-in-0.destination=im.chat"
})
class EndToEndIntegrationTest {

    /**
     * 模拟 Spring Cloud Stream 依赖 Bean.
     * <p>
     * 完整集成测试需要这些 Bean（由 spring-cloud-stream 自动配置提供），
     * 但本测试不使用真实 MQ，用 Mockito mock 替代。
     * </p>
     * <p>
     * 注：{@code BeanDefinitionRegistry} 无需在此提供——
     * {@code EndpointAutoConfiguration.Registrar} 已将其注册为
     * {@code imBeanDefinitionRegistry} Bean。
     * </p>
     */
    @TestConfiguration
    static class MockStreamBeans {
        @Bean
        public StreamBridge streamBridge() {
            return mock(StreamBridge.class);
        }

        @Bean
        public BindingService bindingService() {
            return mock(BindingService.class);
        }

        @Bean
        public BindingServiceProperties bindingServiceProperties() {
            BindingServiceProperties props = mock(BindingServiceProperties.class);
            when(props.getBindings()).thenReturn(new HashMap<>());
            return props;
        }
    }

    @Autowired
    private EndpointRegistry endpointRegistry;

    @Autowired
    private ImProperties imProperties;

    @Autowired
    private ResumeSupport resumeSupport;

    @Autowired
    private MessageHistoryStore messageHistoryStore;

    @Test
    @DisplayName("YAML 端点配置应自动加载")
    void testYamlEndpointsLoaded() {
        assertNotNull(imProperties.getEndpoints());
        assertEquals(2, imProperties.getEndpoints().size());
    }

    @Test
    @DisplayName("端点应自动注册到 EndpointRegistry")
    void testEndpointsRegistered() {
        assertNotNull(endpointRegistry);

        // 验证 PULL 端点
        EndpointProperties pullEndpoint = endpointRegistry.resolve("/api/chat/pull");
        assertNotNull(pullEndpoint);
        assertEquals("im.chat", pullEndpoint.getTopic());
        assertEquals(EndpointMode.PULL, pullEndpoint.getMode());

        // 验证 PUSH 端点
        EndpointProperties pushEndpoint = endpointRegistry.resolve("/api/chat/push");
        assertNotNull(pushEndpoint);
        assertEquals("im.chat", pushEndpoint.getTopic());
        assertEquals(EndpointMode.PUSH, pushEndpoint.getMode());
    }

    @Test
    @DisplayName("运行时动态注册端点")
    void testRuntimeEndpointRegistration() {
        EndpointProperties newEndpoint = new EndpointProperties();
        newEndpoint.setPath("/api/notify/stream");
        newEndpoint.setTopic("im.notify");
        newEndpoint.setMode(EndpointMode.PULL);

        // 注册新端点
        endpointRegistry.register(newEndpoint);

        // 验证已注册
        EndpointProperties resolved = endpointRegistry.resolve("/api/notify/stream");
        assertNotNull(resolved);
        assertEquals("im.notify", resolved.getTopic());
        assertEquals(EndpointMode.PULL, resolved.getMode());
    }

    @Test
    @DisplayName("断点续传装配：ResumeSupport 与内存历史存储自动配置")
    void testResumeWiring() {
        assertNotNull(resumeSupport, "ResumeSupport 应由 ImAutoConfiguration 自动装配");
        assertNotNull(messageHistoryStore,
                "im.sse.resume.in-memory.enabled=true 应自动装配内存历史存储");
        assertInstanceOf(InMemoryMessageHistoryStore.class, messageHistoryStore);
        // 存储注册为 Topic 生命周期监听器（Topic 销毁时清理历史，防泄漏）
        assertInstanceOf(TopicLifecycleListener.class, messageHistoryStore);
    }
}
