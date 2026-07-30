package cn.gmlee.tools.im.core;

import org.springframework.util.MultiValueMap;
import reactor.core.publisher.Flux;

/**
 * 消息订阅器（出/Egress）.
 * <p>
 * Topic 的消息出口点。负责处理 HTTP PULL 请求，返回 SSE 消息流。
 * 每个 Topic 对应一个 Subscriber。
 * </p>
 *
 * <h3>默认实现</h3>
 * <p>
 * {@code DefaultSubscriber} 委托 {@code Repeater.subscribe()} 返回实时消息流。
 * </p>
 *
 * <h3>自定义扩展</h3>
 * <pre>{@code
 * @Component
 * public class RedisSubscriber implements Subscriber {
 *     @Override public String topic() { return "im.chat"; }
 *     @Override public Flux<Msg> pull(MultiValueMap<String,String> params) {
 *         // 回放 Redis 中的历史消息
 *         Flux<Msg> replay = redisStore.load(topic());
 *         // 拼接实时消息流
 *         Subscriber delegate = topicRegistry.createDefaultSubscriber(topic());
 *         return Flux.concat(replay, delegate.pull(params));
 *     }
 * }
 * }</pre>
 * <p>
 * 自定义实现注册为 Spring Bean 后，框架通过 {@link #topic()} 自动匹配到对应 Topic，零配置生效。
 * </p>
 *
 * @since 5.6.0
 */
public interface Subscriber extends Topic {

    /**
     * 订阅消息流.
     * <p>
     * 返回 Topic 的消息流。默认实现委托 {@code Repeater.subscribe()} 返回实时 SSE 流。
     * 自定义实现可在实时流前拼接历史回放流。
     * </p>
     *
     * @param urlParams URL 查询参数（来自 HTTP 请求）
     * @return 消息流
     */
    Flux<Msg> pull(MultiValueMap<String, String> urlParams);
}
