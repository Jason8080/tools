package cn.gmlee.tools.im.topic;

import cn.gmlee.tools.im.core.Repeater;

/**
 * Topic 组件共享基类.
 * <p>
 * 为 {@link DefaultPublisher} 和 {@link DefaultSubscriber} 提供公共字段与懒加载逻辑：
 * </p>
 * <ul>
 *   <li>{@link #topic} — Topic 名称（不可变）</li>
 *   <li>{@link #resolveRepeater()} — 双检锁延迟解析 Repeater</li>
 * </ul>
 * <p>
 * 支持两种构造方式：
 * </p>
 * <ol>
 *   <li>直接注入 Repeater（构造时已知）</li>
 *   <li>注入 {@link TopicRegistry}，首次 {@link #resolveRepeater()} 时延迟解析</li>
 * </ol>
 *
 * @since 5.6.0
 */
abstract class AbstractTopicComponent {

    protected final String topic;
    private volatile Repeater repeater;
    private final TopicRegistry topicRegistry;

    /**
     * 直接注入 Repeater.
     *
     * @param topic    Topic 名称
     * @param repeater 已解析的 Repeater 实例
     */
    protected AbstractTopicComponent(String topic, Repeater repeater) {
        this.topic = topic;
        this.repeater = repeater;
        this.topicRegistry = null;
    }

    /**
     * 注入 TopicRegistry，延迟解析 Repeater.
     *
     * @param topic         Topic 名称
     * @param topicRegistry Topic 组件注册表
     */
    protected AbstractTopicComponent(String topic, TopicRegistry topicRegistry) {
        this.topic = topic;
        this.repeater = null;
        this.topicRegistry = topicRegistry;
    }

    /**
     * 返回 Topic 名称.
     *
     * @return Topic 名称（不可变）
     */
    public final String topic() {
        return topic;
    }

    /**
     * 获取 Repeater 实例（双检锁延迟解析）.
     * <p>
     * 如果构造时已注入 Repeater，直接返回；否则首次调用时通过 TopicRegistry 解析并缓存。
     * </p>
     *
     * @return Repeater 实例
     */
    protected final Repeater resolveRepeater() {
        Repeater r = this.repeater;
        if (r != null) {
            return r;
        }
        synchronized (this) {
            if (this.repeater != null) {
                return this.repeater;
            }
            this.repeater = topicRegistry.getRepeater(topic);
            return this.repeater;
        }
    }
}
