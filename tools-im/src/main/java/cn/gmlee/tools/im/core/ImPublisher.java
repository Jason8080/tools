package cn.gmlee.tools.im.core;

import cn.gmlee.tools.im.model.Msg;
import cn.gmlee.tools.im.model.TopicMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.MultiValueMap;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
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
     * 投递目标键（默认 {@code ["to"]}）.
     * <p>
     * 从 URL 参数中提取哪些字段作为定向投递目标。
     * 单键时支持多值（{@code ?to=alice&to=bob}），多键时按顺序以 {@code |} 拼接为单值。
     * </p>
     */
    protected final List<String> deliveryKeys;

    protected ImPublisher(String topic, Repeater repeater) {
        super(topic, repeater);
        this.deliveryKeys = Collections.singletonList("to");
    }

    protected ImPublisher(String topic, Supplier<Repeater> repeaterSupplier) {
        super(topic, repeaterSupplier);
        this.deliveryKeys = Collections.singletonList("to");
    }

    /**
     * 创建 Publisher（自定义投递目标键）.
     *
     * @param topic        Topic 名称
     * @param repeater     Repeater 实例
     * @param deliveryKeys 投递目标键列表（从 URL 参数提取，多键按顺序以 {@code |} 拼接）
     */
    protected ImPublisher(String topic, Repeater repeater, List<String> deliveryKeys) {
        super(topic, repeater);
        this.deliveryKeys = deliveryKeys != null && !deliveryKeys.isEmpty()
                ? deliveryKeys : Collections.singletonList("to");
    }

    /**
     * 创建 Publisher（自定义投递目标键，延迟解析 Repeater）.
     *
     * @param topic            Topic 名称
     * @param repeaterSupplier Repeater 延迟解析器
     * @param deliveryKeys     投递目标键列表（从 URL 参数提取，多键按顺序以 {@code |} 拼接）
     */
    protected ImPublisher(String topic, Supplier<Repeater> repeaterSupplier, List<String> deliveryKeys) {
        super(topic, repeaterSupplier);
        this.deliveryKeys = deliveryKeys != null && !deliveryKeys.isEmpty()
                ? deliveryKeys : Collections.singletonList("to");
    }

    @Override
    public Serializable push(MultiValueMap<String, String> urlParams, Msg msg) {
        TopicMessage<Msg> event = msg.build(urlParams);
        event.setTopic(topic);
        // 从 URL 参数提取定向投递目标
        // 单键：提取所有值（支持多目标，如 ?to=alice&to=bob）
        // 多键：按顺序提取各一个值，以 "|" 拼接（如 ?tenant=acme&room=lobby → "acme|lobby"）
        Set<String> targets = extractDeliveryTargets(urlParams);
        if (!targets.isEmpty()) {
            event.setTo(targets);
        }
        Serializable id = resolveRepeater().send(event);
        log.debug("[ImPublisher] 发布消息: topic={}, id={}", topic, id);
        return id;
    }

    /**
     * 从 URL 参数提取投递目标集合.
     * <p>
     * 单键模式（如 {@code ["to"]}）：提取该键的所有值，支持多目标投递。
     * 多键模式（如 {@code ["targetTenant", "targetRoom"]}）：按顺序提取各键的一个值，
     * 以 {@code |} 拼接为单值，与 {@code routingKeys} 的多维路由键拼接规则一致。
     * </p>
     *
     * @param urlParams URL 参数
     * @return 投递目标集合（不可变），空集表示广播
     */
    protected Set<String> extractDeliveryTargets(MultiValueMap<String, String> urlParams) {
        if (deliveryKeys.size() == 1) {
            // 单键：提取所有值（多目标）
            List<String> values = urlParams.get(deliveryKeys.getFirst());
            if (values == null || values.isEmpty()) {
                return Collections.emptySet();
            }
            return Set.copyOf(values);
        }
        // 多键：按顺序提取并拼接
        StringBuilder sb = new StringBuilder();
        for (String key : deliveryKeys) {
            String value = urlParams.getFirst(key);
            if (value != null) {
                if (!sb.isEmpty()) sb.append('|');
                sb.append(value);
            }
        }
        if (sb.isEmpty()) {
            return Collections.emptySet();
        }
        return Collections.singleton(sb.toString());
    }
}
