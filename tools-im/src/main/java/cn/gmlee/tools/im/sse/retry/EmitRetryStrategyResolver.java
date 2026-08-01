package cn.gmlee.tools.im.sse.retry;

import java.util.Collections;
import java.util.Map;

/**
 * 重试策略解析器.
 * <p>
 * 根据配置和 Topic 名称解析对应的重试策略。
 * 支持全局默认策略和按 Topic 覆盖。
 * </p>
 *
 * <h3>解析优先级</h3>
 * <ol>
 *   <li>Topic 级别覆盖策略</li>
 *   <li>全局默认策略</li>
 * </ol>
 *
 * @since 5.6.0
 */
public final class EmitRetryStrategyResolver {

    /**
     * 默认策略.
     */
    private final EmitRetryStrategy defaultStrategy;

    /**
     * Topic 级别策略覆盖.
     */
    private final Map<String, EmitRetryStrategy> topicOverrides;

    /**
     * 创建策略解析器.
     *
     * @param defaultStrategy 默认策略
     * @param topicOverrides  Topic 级别策略覆盖
     */
    public EmitRetryStrategyResolver(EmitRetryStrategy defaultStrategy,
                                     Map<String, EmitRetryStrategy> topicOverrides) {
        this.defaultStrategy = defaultStrategy;
        this.topicOverrides = topicOverrides != null ? topicOverrides : Collections.emptyMap();
    }

    /**
     * 解析指定 Topic 的重试策略.
     * <p>
     * 优先返回 Topic 级别覆盖策略，未配置则返回默认策略。
     * </p>
     *
     * @param topic Topic 名称
     * @return 对应的重试策略
     */
    public EmitRetryStrategy resolve(String topic) {
        return topicOverrides.getOrDefault(topic, defaultStrategy);
    }

    /**
     * 创建默认解析器（无重试策略）.
     *
     * @return 默认解析器
     */
    public static EmitRetryStrategyResolver createDefault() {
        return new EmitRetryStrategyResolver(NoRetryStrategy.INSTANCE, Collections.emptyMap());
    }
}
