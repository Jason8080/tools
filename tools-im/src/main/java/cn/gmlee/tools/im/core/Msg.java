package cn.gmlee.tools.im.core;

import org.springframework.util.MultiValueMap;

import java.io.Serializable;

/**
 * 消息.
 * <p>
 * 消息 POJO 实现此接口即可作为 Topic 消息载荷。
 * 默认的 {@link #build(MultiValueMap)} 实现自动将自身包装为 {@link TopicMessage}，
 * 消息类无需关心 topic 名称（由框架通过 {@code EndpointRouter} 注入）。
 * </p>
 *
 * <h3>典型用法</h3>
 * <pre>{@code
 * @Data
 * public class ChatMsg implements Msg {
 *     private String from;
 *     private String content;
 * }
 * }</pre>
 *
 * <h3>自定义构建</h3>
 * <p>
 * 如需在消息中附加 metadata 等自定义逻辑，可重写 {@link #build(MultiValueMap)}：
 * </p>
 * <pre>{@code
 * @Override
 * public <MSG extends Msg> TopicMessage<MSG> build(MultiValueMap<String, String> urlParams) {
 *     TopicMessage<ChatMsg> event = TopicMessage.<ChatMsg>builder()
 *             .msg(this)
 *             .urlParams(urlParams)
 *             .build();
 *     event.getMetadata().put("timestamp", System.currentTimeMillis());
 *     return (TopicMessage<MSG>) event;
 * }
 * }</pre>
 */
public interface Msg extends Serializable {
    /**
     * 将消息自身包装为 {@link TopicMessage}.
     * <p>
     * 默认实现自动完成包装，消息类无需实现此方法。
     * topic 名称由框架在 {@code EndpointRouter} 中注入。
     * </p>
     *
     * @param <MSG>     消息类型
     * @param urlParams URL 参数
     * @return 包装后的 TopicMessage
     */
    @SuppressWarnings("unchecked")
    default <MSG extends Msg> TopicMessage<MSG> build(MultiValueMap<String, String> urlParams) {
        TopicMessage<MSG> event = new TopicMessage<>();
        event.setMsg((MSG) this);
        event.setUrlParams(urlParams);
        return event;
    }
}
