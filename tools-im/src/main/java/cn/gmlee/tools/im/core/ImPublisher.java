package cn.gmlee.tools.im.core;

import cn.gmlee.tools.im.model.Msg;
import cn.gmlee.tools.im.model.TopicMessage;
import cn.gmlee.tools.im.resume.EventIdGenerator;
import cn.gmlee.tools.im.spi.routing.DefaultRoutingKeyComposer;
import cn.gmlee.tools.im.spi.routing.RoutingKeyComposer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.MultiValueMap;
import reactor.core.publisher.Mono;

import java.io.Serializable;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Publisher 框架骨架.
 * <p>
 * 继承 {@link AbstractTopic}，提供 TopicMessage 构建、Repeater 委托和日志记录。
 * 子类只需提供构造器即可组成可用的默认发布器。
 * </p>
 *
 * <h3>扩展方式</h3>
 * <p>
 * 继承此类即可获得 Repeater 解析和日志能力。
 * 可重写 {@link #push(MultiValueMap, Msg)} 自定义发布逻辑。
 * </p>
 *
 * <h3>消息 ID 分配（v5.7.0）</h3>
 * <p>
 * {@code push()} 路径的消息 ID 由 {@link EventIdGenerator} 统一分配
 * （覆盖 {@code Msg.build()} 携带的默认值）——断点续传要求同 Topic 内
 * ID 全局唯一且全序。未注入生成器时保留 {@code Msg.build()} 的原有值。
 * 通过 {@code send(TopicMessage)} 显式构造信封的路径不受影响。
 * </p>
 *
 * @param <ID>  消息 ID 类型
 * @param <MSG> 消息载荷类型
 * @since 5.6.0
 */
@Slf4j
public abstract class ImPublisher<ID extends Serializable, MSG extends Msg>
        extends AbstractTopic<ID, MSG> implements Publisher<ID, MSG> {

    /**
     * 路由键配置.
     * <p>
     * 从 URL 参数中提取哪些字段作为定向投递目标。
     * 经过 {@link RoutingKeyComposer#resolve(List)} 归一化：
     * </p>
     * <ul>
     *   <li>{@code null} → 全部 URL 参数参与</li>
     *   <li>非 null → 使用指定字段</li>
     * </ul>
     */
    protected final List<String> routingKeys;

    /**
     * 路由键组合器.
     * <p>
     * 负责将 URL 参数组合为路由键字符串。默认使用 {@link DefaultRoutingKeyComposer}。
     * </p>
     */
    protected final RoutingKeyComposer composer;

    /**
     * 事件 ID 生成器（可为 null）.
     * <p>
     * push 路径统一分配消息 ID；null 表示保留 {@code Msg.build()} 的原有值。
     * </p>
     *
     * @since 5.7.0
     */
    protected final EventIdGenerator idGenerator;

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected ImPublisher(String topic, Repeater repeater) {
        this(topic, repeater, null, null, null);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected ImPublisher(String topic, Supplier<Repeater> repeaterSupplier) {
        this(topic, repeaterSupplier, null, null, null);
    }

    /**
     * 创建 Publisher（自定义路由键）.
     *
     * @param topic       Topic 名称
     * @param repeater    Repeater 实例
     * @param routingKeys 路由键配置（null/空/["*"] = 全部参数）
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected ImPublisher(String topic, Repeater repeater, List<String> routingKeys) {
        this(topic, repeater, routingKeys, null, null);
    }

    /**
     * 创建 Publisher（自定义路由键 + 组合器）.
     *
     * @param topic       Topic 名称
     * @param repeater    Repeater 实例
     * @param routingKeys 路由键配置（null/空/["*"] = 全部参数）
     * @param composer    路由键组合器（null 使用默认实现）
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected ImPublisher(String topic, Repeater repeater, List<String> routingKeys, RoutingKeyComposer composer) {
        this(topic, repeater, routingKeys, composer, null);
    }

    /**
     * 创建 Publisher（自定义路由键 + 组合器 + ID 生成器）.
     *
     * @param topic       Topic 名称
     * @param repeater    Repeater 实例
     * @param routingKeys 路由键配置（null/空/["*"] = 全部参数）
     * @param composer    路由键组合器（null 使用默认实现）
     * @param idGenerator 事件 ID 生成器（可为 null，框架注入）
     * @since 5.7.0
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected ImPublisher(String topic, Repeater repeater, List<String> routingKeys,
                          RoutingKeyComposer composer, EventIdGenerator idGenerator) {
        super(topic, repeater);
        this.routingKeys = RoutingKeyComposer.resolve(routingKeys);
        this.composer = composer != null ? composer : new DefaultRoutingKeyComposer();
        this.idGenerator = idGenerator;
    }

    /**
     * 创建 Publisher（自定义路由键，延迟解析 Repeater）.
     *
     * @param topic            Topic 名称
     * @param repeaterSupplier Repeater 延迟解析器
     * @param routingKeys      路由键配置（null/空/["*"] = 全部参数）
     */
    @SuppressWarnings({"rawtypes"})
    protected ImPublisher(String topic, Supplier<Repeater> repeaterSupplier, List<String> routingKeys) {
        this(topic, repeaterSupplier, routingKeys, null, null);
    }

    /**
     * 创建 Publisher（自定义路由键 + 组合器，延迟解析 Repeater）.
     *
     * @param topic            Topic 名称
     * @param repeaterSupplier Repeater 延迟解析器
     * @param routingKeys      路由键配置（null/空/["*"] = 全部参数）
     * @param composer         路由键组合器（null 使用默认实现）
     */
    @SuppressWarnings({"rawtypes"})
    protected ImPublisher(String topic, Supplier<Repeater> repeaterSupplier, List<String> routingKeys, RoutingKeyComposer composer) {
        this(topic, repeaterSupplier, routingKeys, composer, null);
    }

    /**
     * 创建 Publisher（自定义路由键 + 组合器 + ID 生成器，延迟解析 Repeater）.
     *
     * @param topic            Topic 名称
     * @param repeaterSupplier Repeater 延迟解析器
     * @param routingKeys      路由键配置（null/空/["*"] = 全部参数）
     * @param composer         路由键组合器（null 使用默认实现）
     * @param idGenerator      事件 ID 生成器（可为 null，框架注入）
     * @since 5.7.0
     */
    @SuppressWarnings({"rawtypes"})
    protected ImPublisher(String topic, Supplier<Repeater> repeaterSupplier, List<String> routingKeys,
                          RoutingKeyComposer composer, EventIdGenerator idGenerator) {
        super(topic, repeaterSupplier);
        this.routingKeys = RoutingKeyComposer.resolve(routingKeys);
        this.composer = composer != null ? composer : new DefaultRoutingKeyComposer();
        this.idGenerator = idGenerator;
    }

    @Override
    public Mono<ID> push(MultiValueMap<String, String> urlParams, MSG msg) {
        TopicMessage<ID, MSG> event = buildEvent(urlParams, msg);
        // 未指定 routingKeys → 从 URL 参数提取定向投递目标
        Set<String> targets = extractRoutingTargets(urlParams);
        if (!targets.isEmpty()) {
            event.setRoutingKeys(targets);
        }
        return send(event);
    }

    @Override
    public Mono<ID> push(MultiValueMap<String, String> urlParams, MSG msg, Set<String> routingKeys) {
        TopicMessage<ID, MSG> event = buildEvent(urlParams, msg);
        if (routingKeys != null && !routingKeys.isEmpty()) {
            event.setRoutingKeys(routingKeys);
        } else {
            // 未指定 routingKeys → 使用自身配置提取
            Set<String> targets = extractRoutingTargets(urlParams);
            if (!targets.isEmpty()) {
                event.setRoutingKeys(targets);
            }
        }
        return send(event);
    }

    @Override
    public Mono<ID> send(TopicMessage<ID, MSG> message) {
        // 无显式 routingKeys 且未通过 push 路径设置 → 从 urlParams 补充提取
        if ((message.getRoutingKeys() == null || message.getRoutingKeys().isEmpty())
                && message.getUrlParams() != null) {
            Set<String> targets = extractRoutingTargets(message.getUrlParams());
            if (!targets.isEmpty()) {
                message.setRoutingKeys(targets);
            }
        }
        return resolveRepeater().send(message)
                .doOnNext(id -> log.debug("[ImPublisher] 发布消息: topic={}, id={}", topic, id));
    }

    /**
     * 构建消息信封（包装载荷 + 注入 Topic + 统一分配 ID）.
     * <p>
     * push 路径的 ID 一律由 {@link EventIdGenerator} 分配（覆盖
     * {@code Msg.build()} 的默认值），保证断点续传所需的全序性；
     * 未注入生成器时保留原值。
     * </p>
     *
     * @param urlParams URL 参数
     * @param msg       消息载荷
     * @return 消息信封
     * @since 5.7.0
     */
    @SuppressWarnings("unchecked")
    protected TopicMessage<ID, MSG> buildEvent(MultiValueMap<String, String> urlParams, MSG msg) {
        TopicMessage<ID, MSG> event = msg.build(urlParams);
        event.setTopic(topic);
        if (idGenerator != null) {
            event.setId((ID) idGenerator.nextId(topic));
        }
        return event;
    }

    /**
     * 从 URL 参数提取路由目标集合.
     * <p>
     * 委托给 {@link RoutingKeyComposer#extractRoutingTargets} 统一提取。
     * </p>
     *
     * @param urlParams URL 参数
     * @return 路由目标集合（不可变），空集表示广播
     */
    protected Set<String> extractRoutingTargets(MultiValueMap<String, String> urlParams) {
        return composer.extractRoutingTargets(routingKeys, urlParams);
    }
}
