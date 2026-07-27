package cn.gmlee.tools.im.core;

import cn.gmlee.tools.im.event.TopicMessage;

import java.util.concurrent.Executor;

/**
 * Topic 订阅者接口
 * <p>
 * 实现此接口以订阅指定 Topic 的消息
 * </p>
 *
 * <pre>
 * &#64;Component
 * public class StockPriceHandler implements TopicSubscriber&lt;StockPrice&gt; {
 *
 *     &#64;Override
 *     public String getTopic() {
 *         return "stock.AAPL";
 *     }
 *
 *     &#64;Override
 *     public void onMessage(String topic, StockPrice price) {
 *         System.out.println("Received: " + price);
 *     }
 * }
 * </pre>
 *
 * @param <T> 消息类型
 * @author SseIm Framework
 * @since 1.0.0
 */
public interface TopicSubscriber<T> {

    /**
     * 获取订阅的 Topic
     * <p>
     * 支持通配符：
     * <ul>
     *   <li><code>stock.AAPL</code> - 精确匹配</li>
     *   <li><code>stock.*</code> - 单层通配符</li>
     *   <li><code>user.#.events</code> - 多层通配符</li>
     * </ul>
     *
     * @return Topic 名称或模式
     */
    String getTopic();

    /**
     * 处理消息
     *
     * @param topic   Topic 名称
     * @param message 消息内容
     */
    void onMessage(String topic, T message);

    /**
     * 处理 TopicMessage（可选，提供更多信息）
     *
     * @param message TopicMessage
     */
    default void onTopicMessage(TopicMessage<T> message) {
        onMessage(message.getTopic(), message.getPayload());
    }

    /**
     * 错误处理（可选）
     *
     * @param topic Topic 名称
     * @param error 异常
     */
    default void onError(String topic, Throwable error) {
        // 默认忽略，子类可覆写
    }

    /**
     * 获取执行器（可选）
     * <p>
     * 返回自定义线程池以异步处理消息
     * </p>
     *
     * @return Executor，返回 null 表示同步处理
     */
    default Executor getExecutor() {
        return null;
    }

    /**
     * 获取消息类型（用于反序列化）
     *
     * @return 消息类型 Class
     */
    @SuppressWarnings("unchecked")
    default Class<T> getMessageType() {
        return (Class<T>) Object.class;
    }

    /**
     * 获取订阅者 ID（用于标识）
     *
     * @return 订阅者 ID
     */
    default String getSubscriberId() {
        return getClass().getName();
    }

    /**
     * 获取优先级
     *
     * @return 优先级，数值越大优先级越高
     */
    default int getPriority() {
        return 0;
    }
}
