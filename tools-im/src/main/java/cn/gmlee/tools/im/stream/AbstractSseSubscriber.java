package cn.gmlee.tools.im.stream;

import cn.gmlee.tools.im.core.Msg;
import cn.gmlee.tools.im.core.Subscriber;
import cn.gmlee.tools.im.core.TopicMessage;
import cn.gmlee.tools.im.sse.SseConnectionManager;
import org.springframework.util.MultiValueMap;
import reactor.core.publisher.Flux;

/**
 * 基于 SSE 连接管理器的订阅者抽象基类.
 * <p>
 * 封装从 SSE 连接管理器获取 Flux 并提取消息体的逻辑，
 * 子类只需实现 {@link #topic()} 指定主题名称。
 * </p>
 *
 * <h3>泛型参数</h3>
 * <ul>
 *   <li>{@code MSG} — 消息类型，必须实现 {@link Msg}</li>
 * </ul>
 *
 * <h3>典型用法</h3>
 * <pre>{@code
 * @Component
 * public class OrderSubscriber extends AbstractSseSubscriber<Msg> {
 *     public OrderSubscriber(SseConnectionManager sseConnectionManager) {
 *         super(sseConnectionManager);
 *     }
 *
 *     @Override
 *     public String topic() {
 *         return "order.update";
 *     }
 * }
 * }</pre>
 *
 * @param <MSG> 消息类型
 */
public abstract class AbstractSseSubscriber<MSG extends Msg> implements Subscriber<MSG> {

    private final SseConnectionManager sseConnectionManager;

    /**
     * 创建 SSE 订阅者.
     *
     * @param sseConnectionManager SSE 连接管理器
     */
    protected AbstractSseSubscriber(SseConnectionManager sseConnectionManager) {
        this.sseConnectionManager = sseConnectionManager;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Flux<MSG> pull(MultiValueMap<String, String> urlParams) {
        return sseConnectionManager.subscribe(topic())
                .map(msg -> (MSG) msg.getMsg());
    }
}
