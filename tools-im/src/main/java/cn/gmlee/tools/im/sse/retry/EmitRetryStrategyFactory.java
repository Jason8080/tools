package cn.gmlee.tools.im.sse.retry;

import cn.gmlee.tools.im.conf.SseProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * 重试策略工厂.
 * <p>
 * 根据配置创建对应的重试策略实例和解析器。
 * </p>
 *
 * @since 5.6.0
 */
public final class EmitRetryStrategyFactory {

    private EmitRetryStrategyFactory() {
    }

    /**
     * 根据配置创建策略解析器.
     *
     * @param config 重试策略配置
     * @return 策略解析器
     */
    public static EmitRetryStrategyResolver createResolver(SseProperties.EmitRetryConfig config) {
        EmitRetryStrategy defaultStrategy = createStrategy(config, config.getDefaultStrategy());

        Map<String, EmitRetryStrategy> overrides = new HashMap<>();
        config.getTopicOverrides().forEach((topic, strategyName) -> {
            overrides.put(topic, createStrategy(config, strategyName));
        });

        return new EmitRetryStrategyResolver(defaultStrategy, overrides);
    }

    /**
     * 根据策略名称创建策略实例.
     *
     * @param config       重试策略配置
     * @param strategyName 策略名称
     * @return 策略实例
     * @throws IllegalArgumentException 如果策略名称未知
     */
    private static EmitRetryStrategy createStrategy(SseProperties.EmitRetryConfig config, String strategyName) {
        switch (strategyName) {
            case "no-retry":
                return NoRetryStrategy.INSTANCE;
            case "busy-loop":
                return new BusyLoopRetryStrategy(config.getBusyLoopTimeout());
            case "bounded":
                return new BoundedRetryStrategy(
                        config.getBoundedMaxRetries(),
                        config.getBoundedRetryInterval()
                );
            case "exponential-backoff":
                return new ExponentialBackoffRetryStrategy(
                        config.getExponentialMaxRetries(),
                        config.getExponentialInitialInterval(),
                        config.getExponentialMultiplier(),
                        config.getExponentialMaxInterval()
                );
            default:
                throw new IllegalArgumentException("Unknown retry strategy: " + strategyName);
        }
    }
}
