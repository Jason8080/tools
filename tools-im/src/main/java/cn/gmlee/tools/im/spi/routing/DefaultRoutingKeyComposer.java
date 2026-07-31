package cn.gmlee.tools.im.spi.routing;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 默认路由键组合器.
 * <p>
 * routingKey 格式为<b>规范化查询字符串</b>：key 按字母排序，{@code key=value} 以 {@code &} 拼接，
 * 值取 {@link MultiValueMap#getFirst(Object)}。保证参数顺序无关性。
 * </p>
 *
 * <h3>示例</h3>
 * <pre>
 * # 全参数模式（routingKeys = null）— 发布/订阅使用相同 URL 参数
 * ?tenant=acme&amp;room=lobby → "room=lobby&amp;tenant=acme"（key 字母排序）
 * ?room=lobby&amp;tenant=acme → "room=lobby&amp;tenant=acme"（相同结果，顺序无关）
 *
 * # 订阅方指定字段（routingKeys = ["me"]）— "我是谁"
 * ?me=alice               → "me=alice"
 *
 * # 发布方指定字段（routingKeys = ["to"]）— "发给谁"
 * ?to=alice               → "to=alice"
 *
 * # 指定多字段（routingKeys = ["tenant", "room"]）
 * ?tenant=acme&amp;room=lobby → "room=lobby&amp;tenant=acme"（指定字段也按字母排序）
 *
 * # 发布方单键多值（批量投递）
 * ?to=alice&amp;to=bob        → {"to=alice", "to=bob"}
 *
 * # 发布方多键多值（位置配对）
 * ?room=lobby&amp;room=main&amp;tenant=acme&amp;tenant=beta
 *                          → {"room=lobby&amp;tenant=acme", "room=main&amp;tenant=beta"}
 *
 * # 无参数（广播）
 * ?                       → null（compose）/ {}（extract）
 * </pre>
 *
 * <h3>扩展方式</h3>
 * <p>
 * 注册自定义 {@link RoutingKeyComposer} Bean 后，此默认实现自动被替代（通过
 * {@link ConditionalOnMissingBean} 保证）。
 * </p>
 *
 * @since 5.6.0
 */
@Component
@ConditionalOnMissingBean(RoutingKeyComposer.class)
public class DefaultRoutingKeyComposer implements RoutingKeyComposer {

    private static final String KV_SEPARATOR = "=";
    private static final String PAIR_SEPARATOR = "&";

    @Override
    public String composeRoutingKey(List<String> routingKeys, MultiValueMap<String, String> params) {
        if (params == null || params.isEmpty()) {
            return null;
        }

        List<String> effectiveKeys = resolveEffectiveKeys(routingKeys, params);
        if (effectiveKeys.isEmpty()) {
            return null;
        }

        return buildQueryString(effectiveKeys, params);
    }

    @Override
    public Set<String> extractRoutingTargets(List<String> routingKeys, MultiValueMap<String, String> params) {
        if (params == null || params.isEmpty()) {
            return Collections.emptySet();
        }

        List<String> effectiveKeys = resolveEffectiveKeys(routingKeys, params);
        if (effectiveKeys.isEmpty()) {
            return Collections.emptySet();
        }

        if (effectiveKeys.size() == 1) {
            // 单键模式：提取该键的所有值，每个值构建一个 target
            String key = effectiveKeys.get(0);
            List<String> values = params.get(key);
            if (values == null || values.isEmpty()) {
                return Collections.emptySet();
            }
            Set<String> targets = new LinkedHashSet<>(values.size());
            for (String value : values) {
                targets.add(key + KV_SEPARATOR + value);
            }
            return Collections.unmodifiableSet(targets);
        }

        // 多键模式：按位置配对
        return extractMultiKeyTargets(effectiveKeys, params);
    }

    /**
     * 解析有效的路由键列表.
     * <p>
     * routingKeys 为 null → 收集全部参数名并排序；否则直接使用指定字段并排序。
     * </p>
     *
     * @param routingKeys 归一化后的路由键配置（null 表示全部参数）
     * @param params      URL 参数
     * @return 有效键列表（已排序，不可变）
     */
    private List<String> resolveEffectiveKeys(List<String> routingKeys, MultiValueMap<String, String> params) {
        if (routingKeys == null) {
            // 全参数模式：收集所有参数名，按字母排序
            List<String> allKeys = new ArrayList<>(params.keySet());
            Collections.sort(allKeys);
            return allKeys;
        }

        // 指定字段模式：按字母排序（保证顺序无关性）
        List<String> sorted = new ArrayList<>(routingKeys);
        Collections.sort(sorted);
        return sorted;
    }

    /**
     * 从参数中构建规范化查询字符串.
     * <p>
     * 每个键取第一个值（{@link MultiValueMap#getFirst(Object)}），按 key 字母排序拼接。
     * </p>
     *
     * @param keys   键列表（已排序）
     * @param params URL 参数
     * @return 规范化查询字符串，无有效键值对时返回 null
     */
    private String buildQueryString(List<String> keys, MultiValueMap<String, String> params) {
        StringBuilder sb = new StringBuilder();
        for (String key : keys) {
            String value = params.getFirst(key);
            if (value == null) {
                continue;
            }
            if (!sb.isEmpty()) {
                sb.append(PAIR_SEPARATOR);
            }
            sb.append(key).append(KV_SEPARATOR).append(value);
        }
        return sb.isEmpty() ? null : sb.toString();
    }

    /**
     * 多键按位置配对提取.
     * <p>
     * 各键的值列表按位置对齐，拼接为规范化查询字符串。
     * 以最短列表为准，多余值忽略。
     * </p>
     *
     * @param keys   键列表（已排序）
     * @param params URL 参数
     * @return 路由目标集合（不可变）
     */
    private Set<String> extractMultiKeyTargets(List<String> keys, MultiValueMap<String, String> params) {
        int keyCount = keys.size();
        List<List<String>> valueLists = new ArrayList<>(keyCount);
        int minSize = Integer.MAX_VALUE;

        for (String key : keys) {
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
                    sb.append(PAIR_SEPARATOR);
                }
                sb.append(keys.get(k)).append(KV_SEPARATOR).append(valueLists.get(k).get(i));
            }
            targets.add(sb.toString());
        }
        return Collections.unmodifiableSet(targets);
    }
}
