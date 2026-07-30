package cn.gmlee.tools.im.core;

/**
 * Topic 标记接口.
 * <p>
 * {@link Publisher}、{@link Repeater}、{@link Subscriber} 的共同基接口，
 * 提供 {@link #topic()} 方法用于框架按 Topic 匹配组件实现。
 * </p>
 *
 * @since 5.6.0
 */
public interface Topic {

    /**
     * 所属 Topic 名称.
     * <p>
     * 框架通过此方法将自定义实现匹配到对应的 Topic。
     * </p>
     *
     * @return Topic 名称（不可为 null）
     */
    String topic();
}
