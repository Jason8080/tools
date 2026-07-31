package cn.gmlee.tools.im.core;

import cn.gmlee.tools.im.model.Msg;

import java.io.Serializable;
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
 * @param <ID>  消息 ID 类型
 * @param <MSG> 消息载荷类型
 * @since 5.6.0
 */
abstract class AbstractTopic<ID extends Serializable, MSG extends Msg> implements Topic {

    protected final String topic;
    private volatile Repeater<ID, MSG> repeater;
    private final Supplier<Repeater<ID, MSG>> repeaterSupplier;

    /**
     * 直接注入 Repeater.
     *
     * @param topic    Topic 名称
     * @param repeater 已解析的 Repeater 实例
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected AbstractTopic(String topic, Repeater repeater) {
        this.topic = topic;
        this.repeater = (Repeater<ID, MSG>) repeater;
        this.repeaterSupplier = null;
    }

    /**
     * 延迟解析 Repeater.
     *
     * @param topic            Topic 名称
     * @param repeaterSupplier Repeater 解析器（首次使用时调用）
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected AbstractTopic(String topic, Supplier repeaterSupplier) {
        this.topic = topic;
        this.repeater = null;
        this.repeaterSupplier = (Supplier<Repeater<ID, MSG>>) repeaterSupplier;
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
    protected final Repeater<ID, MSG> resolveRepeater() {
        Repeater<ID, MSG> r = this.repeater;
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
