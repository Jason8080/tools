package cn.gmlee.tools.im.core;

import cn.gmlee.tools.im.model.Msg;
import cn.gmlee.tools.im.model.TopicMessage;
import cn.gmlee.tools.im.util.RoutingKeyExtractor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.MultiValueMap;

import java.io.Serializable;
import java.util.Collections;
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
 * @since 5.6.0
 */
@Slf4j
public abstract class ImPublisher extends AbstractTopic implements Publisher {

    /**
     * 路由键（默认 {@code ["me"]}）.
     * <p>
     * 从 URL 参数中提取哪些字段作为定向投递目标，与订阅方的 routingKey 提取逻辑对称。
     * 单键时支持多值（{@code ?me=alice&me=bob}），多键时按顺序以 {@code |} 拼接为单值。
     * </p>
     */
    protected final List<String> routingKeys;

    protected ImPublisher(String topic, Repeater repeater) {
        super(topic, repeater);
        this.routingKeys = Collections.singletonList("me");
    }

    protected ImPublisher(String topic, Supplier<Repeater> repeaterSupplier) {
        super(topic, repeaterSupplier);
        this.routingKeys = Collections.singletonList("me");
    }

    /**
     * 创建 Publisher（自定义路由键）.
     *
     * @param topic       Topic 名称
     * @param repeater    Repeater 实例
     * @param routingKeys 路由键列表（从 URL 参数提取，多键按顺序以 {@code |} 拼接）
     */
    protected ImPublisher(String topic, Repeater repeater, List<String> routingKeys) {
        super(topic, repeater);
        this.routingKeys = routingKeys != null && !routingKeys.isEmpty()
                ? routingKeys : Collections.singletonList("me");
    }

    /**
     * 创建 Publisher（自定义路由键，延迟解析 Repeater）.
     *
     * @param topic            Topic 名称
     * @param repeaterSupplier Repeater 延迟解析器
     * @param routingKeys      路由键列表（从 URL 参数提取，多键按顺序以 {@code |} 拼接）
     */
    protected ImPublisher(String topic, Supplier<Repeater> repeaterSupplier, List<String> routingKeys) {
        super(topic, repeaterSupplier);
        this.routingKeys = routingKeys != null && !routingKeys.isEmpty()
                ? routingKeys : Collections.singletonList("me");
    }

    @Override
    public Serializable push(MultiValueMap<String, String> urlParams, Msg msg) {
        TopicMessage<Msg> event = msg.build(urlParams);
        event.setTopic(topic);
        // 从 URL 参数提取定向投递目标（委托给 RoutingKeyExtractor 统一提取）
        Set<String> targets = extractRoutingTargets(urlParams);
        if (!targets.isEmpty()) {
            event.setRoutingKeys(targets);
        }
        Serializable id = resolveRepeater().send(event);
        log.debug("[ImPublisher] 发布消息: topic={}, id={}", topic, id);
        return id;
    }

    /**
     * 从 URL 参数提取路由目标集合.
     * <p>
     * 委托给 {@link cn.gmlee.tools.im.util.RoutingKeyExtractor#extractTargets} 统一提取，
     * 支持单键多值批量投递和多键按位置配对批量投递。
     * </p>
     *
     * @param urlParams URL 参数
     * @return 路由目标集合（不可变），空集表示广播
     */
    protected Set<String> extractRoutingTargets(MultiValueMap<String, String> urlParams) {
        return RoutingKeyExtractor.extractTargets(routingKeys, urlParams);
    }
}
