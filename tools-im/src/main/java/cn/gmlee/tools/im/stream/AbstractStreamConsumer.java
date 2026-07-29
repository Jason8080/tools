package cn.gmlee.tools.im.stream;

import cn.gmlee.tools.im.core.Msg;
import cn.gmlee.tools.im.core.TopicMessage;
import cn.gmlee.tools.im.sse.SseConnectionManager;

import java.util.function.Consumer;

/**
 * 基于 SSE 连接管理器的消费者抽象基类.
 * <p>
 * 封装从 Spring Cloud Stream 接收消息并转发到本地 SSE 连接管理器的逻辑。
 * 子类无需重写任何方法，只需通过构造器注入 {@link SseConnectionManager}。
 * </p>
 *
 * <h3>典型用法</h3>
 * <pre>{@code
 * @Component
 * public class OrderConsumer extends AbstractStreamConsumer {
 *     public OrderConsumer(SseConnectionManager sseConnectionManager) {
 *         super(sseConnectionManager);
 *     }
 *     // 无需重写 accept()，基类已实现
 * }
 * }</pre>
 */
public abstract class AbstractStreamConsumer implements Consumer<TopicMessage<Msg>> {

    private final SseConnectionManager sseConnectionManager;

    /**
     * 创建 Stream 消费者.
     *
     * @param sseConnectionManager SSE 连接管理器
     */
    protected AbstractStreamConsumer(SseConnectionManager sseConnectionManager) {
        this.sseConnectionManager = sseConnectionManager;
    }

    @Override
    public void accept(TopicMessage<Msg> message) {
        if (message != null) {
            sseConnectionManager.publish(message);
        }
    }
}
