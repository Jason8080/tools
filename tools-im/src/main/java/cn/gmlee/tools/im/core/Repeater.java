package cn.gmlee.tools.im.core;

import cn.gmlee.tools.im.model.ConnectionMetadata;
import cn.gmlee.tools.im.model.Msg;
import cn.gmlee.tools.im.model.TopicMessage;
import cn.gmlee.tools.im.spi.interceptor.RepeaterInterceptor;
import org.springframework.util.MultiValueMap;
import reactor.core.publisher.Flux;

import java.io.Serializable;
import java.util.function.Consumer;

/**
 * 消息转发器（转/Transform）.
 * <p>
 * Topic 的消息枢纽，聚合了发送、消费、订阅三端的职责：
 * </p>
 * <ul>
 *   <li>{@link #send(TopicMessage)} — 将消息发送到 Stream (MQ)</li>
 *   <li>{@link #receive(TopicMessage)} — MQ 消费后转发到 SSE 连接</li>
 *   <li>{@link #subscribe(MultiValueMap, ConnectionMetadata)} — 提供 SSE 实时消息流</li>
 *   <li>{@link #accept(TopicMessage)} — {@link Consumer} 入口，MQ 消费者回调，默认委托给 {@link #receive}</li>
 * </ul>
 *
 * @since 5.6.0
 */
public interface Repeater extends Topic, Consumer<TopicMessage<Msg>> {

    /**
     * 发送消息到 Stream (MQ).
     *
     * @param message 消息信封
     * @return 消息 ID
     */
    Serializable send(TopicMessage<Msg> message);

    /**
     * 订阅 Topic 的实时消息流.
     *
     * @param urlParams 客户端请求参数（来自 SSE 订阅 URL）
     * @param metadata  连接元数据（身份标识等）
     * @return 消息流
     */
    Flux<Msg> subscribe(MultiValueMap<String, String> urlParams, ConnectionMetadata metadata);

    /**
     * 接收 Stream 消息并转发到 SSE.
     *
     * @param message 来自 Stream 的消息
     */
    void receive(TopicMessage<Msg> message);

    /**
     * {@link Consumer} 入口，MQ 消费者回调.
     *
     * @param message 来自 Stream 的消息
     */
    @Override
    default void accept(TopicMessage<Msg> message) {
        receive(message);
    }
}
