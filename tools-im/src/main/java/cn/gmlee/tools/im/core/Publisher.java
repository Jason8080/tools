package cn.gmlee.tools.im.core;

import cn.gmlee.tools.im.model.Msg;
import cn.gmlee.tools.im.model.TopicMessage;
import org.springframework.util.MultiValueMap;
import reactor.core.publisher.Mono;

import java.io.Serializable;
import java.util.Set;

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
 * 并通过 {@code MessageSender} 发送到 MQ。
 * </p>
 *
 * <h3>自定义扩展</h3>
 * <pre>{@code
 * @Component
 * public class RedisPublisher<ID extends Serializable> implements Publisher<ID, MessageMap> {
 *     @Override public String topic() { return "im.chat"; }
 *     @Override public Mono<ID> push(MultiValueMap<String,String> params, MessageMap msg) {
 *         return redisStore.saveAsync(msg)   // 自定义逻辑：异步存储到 Redis
 *             .then(Mono.fromSupplier(() -> {
 *                 Publisher<ID, MessageMap> delegate = topicRegistry.createDefaultPublisher(topic());
 *                 return delegate.push(params, msg);  // 委托默认实现发送到 MQ
 *             }));
 *     }
 * }
 * }</pre>
 * <p>
 * 自定义实现注册为 Spring Bean 后，框架通过 {@link #topic()} 自动匹配到对应 Topic，零配置生效。
 * </p>
 *
 * @param <ID>  消息 ID 类型
 * @param <MSG> 消息载荷类型
 * @since 5.6.0
 */
public interface Publisher<ID extends Serializable, MSG extends Msg> extends Topic {

    /**
     * 发布消息.
     * <p>
     * 接收 HTTP PUSH 请求的消息载荷，执行消息注入逻辑，异步返回消息 ID。
     * 默认实现将消息包装为 {@link TopicMessage} 并通过 MessageSender 发送到 MQ。
     * </p>
     * <p>
     * 返回 {@link Mono} 异步执行，不阻塞调用线程。拦截器链中的异步 I/O
     * （如 Redis 查询、数据库写入）在此 Mono 内完成，不会阻塞 WebFlux 事件循环。
     * </p>
     *
     * @param urlParams URL 查询参数（来自 HTTP 请求）
     * @param msg       消息载荷
     * @return 消息 ID（异步）
     */
    Mono<ID> push(MultiValueMap<String, String> urlParams, MSG msg);

    /**
     * 发布消息（指定路由目标）.
     * <p>
     * 供框架层（如 {@code EndpointRouter}）传递端点级路由键配置提取的定向投递目标。
     * 默认实现将 {@code routingKeys} 设置到 {@link TopicMessage} 后委托 {@link #send(TopicMessage)}。
     * </p>
     * <p>
     * 自定义 Publisher 推荐重写 {@link #send(TopicMessage)} 作为统一扩展点，
     * {@code push()} 的两个重载均委托到 {@code send()}，重写一次即可覆盖所有入口。
     * </p>
     *
     * @param urlParams   URL 查询参数
     * @param msg         消息载荷
     * @param routingKeys 定向投递目标集合（空集或 null 表示广播）
     * @return 消息 ID（异步）
     * @since 5.6.0
     */
    default Mono<ID> push(MultiValueMap<String, String> urlParams, MSG msg, Set<String> routingKeys) {
        TopicMessage<ID, MSG> event = msg.build(urlParams);
        event.setTopic(topic());
        if (routingKeys != null && !routingKeys.isEmpty()) {
            event.setRoutingKeys(routingKeys);
        }
        return send(event);
    }

    /**
     * 发送消息信封.
     * <p>
     * 将 {@link TopicMessage} 中的 routingKeys 提取后委托 {@link #push(MultiValueMap, Msg, Set)}。
     * 自定义 Publisher 推荐重写此方法作为统一扩展点：{@code push()} 的两个重载均委托到此方法，
     * 重写一次即可覆盖所有入口。
     * </p>
     * <p>
     * 默认实现委托 {@link #push(MultiValueMap, Msg, Set)}，routingKeys 从 message 提取。
     * 仅实现 {@link #push(MultiValueMap, Msg)} 的旧自定义 Publisher 需同时重写此方法以避免 routingKeys 丢失。
     * </p>
     *
     * @param message 消息信封
     * @return 消息 ID（异步）
     */
    default Mono<ID> send(TopicMessage<ID, MSG> message) {
        return push(message.getUrlParams(), message.getMsg(), message.getRoutingKeys());
    }
}
