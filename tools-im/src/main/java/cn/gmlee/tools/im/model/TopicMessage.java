package cn.gmlee.tools.im.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.MultiValueMap;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopicMessage<ID extends Serializable, MSG extends Msg> implements Serializable {

    /**
     * 全局自增 ID 生成器.
     * <p>
     * 使用 AtomicLong 替代 System.currentTimeMillis()，避免同一毫秒内
     * 创建多条消息时 ID 冲突。序列号从当前时间戳开始，保证跨 JVM 重启后
     * ID 仍然单调递增（大部分情况下）。
     * </p>
     * <p>
     * <b>唯一性范围</b>：仅保证单 JVM 生命周期内唯一。分布式环境下可能存在 ID 冲突，
     * 建议开发者根据场景自定义 ID 生成策略（如 UUID、雪花算法），通过
     * {@code TopicMessage.builder().id(customId).build()} 覆盖默认值。
     * </p>
     */
    private static final AtomicLong ID_GENERATOR = new AtomicLong(System.currentTimeMillis());

    /**
     * 消息 ID.
     * <p>
     * 默认使用全局自增 ID（Long 类型，单 JVM 唯一）。
     * 泛型参数 {@code ID} 允许开发者自定义 ID 类型（如 UUID、雪花算法 ID）。
     * </p>
     */
    @SuppressWarnings("unchecked")
    @Builder.Default
    private ID id = (ID) (Serializable) ID_GENERATOR.incrementAndGet();
    private String topic;
    /**
     * 路由目标集合（定向投递）.
     * <p>
     * null 或空集 = 广播到 Topic 下所有连接；
     * 非空 = 仅投递给 routingKey 匹配的连接。
     * </p>
     */
    private Set<String> routingKeys;
    private MultiValueMap<String, String> urlParams;
    private MultiValueMap<String, String> headers;
    private MSG msg;
    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();
    public String topic() {
        return topic;
    }

    /**
     * 从 Map 构建 TopicMessage（反序列化辅助方法）.
     * <p>
     * 将 Map 中的字段映射到 TopicMessage 属性。主要用于 Spring Cloud Stream
     * 消息反序列化场景，避免 Jackson 直接反序列化泛型接口的问题。
     * </p>
     *
     * <h3>字段映射规则</h3>
     * <ul>
     *   <li>{@code id} — 支持 {@link Number} 和 {@link String} 类型</li>
     *   <li>{@code topic} — 字符串</li>
     *   <li>{@code msg} — {@link Map} 类型自动转换为 {@link MessageMap}</li>
     *   <li>{@code metadata} — {@link Map} 类型直接映射</li>
     *   <li>{@code routingKeys} — {@link java.util.Collection} 类型转换为 {@link Set}</li>
     * </ul>
     *
     * @param map JSON 反序列化后的 Map
     * @return TopicMessage 实例（raw type，泛型由调用方保证）
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static TopicMessage fromMap(Map<String, Object> map) {
        TopicMessage message = new TopicMessage();

        // id: Number 或 String
        Object id = map.get("id");
        if (id instanceof Number) {
            message.setId((Serializable) id);
        } else if (id instanceof String) {
            message.setId((String) id);
        }

        // topic: String
        Object topic = map.get("topic");
        if (topic instanceof String) {
            message.setTopic((String) topic);
        }

        // msg: Map → MessageMap
        Object msg = map.get("msg");
        if (msg instanceof Map) {
            message.setMsg(new MessageMap((Map<String, Object>) msg));
        }

        // metadata: Map
        Object metadata = map.get("metadata");
        if (metadata instanceof Map) {
            message.setMetadata((Map<String, Object>) metadata);
        }

        // routingKeys: Collection → Set
        Object routingKeys = map.get("routingKeys");
        if (routingKeys instanceof java.util.Collection) {
            message.setRoutingKeys(new java.util.HashSet<>((java.util.Collection<String>) routingKeys));
        }

        return message;
    }
}
