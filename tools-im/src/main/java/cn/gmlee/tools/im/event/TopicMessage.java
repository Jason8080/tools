package cn.gmlee.tools.im.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Topic 消息封装
 *
 * @author SseIm Framework
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopicMessage<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 消息唯一 ID
     */
    @Builder.Default
    private String id = UUID.randomUUID().toString();

    /**
     * Topic 名称
     */
    private String topic;

    /**
     * 消息负载
     */
    private T payload;

    /**
     * 消息时间戳
     */
    @Builder.Default
    private long timestamp = Instant.now().toEpochMilli();

    /**
     * 消息来源实例 ID
     */
    private String sourceInstanceId;

    /**
     * 消息元数据
     */
    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();

    /**
     * 创建消息
     */
    public static <T> TopicMessage<T> of(String topic, T payload) {
        return TopicMessage.<T>builder()
                .topic(topic)
                .payload(payload)
                .build();
    }

    /**
     * 创建带元数据的消息
     */
    public static <T> TopicMessage<T> of(String topic, T payload, Map<String, Object> metadata) {
        return TopicMessage.<T>builder()
                .topic(topic)
                .payload(payload)
                .metadata(metadata != null ? metadata : new HashMap<>())
                .build();
    }

    /**
     * 添加元数据
     */
    public TopicMessage<T> withMetadata(String key, Object value) {
        if (this.metadata == null) {
            this.metadata = new HashMap<>();
        }
        this.metadata.put(key, value);
        return this;
    }
}
