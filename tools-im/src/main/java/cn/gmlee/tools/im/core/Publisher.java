package cn.gmlee.tools.im.core;

import cn.gmlee.tools.im.model.Msg;
import cn.gmlee.tools.im.model.TopicMessage;
import org.springframework.util.MultiValueMap;

import java.io.Serializable;

/**
 * 消息发布器（进/Ingress）.
 * <p>
 * Topic 的消息入口点。负责接收 HTTP PUSH 请求的消息载荷，
 * 将其注入到 Stream 中。每个 Topic 对应一个 Publisher。
 * </p>
 *
 * <h3>默认实现</h3>
 * <p>
 * 框架自动创建 {@code DefaultPublisher}，将消息包装为 {@link TopicMessage}
 * 并通过 {@code StreamBridge} 发送到 MQ。
 * </p>
 *
 * <h3>自定义扩展</h3>
 * <pre>{@code
 * @Component
 * public class RedisPublisher implements Publisher {
 *     @Override public String topic() { return "im.chat"; }
 *     @Override public Serializable push(MultiValueMap<String,String> params, Msg msg) {
 *         redisStore.save(msg);  // 自定义逻辑：存储到 Redis
 *         // 委托默认实现发送到 MQ
 *         Publisher delegate = topicRegistry.createDefaultPublisher(topic());
 *         return delegate.push(params, msg);
 *     }
 * }
 * }</pre>
 * <p>
 * 自定义实现注册为 Spring Bean 后，框架通过 {@link #topic()} 自动匹配到对应 Topic，零配置生效。
 * </p>
 *
 * @since 5.6.0
 */
public interface Publisher extends Topic {

    /**
     * 发布消息.
     * <p>
     * 接收 HTTP PUSH 请求的消息载荷，执行消息注入逻辑，返回消息 ID。
     * 默认实现将消息包装为 {@link TopicMessage} 并通过 StreamBridge 发送到 MQ。
     * </p>
     *
     * @param urlParams URL 查询参数（来自 HTTP 请求）
     * @param msg       消息载荷
     * @return 消息 ID
     */
    Serializable push(MultiValueMap<String, String> urlParams, Msg msg);
}
