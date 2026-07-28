package cn.gmlee.tools.im.sse.backpressure;

import cn.gmlee.tools.im.conf.SseProperties;
import lombok.RequiredArgsConstructor;

import java.util.Map;

/**
 * 默认背压策略解析器.
 * <p>
 * 根据配置解析 Topic 对应的背压策略：
 * <ol>
 *   <li>检查 topic-overrides 映射是否有该 Topic 的覆盖</li>
 *   <li>未命中则返回默认策略</li>
 * </ol>
 * </p>
 */
@RequiredArgsConstructor
public class DefaultBackpressureStrategyResolver implements BackpressureStrategyResolver {

    private final SseProperties properties;
    private final Map<String, BackpressureStrategy> strategyMap;

    @Override
    public BackpressureStrategy resolve(String topic) {
        // 检查 topic 覆盖
        Map<String, String> overrides = properties.getBackpressure().getTopicOverrides();
        if (overrides != null && overrides.containsKey(topic)) {
            String strategyName = overrides.get(topic);
            BackpressureStrategy strategy = strategyMap.get(strategyName);
            if (strategy != null) {
                return strategy;
            }
        }

        // 返回默认策略
        String defaultName = properties.getBackpressure().getDefaultStrategy();
        return strategyMap.getOrDefault(defaultName, strategyMap.get(BufferBackpressureStrategy.NAME));
    }
}
