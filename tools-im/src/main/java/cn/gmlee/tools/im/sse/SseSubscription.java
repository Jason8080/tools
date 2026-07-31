package cn.gmlee.tools.im.sse;

import cn.gmlee.tools.im.model.Msg;
import cn.gmlee.tools.im.model.TopicMessage;
import lombok.Getter;
import reactor.core.publisher.Flux;

/**
 * SSE 订阅结果（内部使用）.
 * <p>
 * 封装订阅返回的 Flux 和对应的连接引用，供 {@link SseConnectionManager} 内部使用。
 * 连接引用会通过 Reactor Context 传递给调用方。
 * </p>
 *
 * @author tools-im
 */
@Getter
class SseSubscription {

    /**
     * 消息流
     */
    private final Flux<TopicMessage> flux;

    /**
     * 对应的连接引用
     */
    private final SseConnection connection;

    /**
     * 创建订阅结果.
     *
     * @param flux       消息流
     * @param connection 连接引用
     */
    SseSubscription(Flux<TopicMessage> flux, SseConnection connection) {
        this.flux = flux;
        this.connection = connection;
    }
}
