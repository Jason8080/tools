package cn.gmlee.tools.im.conf;

import cn.gmlee.tools.im.endpoint.EndpointRegistry;
import cn.gmlee.tools.im.model.EndpointMode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.stream.binding.BindingService;
import org.springframework.cloud.stream.config.BindingServiceProperties;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.test.context.TestPropertySource;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 端到端集成测试.
 * <p>
 * 验证 YAML 配置 → 端点注册 → Stream 资源创建的完整链路。
 * </p>
 */
@SpringBootTest(classes = {ImAutoConfiguration.class, EndpointAutoConfiguration.class,
        EndToEndIntegrationTest.MockStreamBeans.class})
@TestPropertySource(properties = {
    "im.endpoints[0].path=/api/chat/pull",
    "im.endpoints[0].topic=im.chat",
    "im.endpoints[0].mode=pull",
    "im.endpoints[1].path=/api/chat/push",
    "im.endpoints[1].topic=im.chat",
    "im.endpoints[1].mode=push",
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

        /**
         * EndpointAutoConfiguration 构造器需要 BeanDefinitionRegistry.
         * 在常规 Spring Boot 应用中，ApplicationContext 自身实现了该接口，
         * 但测试上下文不会自动将其暴露为该类型的 Bean，需显式提供。
         */
        @Bean
        public BeanDefinitionRegistry beanDefinitionRegistry(ApplicationContext ctx) {
            return (BeanDefinitionRegistry) ctx;
        }
    }

    @Autowired
    private EndpointRegistry endpointRegistry;

    @Autowired
    private ImProperties imProperties;

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
}
