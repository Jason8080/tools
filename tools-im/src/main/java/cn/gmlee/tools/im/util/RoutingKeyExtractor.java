package cn.gmlee.tools.im.util;

import org.springframework.util.MultiValueMap;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 路由键提取工具.
 * <p>
 * 统一发布方和订阅方的路由键提取逻辑，隔离 URL 参数解析复杂度。
 * 端点层（{@code EndpointRouter}、{@code ImPublisher}）调用此工具，
 * 框架核心层（{@code TopicMessage}、{@code SseConnectionFluxBuilder}）
 * 只使用提取结果，不感知 URL 参数结构。
 * </p>
 *
 * <h3>提取规则</h3>
 * <ul>
 *   <li><b>单键</b>（如 {@code ["me"]}）：提取该键的值，多键值表示多目标</li>
 *   <li><b>多键</b>（如 {@code ["tenant", "room"]}）：按顺序提取各键的值，以 {@code |} 拼接</li>
 * </ul>
 *
 * <h3>示例</h3>
 * <pre>
 * # 单键 — 订阅方
 * routingKeys = ["me"]
 * params = {me=[alice]}
 * extract() → "alice"
 *
 * # 单键 — 发布方（批量）
 * routingKeys = ["me"]
 * params = {me=[alice, bob]}
 * extractTargets() → {"alice", "bob"}
 *
 * # 多键 — 订阅方
 * routingKeys = ["tenant", "room"]
 * params = {tenant=[acme], room=[lobby]}
 * extract() → "acme|lobby"
 *
 * # 多键 — 发布方（批量，按位置配对）
 * routingKeys = ["tenant", "room"]
 * params = {tenant=[acme, beta], room=[lobby, main]}
 * extractTargets() → {"acme|lobby", "beta|main"}
 * </pre>
 *
 * @since 5.6.0
 */
public final class RoutingKeyExtractor {

    /**
     * 拼接分隔符
     */
    private static final String SEPARATOR = "|";

    private RoutingKeyExtractor() {
        // 工具类禁止实例化
    }

    /**
     * 提取单个路由键（订阅方使用）.
     * <p>
     * 从 URL 参数中按配置的路由键列表提取值，多键以 {@code |} 拼接为单个字符串。
     * </p>
     *
     * @param routingKeys 路由键配置列表
     * @param params      URL 参数
     * @return 路由键字符串，无匹配时返回 null
     */
    public static String extract(List<String> routingKeys, MultiValueMap<String, String> params) {
        if (routingKeys == null || routingKeys.isEmpty()) {
            return null;
        }
        if (routingKeys.size() == 1) {
            return params.getFirst(routingKeys.getFirst());
        }
        // 多键：按顺序提取并拼接
        StringBuilder sb = new StringBuilder();
        for (String key : routingKeys) {
            String value = params.getFirst(key);
            if (value != null) {
                if (!sb.isEmpty()) {
                    sb.append(SEPARATOR);
                }
                sb.append(value);
            }
        }
        return !sb.isEmpty() ? sb.toString() : null;
    }

    /**
     * 提取路由目标集合（发布方使用，支持批量）.
     * <p>
     * 单键模式：提取该键的所有值，支持多目标投递（如 {@code ?me=alice&me=bob}）。
     * 多键模式：按位置配对各键的值，以 {@code |} 拼接，支持批量复合目标
     * （如 {@code ?tenant=acme&room=lobby&tenant=beta&room=main} → {@code {"acme|lobby", "beta|main"}}）。
     * </p>
     *
     * @param routingKeys 路由键配置列表
     * @param params      URL 参数
     * @return 路由目标集合（不可变），空集表示广播
     */
    public static Set<String> extractTargets(List<String> routingKeys, MultiValueMap<String, String> params) {
        if (routingKeys == null || routingKeys.isEmpty()) {
            return Collections.emptySet();
        }
        if (routingKeys.size() == 1) {
            // 单键：提取所有值（多目标）
            List<String> values = params.get(routingKeys.getFirst());
            if (values == null || values.isEmpty()) {
                return Collections.emptySet();
            }
            return Set.copyOf(values);
        }
        // 多键：按位置配对
        return extractMultiKeyTargets(routingKeys, params);
    }

    /**
     * 多键按位置配对提取.
     * <p>
     * 各键的值列表按位置对齐，拼接为复合路由键。
     * 以最短列表为准，多余值忽略。
     * </p>
     *
     * @param routingKeys 路由键配置列表（多键）
     * @param params      URL 参数
     * @return 路由目标集合
     */
    private static Set<String> extractMultiKeyTargets(List<String> routingKeys, MultiValueMap<String, String> params) {
        // 收集各键的值列表
        int keyCount = routingKeys.size();
        List<List<String>> valueLists = new java.util.ArrayList<>(keyCount);
        int minSize = Integer.MAX_VALUE;

        for (String key : routingKeys) {
            List<String> values = params.get(key);
            if (values == null || values.isEmpty()) {
                return Collections.emptySet();
            }
            valueLists.add(values);
            minSize = Math.min(minSize, values.size());
        }

        if (minSize == 0 || minSize == Integer.MAX_VALUE) {
            return Collections.emptySet();
        }

        // 按位置配对拼接
        Set<String> targets = new LinkedHashSet<>(minSize);
        for (int i = 0; i < minSize; i++) {
            StringBuilder sb = new StringBuilder();
            for (int k = 0; k < keyCount; k++) {
                if (k > 0) {
                    sb.append(SEPARATOR);
                }
                sb.append(valueLists.get(k).get(i));
            }
            targets.add(sb.toString());
        }
        return Collections.unmodifiableSet(targets);
    }
}
