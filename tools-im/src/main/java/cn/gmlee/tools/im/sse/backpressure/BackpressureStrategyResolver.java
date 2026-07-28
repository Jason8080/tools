package cn.gmlee.tools.im.sse.backpressure;

/**
 * 背压策略解析器接口.
 * <p>
 * 根据 Topic 名称解析对应的背压策略。
 * 默认实现检查配置的 topic-overrides 映射，未命中则返回默认策略。
 * </p>
 * <p>
 * 用户可实现自定义解析器（如基于模式匹配、正则表达式等）注册为 Spring Bean。
 * </p>
 */
public interface BackpressureStrategyResolver {

    /**
     * 解析 Topic 对应的背压策略.
     *
     * @param topic Topic 名称
     * @return 对应的背压策略
     */
    BackpressureStrategy resolve(String topic);
}
