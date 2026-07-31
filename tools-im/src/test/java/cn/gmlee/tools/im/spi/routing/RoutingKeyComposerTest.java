package cn.gmlee.tools.im.spi.routing;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 路由键组合器测试.
 * <p>
 * 验证 {@link DefaultRoutingKeyComposer} 的组合逻辑和 {@link RoutingKeyComposer#resolve} 归一化语义。
 * </p>
 */
class RoutingKeyComposerTest {

    private DefaultRoutingKeyComposer composer;

    @BeforeEach
    void setUp() {
        composer = new DefaultRoutingKeyComposer();
    }

    // ==================== resolve() 归一化 ====================

    @Nested
    @DisplayName("resolve() 归一化")
    class ResolveTests {

        @Test
        @DisplayName("null → null（全参数模式）")
        void resolveNull() {
            assertNull(RoutingKeyComposer.resolve(null));
        }

        @Test
        @DisplayName("空列表 → null（全参数模式）")
        void resolveEmpty() {
            assertNull(RoutingKeyComposer.resolve(Collections.emptyList()));
        }

        @Test
        @DisplayName("[\"*\"] → null（显式全参数）")
        void resolveStar() {
            assertNull(RoutingKeyComposer.resolve(Collections.singletonList("*")));
        }

        @Test
        @DisplayName("[\"room\"] → [\"room\"]（指定字段不变）")
        void resolveSpecific() {
            List<String> result = RoutingKeyComposer.resolve(Collections.singletonList("room"));
            assertEquals(Collections.singletonList("room"), result);
        }

        @Test
        @DisplayName("[\"tenant\", \"room\"] → 原样返回")
        void resolveMulti() {
            List<String> input = Arrays.asList("tenant", "room");
            List<String> result = RoutingKeyComposer.resolve(input);
            assertEquals(input, result);
        }
    }

    // ==================== composeRoutingKey ====================

    @Nested
    @DisplayName("composeRoutingKey")
    class ComposeTests {

        @Test
        @DisplayName("全参数模式：按字母排序构建规范化查询字符串")
        void composeAllParamsSorted() {
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("tenant", "acme");
            params.add("room", "lobby");

            String key = composer.composeRoutingKey(null, params);
            assertEquals("room=lobby&tenant=acme", key);
        }

        @Test
        @DisplayName("全参数模式：参数顺序无关")
        void composeAllParamsOrderIndependent() {
            MultiValueMap<String, String> params1 = new LinkedMultiValueMap<>();
            params1.add("b", "2");
            params1.add("a", "1");

            MultiValueMap<String, String> params2 = new LinkedMultiValueMap<>();
            params2.add("a", "1");
            params2.add("b", "2");

            assertEquals(
                    composer.composeRoutingKey(null, params1),
                    composer.composeRoutingKey(null, params2)
            );
        }

        @Test
        @DisplayName("指定字段模式：仅提取指定字段")
        void composeSpecificFields() {
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("room", "lobby");
            params.add("tenant", "acme");

            String key = composer.composeRoutingKey(Collections.singletonList("room"), params);
            assertEquals("room=lobby", key);
        }

        @Test
        @DisplayName("指定多字段模式：也按字母排序")
        void composeMultiFieldsSorted() {
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("tenant", "acme");
            params.add("room", "lobby");

            // 配置顺序为 ["tenant", "room"]，但输出按字母排序
            String key = composer.composeRoutingKey(Arrays.asList("tenant", "room"), params);
            assertEquals("room=lobby&tenant=acme", key);
        }

        @Test
        @DisplayName("无参数 → null（广播连接）")
        void composeNoParams() {
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            assertNull(composer.composeRoutingKey(null, params));
        }

        @Test
        @DisplayName("null 参数 → null")
        void composeNullParams() {
            assertNull(composer.composeRoutingKey(null, null));
        }

        @Test
        @DisplayName("指定字段不存在 → null")
        void composeMissingField() {
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("room", "lobby");

            // 指定 tenant 字段但 URL 没有
            assertNull(composer.composeRoutingKey(Collections.singletonList("tenant"), params));
        }

        @Test
        @DisplayName("多值参数取第一个值")
        void composeMultiValueTakesFirst() {
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("room", "lobby");
            params.add("room", "main");

            String key = composer.composeRoutingKey(Collections.singletonList("room"), params);
            assertEquals("room=lobby", key);
        }
    }

    // ==================== extractRoutingTargets ====================

    @Nested
    @DisplayName("extractRoutingTargets")
    class ExtractTests {

        @Test
        @DisplayName("全参数模式：单值参数 → 单个 target")
        void extractAllParamsSingleValue() {
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("tenant", "acme");
            params.add("room", "lobby");

            Set<String> targets = composer.extractRoutingTargets(null, params);
            assertEquals(Set.of("room=lobby&tenant=acme"), targets);
        }

        @Test
        @DisplayName("单键多值：每个值各为一个 target")
        void extractSingleKeyMultiValue() {
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("room", "lobby");
            params.add("room", "main");

            Set<String> targets = composer.extractRoutingTargets(Collections.singletonList("room"), params);
            assertEquals(Set.of("room=lobby", "room=main"), targets);
        }

        @Test
        @DisplayName("多键多值：按位置配对")
        void extractMultiKeyPositionPairing() {
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("room", "lobby");
            params.add("room", "main");
            params.add("tenant", "acme");
            params.add("tenant", "beta");

            Set<String> targets = composer.extractRoutingTargets(Arrays.asList("tenant", "room"), params);
            assertEquals(Set.of("room=lobby&tenant=acme", "room=main&tenant=beta"), targets);
        }

        @Test
        @DisplayName("多键长度不一致：以最短为准")
        void extractMultiKeyUnevenLengths() {
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("room", "lobby");
            params.add("room", "main");
            params.add("room", "extra");
            params.add("tenant", "acme");
            params.add("tenant", "beta");

            Set<String> targets = composer.extractRoutingTargets(Arrays.asList("tenant", "room"), params);
            // tenant 只有 2 个值，所以只产生 2 个 target（extra 被忽略）
            assertEquals(2, targets.size());
            assertTrue(targets.contains("room=lobby&tenant=acme"));
            assertTrue(targets.contains("room=main&tenant=beta"));
        }

        @Test
        @DisplayName("无参数 → 空集（广播）")
        void extractNoParams() {
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            Set<String> targets = composer.extractRoutingTargets(null, params);
            assertTrue(targets.isEmpty());
        }

        @Test
        @DisplayName("null 参数 → 空集")
        void extractNullParams() {
            Set<String> targets = composer.extractRoutingTargets(null, null);
            assertTrue(targets.isEmpty());
        }

        @Test
        @DisplayName("指定字段不存在 → 空集")
        void extractMissingField() {
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("room", "lobby");

            Set<String> targets = composer.extractRoutingTargets(Collections.singletonList("tenant"), params);
            assertTrue(targets.isEmpty());
        }

        @Test
        @DisplayName("全参数多值：多键按位置配对")
        void extractAllParamsMultiValue() {
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("tenant", "acme");
            params.add("tenant", "beta");
            params.add("room", "lobby");
            params.add("room", "main");

            Set<String> targets = composer.extractRoutingTargets(null, params);
            assertEquals(Set.of("room=lobby&tenant=acme", "room=main&tenant=beta"), targets);
        }
    }

    // ==================== 发布方-订阅方对称性 ====================

    @Nested
    @DisplayName("发布方-订阅方对称性")
    class SymmetryTests {

        @Test
        @DisplayName("订阅方的 routingKey 应等于发布方 target 之一")
        void subscriberPublisherSymmetry() {
            // 订阅方：以 ?tenant=acme&room=lobby 连接
            MultiValueMap<String, String> subscriberParams = new LinkedMultiValueMap<>();
            subscriberParams.add("tenant", "acme");
            subscriberParams.add("room", "lobby");
            String subscriberKey = composer.composeRoutingKey(null, subscriberParams);

            // 发布方：以相同参数投递
            MultiValueMap<String, String> publisherParams = new LinkedMultiValueMap<>();
            publisherParams.add("tenant", "acme");
            publisherParams.add("room", "lobby");
            Set<String> targets = composer.extractRoutingTargets(null, publisherParams);

            assertTrue(targets.contains(subscriberKey),
                    "发布方的 targets 应包含订阅方的 routingKey");
        }

        @Test
        @DisplayName("不同参数顺序产生相同的 routingKey 和 targets")
        void parameterOrderIndependence() {
            MultiValueMap<String, String> order1 = new LinkedMultiValueMap<>();
            order1.add("b", "2");
            order1.add("a", "1");

            MultiValueMap<String, String> order2 = new LinkedMultiValueMap<>();
            order2.add("a", "1");
            order2.add("b", "2");

            assertEquals(
                    composer.composeRoutingKey(null, order1),
                    composer.composeRoutingKey(null, order2)
            );
            assertEquals(
                    composer.extractRoutingTargets(null, order1),
                    composer.extractRoutingTargets(null, order2)
            );
        }
    }
}
