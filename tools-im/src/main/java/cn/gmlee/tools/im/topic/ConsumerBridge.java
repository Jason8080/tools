package cn.gmlee.tools.im.topic;

import cn.gmlee.tools.im.core.Repeater;
import cn.gmlee.tools.im.model.TopicMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;
import java.util.function.Consumer;

/**
 * Consumer 桥接器（包级私有）.
 * <p>
 * 将泛型 {@link Repeater} 桥接到 SSE 管道。
 * </p>
 *
 * <h3>设计说明</h3>
 * <ul>
 *   <li>接受 {@code Message<byte[]>}（Spring Cloud Stream 的原始消息格式），
 *       先反序列化为 {@code Map}，再手动构建 {@link TopicMessage}。
 *       这样可以避免 Jackson 反序列化 {@code Msg} 接口（抽象类型）的问题。</li>
 *   <li>{@code msg} 字段自动转换为 {@link MessageMap}（默认消息实现）。</li>
 *   <li>内部持有 raw {@link Repeater} 引用，通过运行时类型擦除安全分派消息。</li>
 * </ul>
 *
 * @since 5.6.0
 */
@Slf4j
@SuppressWarnings({"rawtypes", "unchecked"})
class ConsumerBridge implements Consumer<Message<byte[]>> {

    private final Repeater repeater;
    private final ObjectMapper objectMapper;

    ConsumerBridge(Repeater repeater, ObjectMapper objectMapper) {
        this.repeater = repeater;
        this.objectMapper = objectMapper;
    }

    @Override
    public void accept(Message<byte[]> message) {
        try {
            byte[] payload = message.getPayload();
            // 先反序列化为 Map，避免 Msg 接口的抽象类型问题
            Map<String, Object> map = objectMapper.readValue(payload, Map.class);
            // 使用 TopicMessage 的工厂方法统一处理字段映射
            TopicMessage topicMessage = TopicMessage.fromMap(map);
            repeater.receive(topicMessage);
        } catch (Exception e) {
            log.error("[ConsumerBridge] 消息反序列化失败: topic={}", repeater.topic(), e);
            throw new RuntimeException("消息反序列化失败", e);
        }
    }
}
