package cn.gmlee.tools.im.core;

import cn.gmlee.tools.im.event.TopicMessage;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Topic 发布者接口
 * <p>
 * 提供向指定 Topic 推送消息的能力
 * </p>
 *
 * <pre>
 * &#64;Service
 * public class StockService {
 *     &#64;Autowired
 *     private TopicPublisher publisher;
 *
 *     public void updatePrice(String symbol, BigDecimal price) {
 *         publisher.publish("stock." + symbol, new StockPrice(symbol, price));
 *     }
 * }
 * </pre>
 *
 * @author SseIm Framework
 * @since 1.0.0
 */
public interface TopicPublisher {

    /**
     * 发布消息到指定 Topic
     *
     * @param topic   Topic 名称
     * @param message 消息内容
     */
    void publish(String topic, Object message);

    /**
     * 发布消息到多个 Topic
     *
     * @param topics  Topic 列表
     * @param message 消息内容
     */
    void publish(List<String> topics, Object message);

    /**
     * 发布带元数据的消息
     *
     * @param topic    Topic 名称
     * @param message  消息内容
     * @param metadata 元数据
     */
    void publish(String topic, Object message, Map<String, Object> metadata);

    /**
     * 批量发布消息
     *
     * @param topic    Topic 名称
     * @param messages 消息列表
     */
    void publishBatch(String topic, List<?> messages);

    /**
     * 异步发布消息
     *
     * @param topic   Topic 名称
     * @param message 消息内容
     * @return CompletableFuture
     */
    CompletableFuture<Void> publishAsync(String topic, Object message);

    /**
     * 检查 Topic 是否有订阅者
     *
     * @param topic Topic 名称
     * @return 是否有订阅者
     */
    boolean hasSubscribers(String topic);

    /**
     * 获取 Topic 订阅者数量
     *
     * @param topic Topic 名称
     * @return 订阅者数量
     */
    int getSubscriberCount(String topic);

    /**
     * 发布 TopicMessage 对象
     *
     * @param message TopicMessage
     */
    void publishMessage(TopicMessage<?> message);
}
