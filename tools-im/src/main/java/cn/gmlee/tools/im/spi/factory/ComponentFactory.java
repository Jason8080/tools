package cn.gmlee.tools.im.spi.factory;

/**
 * 组件工厂泛型基接口.
 * <p>
 * 为 {@link PublisherFactory}、{@link RepeaterFactory}、{@link SubscriberFactory} 提供统一的类型抽象。
 * 实现类通过泛型参数指定创建的组件类型和上下文类型。
 * </p>
 *
 * <h3>设计说明</h3>
 * <ul>
 *   <li>{@code T} — 创建的组件类型（Publisher/Repeater/Subscriber）</li>
 *   <li>{@code C} — 创建上下文类型（{@code Supplier<Repeater>} 或 {@link RepeaterContext}）</li>
 * </ul>
 * <p>
 * 具体的工厂接口继承此接口并指定具体类型，开发者通常直接实现具体接口而非此泛型基接口。
 * </p>
 *
 * @param <T> 组件类型
 * @param <C> 上下文类型
 * @since 5.6.0
 */
public interface ComponentFactory<T, C> {

    /**
     * 创建组件实例.
     *
     * @param topic   Topic 名称
     * @param context 创建上下文
     * @return 组件实例，返回 {@code null} 表示使用默认工厂
     */
    T create(String topic, C context);
}
