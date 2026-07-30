package cn.gmlee.tools.im.core;

import java.util.function.Supplier;

/**
 * Topic 组件共享基类（包级私有）.
 * <p>
 * 为 {@link ImPublisher} 和 {@link ImSubscriber} 提供公共字段与懒加载逻辑：
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
 *   <li>注入 {@link Supplier} ，首次 {@link #resolveRepeater()} 时延迟解析</li>
 * </ol>
 *
 * @since 5.6.0
 */
abstract class AbstractTopic implements Topic {

    protected final String topic;
    private volatile Repeater repeater;
    private final Supplier<Repeater> repeaterSupplier;

    /**
     * 直接注入 Repeater.
     *
     * @param topic    Topic 名称
     * @param repeater 已解析的 Repeater 实例
     */
    protected AbstractTopic(String topic, Repeater repeater) {
        this.topic = topic;
        this.repeater = repeater;
        this.repeaterSupplier = null;
    }

    /**
     * 延迟解析 Repeater.
     *
     * @param topic            Topic 名称
     * @param repeaterSupplier Repeater 解析器（首次使用时调用）
     */
    protected AbstractTopic(String topic, Supplier<Repeater> repeaterSupplier) {
        this.topic = topic;
        this.repeater = null;
        this.repeaterSupplier = repeaterSupplier;
    }

    @Override
    public final String topic() {
        return topic;
    }

    /**
     * 获取 Repeater 实例（双检锁延迟解析）.
     * <p>
     * 如果构造时已注入 Repeater，直接返回；否则首次调用时通过 Supplier 解析并缓存。
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
            this.repeater = repeaterSupplier.get();
            return this.repeater;
        }
    }
}
