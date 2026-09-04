package cn.gmlee.tools.im.core;

import cn.gmlee.tools.im.model.ConnectionMetadata;
import cn.gmlee.tools.im.model.Msg;
import cn.gmlee.tools.im.model.TopicMessage;
import org.springframework.util.MultiValueMap;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.Serializable;
import java.util.function.Consumer;

/**
 * 消息转发器（转/Transform）.
 * <p>
 * Topic 的消息枢纽，聚合了发送、消费、订阅三端的职责：
 * </p>
 * <ul>
 *   <li>{@link #send(TopicMessage)} — 将消息发送到 Stream (MQ)，异步返回消息 ID</li>
 *   <li>{@link #receive(TopicMessage)} — MQ 消费后转发到 SSE 连接</li>
 *   <li>{@link #subscribe(MultiValueMap, ConnectionMetadata)} — 提供 SSE 实时消息流</li>
 *   <li>{@link #accept(TopicMessage)} — {@link Consumer} 入口，MQ 消费者回调，默认委托给 {@link #receive}</li>
 * </ul>
 *
 * @param <ID>  消息 ID 类型
 * @param <MSG> 消息载荷类型
 * @since 5.6.0
 */
public interface Repeater<ID extends Serializable, MSG extends Msg> extends Topic, Consumer<TopicMessage<ID, MSG>> {

    /**
     * 发送消息到 Stream (MQ).
     * <p>
     * 异步执行，不阻塞调用线程。拦截器链（{@code beforeSend}）中的异步 I/O
     * （如 Redis 查询、数据库写入）在此 Mono 内完成，不会阻塞 WebFlux 事件循环。
     * </p>
     *
     * @param message 消息信封
     * @return 消息 ID（异步）
     */
    Mono<ID> send(TopicMessage<ID, MSG> message);

    /**
     * 订阅 Topic 的实时消息流.
     * <p>
     * 返回完整消息信封（含 ID），供下游输出 SSE {@code id:} 字段与断点续传；
     * 载荷通过 {@link TopicMessage#getMsg()} 获取。
     * </p>
     *
     * @param urlParams 客户端请求参数（来自 SSE 订阅 URL）
     * @param metadata  连接元数据（身份标识、续传位点等）
     * @return 消息信封流
     * @since 5.7.0 由 {@code Flux<MSG>} 升级为信封流（断点续传契约）
     */
    Flux<TopicMessage<ID, MSG>> subscribe(MultiValueMap<String, String> urlParams, ConnectionMetadata metadata);

    /**
     * 接收 Stream 消息并转发到 SSE.
     *
     * @param message 来自 Stream 的消息
     */
    void receive(TopicMessage<ID, MSG> message);

    /**
     * {@link Consumer} 入口，MQ 消费者回调.
     *
     * @param message 来自 Stream 的消息
     */
    @Override
    default void accept(TopicMessage<ID, MSG> message) {
        receive(message);
    }
}
