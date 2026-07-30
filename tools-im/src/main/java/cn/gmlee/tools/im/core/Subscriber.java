package cn.gmlee.tools.im.core;

import cn.gmlee.tools.im.model.ConnectionMetadata;
import cn.gmlee.tools.im.model.Msg;
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
 * @since 5.6.0
 */
public interface Subscriber extends Topic {

    /**
     * 订阅消息流.
     *
     * @param urlParams URL 查询参数（来自 HTTP 请求）
     * @param metadata  连接元数据（身份标识等，来自 HTTP Headers）
     * @return 消息流
     */
    Flux<Msg> pull(MultiValueMap<String, String> urlParams, ConnectionMetadata metadata);
}
