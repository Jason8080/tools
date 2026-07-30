package cn.gmlee.tools.im.core;

import cn.gmlee.tools.im.model.Msg;
import cn.gmlee.tools.im.model.TopicMessage;
import cn.gmlee.tools.im.spi.interceptor.RepeaterInterceptor;
import cn.gmlee.tools.im.util.BindingNames;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.util.MultiValueMap;
import reactor.core.publisher.Flux;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Repeater 框架骨架（IM 场景标准实现）.
 * <p>
 * 使用模板方法模式（Template Method Pattern），通过 {@code final} 方法保证
 * {@link RepeaterInterceptor} 拦截器链不可被绕过：
 * </p>
 * <ul>
 *   <li>{@link #send(TopicMessage)} — 织入 {@code beforeSend} → 调用 {@link #doSend(TopicMessage)}</li>
 *   <li>{@link #receive(TopicMessage)} — 调用 {@link #doReceive(TopicMessage)} → 织入 {@code afterReceive}</li>
 *   <li>{@link #subscribe(MultiValueMap)} — 调用 {@link #doSubscribe(MultiValueMap)} → 织入 {@code transformSubscribeStream}</li>
 * </ul>
 * <p>
 * 提供 IM 场景下的标准默认实现：
 * </p>
 * <ul>
 *   <li>{@link #doSend(TopicMessage)} — 通过 {@link StreamBridge} 发送到 MQ</li>
 *   <li>{@link #doReceive(TopicMessage)} — 通过函数式接口转发到 SSE 连接</li>
 *   <li>{@link #doSubscribe(MultiValueMap)} — 通过函数式接口提供实时消息流</li>
 * </ul>
 *
 * <h3>扩展方式</h3>
 * <p>
 * 继承此类并重写 {@code doSend} / {@code doReceive} / {@code doSubscribe} 自定义行为，
 * 拦截器自动生效。如需完全绕过拦截器，直接实现 {@link Repeater} 接口。
 * </p>
 *
 * @since 5.6.0
 */
@Slf4j
public abstract class ImRepeater implements Repeater {

    private final String topic;
    private final List<RepeaterInterceptor> interceptors;
    private final StreamBridge streamBridge;
    private final Consumer<TopicMessage<Msg>> publishFunction;
    private final Function<String, Flux<TopicMessage<Msg>>> subscribeFunction;

    /**
     * 创建 Repeater（使用上下文对象）.
     * <p>
     * 推荐用于自定义 Repeater 实现，通过 {@link RepeaterContext} 获取 SSE 能力。
     * 此构造函数不包含 {@code streamBridge}，适用于不需要 MQ 发送的场景。
     * 如需 MQ 发送能力，请重写 {@link #doSend(TopicMessage)} 方法。
     * </p>
     *
     * @param topic     Topic 名称
     * @param context   Repeater 上下文（封装 SSE 发布/订阅函数）
     * @param interceptors 拦截器列表（可为 null）
     */
    protected ImRepeater(String topic,
                         cn.gmlee.tools.im.spi.factory.RepeaterContext context,
                         List<RepeaterInterceptor> interceptors) {
        this(topic, null, context.getPublishFunction(), context.getSubscribeFunction(), interceptors);
    }

    /**
     * 创建 Repeater（完整参数，框架内部使用）.
     * <p>
     * 此构造函数包含 {@code streamBridge}，用于默认实现的 MQ 发送。
     * 自定义实现建议使用 {@link #ImRepeater(String, cn.gmlee.tools.im.spi.factory.RepeaterContext, List)}。
     * </p>
     *
     * @param topic               Topic 名称
     * @param streamBridge        Stream 桥接器（可为 null）
     * @param publishFunction     SSE 发布函数（通常为 SseConnectionManager::publish）
     * @param subscribeFunction   SSE 订阅函数（通常为 SseConnectionManager::subscribe）
     * @param interceptors        拦截器列表（可为 null）
     */
    protected ImRepeater(String topic,
                         StreamBridge streamBridge,
                         Consumer<TopicMessage<Msg>> publishFunction,
                         Function<String, Flux<TopicMessage<Msg>>> subscribeFunction,
                         List<RepeaterInterceptor> interceptors) {
        this.topic = topic;
        this.streamBridge = streamBridge;
        this.publishFunction = publishFunction;
        this.subscribeFunction = subscribeFunction;
        this.interceptors = interceptors != null ? interceptors : Collections.emptyList();
    }

    @Override
    public final String topic() {
        return topic;
    }

    /**
     * 发送消息（final，保证拦截器织入）.
     * <p>
     * 调用链：{@code beforeSend} → {@link #doSend(TopicMessage)}
     * </p>
     * <p>
     * 如果任一拦截器的 {@code beforeSend} 返回 {@code false}，消息将被拦截，不再发送。
     * </p>
     */
    @Override
    public final Serializable send(TopicMessage<Msg> message) {
        for (RepeaterInterceptor i : interceptors) {
            if (!i.beforeSend(message)) {
                log.debug("[ImRepeater] 消息被拦截器拦截: topic={}, id={}, interceptor={}",
                        topic, message.getId(), i.getClass().getSimpleName());
                return null;
            }
        }
        return doSend(message);
    }

    /**
     * 接收消息（final，保证拦截器织入）.
     * <p>
     * 调用链：{@link #doReceive(TopicMessage)} → {@code afterReceive}
     * </p>
     */
    @Override
    public final void receive(TopicMessage<Msg> message) {
        doReceive(message);
        for (RepeaterInterceptor i : interceptors) {
            i.afterReceive(message);
        }
    }

    /**
     * 订阅消息流（final，保证拦截器织入）.
     * <p>
     * 调用链：{@link #doSubscribe(MultiValueMap)} → {@code transformSubscribeStream} 逐级转换
     * </p>
     */
    @Override
    public final Flux<Msg> subscribe(MultiValueMap<String, String> urlParams) {
        Flux<Msg> stream = doSubscribe(urlParams);
        for (RepeaterInterceptor i : interceptors) {
            stream = i.transformSubscribeStream(topic, stream, urlParams);
        }
        return stream;
    }

    /**
     * 实际发送逻辑（IM 标准实现：通过 StreamBridge 发送到 MQ）.
     * <p>
     * 子类可重写以自定义发送行为。
     * </p>
     *
     * @param message 消息信封
     * @return 消息 ID
     */
    protected Serializable doSend(TopicMessage<Msg> message) {
        String bindingName = BindingNames.outputBinding(message.getTopic());
        streamBridge.send(bindingName, message);
        log.debug("[ImRepeater] 发送到 Stream: topic={}, id={}", message.getTopic(), message.getId());
        return message.getId();
    }

    /**
     * 实际接收逻辑（IM 标准实现：通过函数式接口转发到 SSE 连接）.
     * <p>
     * 子类可重写以自定义接收行为。
     * </p>
     *
     * @param message 来自 Stream 的消息
     */
    protected void doReceive(TopicMessage<Msg> message) {
        publishFunction.accept(message);
        log.debug("[ImRepeater] 转发到 SSE: topic={}, id={}", topic, message.getId());
    }

    /**
     * 实际订阅逻辑（IM 标准实现：通过函数式接口提供实时消息流）.
     * <p>
     * 子类可重写以自定义订阅行为（如拼接历史回放流）。
     * </p>
     *
     * @param urlParams 客户端请求参数
     * @return 消息流
     */
    protected Flux<Msg> doSubscribe(MultiValueMap<String, String> urlParams) {
        log.debug("[ImRepeater] 订阅消息流: topic={}", topic);
        return subscribeFunction.apply(topic)
                .map(TopicMessage::getMsg);
    }
}
