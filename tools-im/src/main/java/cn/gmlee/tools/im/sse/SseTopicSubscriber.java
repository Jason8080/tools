package cn.gmlee.tools.im.sse;

import cn.gmlee.tools.im.core.TopicRouter;
import cn.gmlee.tools.im.core.TopicSubscriber;
import cn.gmlee.tools.im.event.TopicMessage;
import lombok.extern.slf4j.Slf4j;

/**
 * SSE Topic 订阅者
 * <p>
 * 桥接 TopicRouter 和 SseConnectionManager，
 * 将所有路由的消息推送到 SSE 客户端
 * </p>
 *
 * @author SseIm Framework
 * @since 1.0.0
 */
@Slf4j
public class SseTopicSubscriber implements TopicSubscriber<Object> {

    private final SseConnectionManager connectionManager;

    public SseTopicSubscriber(SseConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    public String getTopic() {
        // 订阅所有 Topic（使用多层通配符）
        return "#";
    }

    @Override
    public void onMessage(String topic, Object message) {
        // 这个方法不会被调用，因为我们在 onTopicMessage 中处理
    }

    @Override
    public void onTopicMessage(TopicMessage<Object> message) {
        // 将消息推送到 SSE 连接
        connectionManager.publish(message);
        log.debug("Pushed message to SSE clients for topic: {}", message.getTopic());
    }

    @Override
    public int getPriority() {
        // 最低优先级，确保业务订阅者先处理
        return Integer.MIN_VALUE;
    }

    @Override
    public String getSubscriberId() {
        return "SseTopicSubscriber";
    }
}
