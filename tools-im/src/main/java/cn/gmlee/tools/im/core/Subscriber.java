package cn.gmlee.tools.im.core;

import org.springframework.util.MultiValueMap;
import reactor.core.publisher.Flux;

import java.io.Serializable;

/**
 * 订阅者.
 * <p>
 * 泛型参数 {@code MSG} 表示消息类型，{@link #pull(MultiValueMap)} 返回 {@code Flux<MSG>}，
 * 表示消息的响应式流。
 * </p>
 *
 * <h3>设计说明</h3>
 * <p>
 * SSE（Server-Sent Events）本质是流式数据传输，因此 {@code pull()} 方法直接返回
 * {@link Flux} 而非单个消息。这使得类型语义更清晰：
 * </p>
 * <ul>
 *   <li>{@code Subscriber<Msg>} — 订阅消息类型为 {@code Msg} 的流</li>
 *   <li>{@code pull()} 返回 {@code Flux<Msg>} — 消息流</li>
 * </ul>
 *
 * <h3>典型用法</h3>
 * <pre>{@code
 * @Component
 * public class OrderSubscriber implements Subscriber<OrderMsg> {
 *     @Override
 *     public String topic() { return "order.update"; }
 *
 *     @Override
 *     public Flux<OrderMsg> pull(MultiValueMap<String, String> urlParams) {
 *         return sseConnectionManager.subscribe(topic())
 *                 .map(TopicMessage::getMsg);
 *     }
 * }
 * }</pre>
 *
 * @param <MSG> 消息类型
 */
public interface Subscriber<MSG> extends Topic, Serializable {

    /**
     * 拉取消息流.
     *
     * @param urlParams URL 参数
     * @return 消息响应式流
     */
    Flux<MSG> pull(MultiValueMap<String, String> urlParams);
}
