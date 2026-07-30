package cn.gmlee.tools.im.core;

import org.springframework.util.MultiValueMap;
import reactor.core.publisher.Flux;

/**
 * Repeater 拦截器.
 * <p>
 * 拦截消息流的关键节点，用于审计、持久化、回放等横切关注点。
 * 遵循拦截器模式（Interceptor Pattern），所有方法提供默认空实现，按需重写。
 * </p>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * @Component
 * public class RedisReplayInterceptor implements RepeaterInterceptor {
 *     @Autowired
 *     private RedisTemplate<String, byte[]> redisTemplate;
 *
 *     @Override
 *     public void beforeSend(TopicMessage<Msg> message) {
 *         // 发送前持久化到 Redis
 *         redisTemplate.opsForList().rightPush(key(message.getTopic()), serialize(message));
 *     }
 *
 *     @Override
 *     public Flux<Msg> transformSubscribeStream(String topic, Flux<Msg> stream, MultiValueMap<String, String> urlParams) {
 *         // 订阅时先回放历史消息
 *         Flux<Msg> history = loadFromRedis(topic);
 *         return Flux.concat(history, stream);
 *     }
 * }
 * }</pre>
 * <p>
 * 实现类注册为 Spring Bean 后，框架自动织入所有 {@link Repeater}，零配置生效。
 * </p>
 *
 * @since 5.6.0
 */
public interface RepeaterInterceptor {

    /**
     * 发送前拦截.
     * <p>
     * 在消息发送到 MQ 之前调用。可用于：消息持久化、审计日志、参数校验。
     * 抛出异常可阻止发送。
     * </p>
     *
     * @param message 待发送的消息
     */
    default void beforeSend(TopicMessage<Msg> message) {
    }

    /**
     * 接收后拦截（MQ 消费后、推送 SSE 前）.
     * <p>
     * 在从 MQ 接收到消息后、推送到 SSE 连接之前调用。
     * 可用于：消息持久化、指标收集、日志记录。
     * </p>
     *
     * @param message 接收到的消息
     */
    default void afterReceive(TopicMessage<Msg> message) {
    }

    /**
     * 转换订阅流.
     * <p>
     * 在返回 SSE 消息流之前调用，可对流进行转换。
     * 可用于：历史消息回放、消息过滤、流式转换。
     * 默认直接返回原始流。
     * </p>
     *
     * @param topic     Topic 名称
     * @param stream    原始消息流
     * @param urlParams 客户端请求参数
     * @return 转换后的消息流
     */
    default Flux<Msg> transformSubscribeStream(
            String topic,
            Flux<Msg> stream,
            MultiValueMap<String, String> urlParams) {
        return stream;
    }
}
