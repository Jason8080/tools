package cn.gmlee.tools.im.core;

import cn.gmlee.tools.im.model.ConnectionMetadata;
import cn.gmlee.tools.im.model.Msg;
import cn.gmlee.tools.im.model.TopicMessage;
import cn.gmlee.tools.im.resume.ResumeSupport;
import cn.gmlee.tools.im.spi.factory.RepeaterContext;
import cn.gmlee.tools.im.spi.interceptor.RepeaterInterceptor;
import cn.gmlee.tools.im.spi.interceptor.SubscribeContext;
import cn.gmlee.tools.im.util.BindingNames;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.MultiValueMap;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;

/**
 * Repeater 框架骨架（IM 场景标准实现）.
 * <p>
 * 使用模板方法模式（Template Method Pattern），通过 {@code final} 方法保证
 * {@link RepeaterInterceptor} 拦截器链与续传挂点不可被绕过：
 * </p>
 * <ul>
 *   <li>{@link #send(TopicMessage)} — 织入 {@code beforeSend} → 调用 {@link #doSend(TopicMessage)}
 *       → 成功后 fire-and-forget 写入历史存储（续传写入侧挂点）</li>
 *   <li>{@link #receive(TopicMessage)} — 调用 {@link #doReceive(TopicMessage)} → 织入 {@code afterReceive}</li>
 *   <li>{@link #subscribe(MultiValueMap, ConnectionMetadata)} — 调用 {@link #doSubscribe(MultiValueMap, ConnectionMetadata)}
 *       → 断点续传包装（{@link ResumeSupport}）→ 织入 {@code transformSubscribeStream}</li>
 * </ul>
 * <p>
 * <b>泛型设计</b>：公共方法（{@code send/receive/subscribe}）使用泛型 {@code <ID, MSG>}，
 * 内部方法（{@code doSend/doReceive/doSubscribe}）和字段使用 {@code TopicMessage}，
 * 与 SSE 管道对齐。子类重写 {@code doXxx} 方法无需处理泛型。
 * </p>
 * <p>
 * <b>信封契约（v5.7.0）</b>：{@link #subscribe} 返回完整 {@link TopicMessage} 信封流
 * （含消息 ID），供下游输出 SSE {@code id:} 字段与断点续传；
 * 拦截器 {@code transformSubscribeStream} 同样收到信封流。
 * </p>
 *
 * @param <ID>  消息 ID 类型
 * @param <MSG> 消息载荷类型
 * @since 5.6.0
 */
@Slf4j
public abstract class ImRepeater<ID extends Serializable, MSG extends Msg> implements Repeater<ID, MSG> {

    private final String topic;
    private final List<RepeaterInterceptor> interceptors;
    private final MessageSender messageSender;
    private final Consumer<TopicMessage> publishFunction;
    private final BiFunction<String, ConnectionMetadata, Flux<TopicMessage>> subscribeFunction;
    private final ResumeSupport resumeSupport;

    /**
     * 创建 Repeater（使用上下文对象）.
     * <p>
     * 推荐用于自定义 Repeater 实现，通过 {@link RepeaterContext} 获取 SSE 能力与续传支持。
     * 此构造函数不包含 {@code messageSender}，适用于不需要 MQ 发送的场景。
     * 如需 MQ 发送能力，请重写 {@link #doSend(TopicMessage)} 方法。
     * </p>
     *
     * @param topic        Topic 名称
     * @param context      Repeater 上下文（封装 SSE 发布/订阅函数与续传支持）
     * @param interceptors 拦截器列表（可为 null）
     */
    protected ImRepeater(String topic,
                         RepeaterContext context,
                         List<RepeaterInterceptor> interceptors) {
        this(topic, null, context.getPublishFunction(), context.getSubscribeFunction(),
                context.getResumeSupport(), interceptors);
    }

    /**
     * 创建 Repeater（完整参数，框架内部使用）.
     * <p>
     * 此构造函数包含 {@code messageSender}，用于默认实现的 MQ 发送。
     * 自定义实现建议使用 {@link #ImRepeater(String, RepeaterContext, List)}。
     * </p>
     *
     * @param topic             Topic 名称
     * @param messageSender     消息传输器（可为 null，CLUSTER 模式由 StreamBridge 适配）
     * @param publishFunction   SSE 发布函数（通常为 SseConnectionManager::publish）
     * @param subscribeFunction SSE 订阅函数（通常为 SseConnectionManager::subscribe）
     * @param resumeSupport     断点续传支持（可为 null，null 表示不启用续传）
     * @param interceptors      拦截器列表（可为 null）
     * @since 5.7.0 新增 {@code resumeSupport} 参数
     */
    protected ImRepeater(String topic,
                         MessageSender messageSender,
                         Consumer<TopicMessage> publishFunction,
                         BiFunction<String, ConnectionMetadata, Flux<TopicMessage>> subscribeFunction,
                         ResumeSupport resumeSupport,
                         List<RepeaterInterceptor> interceptors) {
        this.topic = topic;
        this.messageSender = messageSender;
        this.publishFunction = publishFunction;
        this.subscribeFunction = subscribeFunction;
        this.resumeSupport = resumeSupport;
        this.interceptors = interceptors != null ? interceptors : Collections.emptyList();
    }

    @Override
    public final String topic() {
        return topic;
    }

    /**
     * 发送消息（final，保证拦截器织入与续传写入挂点）.
     * <p>
     * 调用链：{@code beforeSend} → {@link #doSend(TopicMessage)} → 历史存储异步落盘
     * </p>
     * <p>
     * 返回 {@link Mono} 异步执行，不阻塞调用线程。拦截器链中的异步 I/O
     * （如 Redis 查询）在此 Mono 内完成，不会阻塞 WebFlux 事件循环。
     * 如果任一拦截器的 {@code beforeSend} 返回 {@code Mono.just(false)}，消息将被拦截，不再发送。
     * </p>
     * <p>
     * <b>续传写入挂点</b>：发送成功后（{@code id != null}）fire-and-forget 调用
     * {@link ResumeSupport#storeAsync}；CLUSTER / STANDALONE 两种模式行为一致。
     * </p>
     */
    @Override
    @SuppressWarnings("unchecked")
    public final Mono<ID> send(TopicMessage<ID, MSG> message) {
        // message (TopicMessage<ID,MSG>) 可赋值给 TopicMessage 参数（协变通配符 + 类型擦除）
        Mono<Boolean> chain = Mono.just(true);
        for (RepeaterInterceptor i : interceptors) {
            chain = chain.flatMap(allowed -> {
                if (!allowed) return Mono.just(false);
                return i.beforeSend(message);
            });
        }

        return chain.filter(Boolean::booleanValue)
                .flatMap(allowed -> doSend(message))
                .map(id -> (ID) id)  // Serializable → ID: unchecked cast（ClusterRepeater 时退化为 no-op）
                .doOnSuccess(id -> {
                    // 仅在消息真正发送后（未被拦截）写入历史存储；异常不传播
                    if (id != null && resumeSupport != null) {
                        resumeSupport.storeAsync(message);
                    }
                });
    }

    /**
     * 接收消息（final，保证拦截器织入）.
     * <p>
     * 调用链：{@link #doReceive(TopicMessage)} → {@code afterReceive}
     * </p>
     */
    @Override
    public final void receive(TopicMessage<ID, MSG> message) {
        doReceive(message);
        // 异步执行拦截器，不阻塞主流程
        Mono<Void> chain = Mono.empty();
        for (RepeaterInterceptor i : interceptors) {
            chain = chain.then(i.afterReceive(message));
        }
        chain.subscribe(
            unused -> {},
            error -> log.error("[ImRepeater] afterReceive 拦截器异常: topic={}, id={}",
                    topic, message.getId(), error)
        );
    }

    /**
     * 订阅消息信封流（final，保证续传包装与拦截器织入）.
     * <p>
     * 调用链：{@link #doSubscribe(MultiValueMap, ConnectionMetadata)}（实时信封流）
     * → {@link ResumeSupport#wrapIfResuming}（携带 Last-Event-ID 时零间隙续接）
     * → {@code transformSubscribeStream} 逐级转换
     * </p>
     *
     * @since 5.7.0 返回信封流（含消息 ID），支持 SSE {@code id:} 输出与断点续传
     */
    @Override
    @SuppressWarnings("unchecked")
    public final Flux<TopicMessage<ID, MSG>> subscribe(MultiValueMap<String, String> urlParams,
                                                       ConnectionMetadata metadata) {
        Flux<TopicMessage> stream = doSubscribe(urlParams, metadata);
        if (resumeSupport != null) {
            stream = (Flux<TopicMessage>) (Flux<?>) resumeSupport.wrapIfResuming(
                    (Flux<TopicMessage<?, ?>>) (Flux<?>) stream, topic, metadata);
        }
        SubscribeContext context = SubscribeContext.of(topic, urlParams, metadata);
        for (RepeaterInterceptor i : interceptors) {
            stream = (Flux<TopicMessage>) (Flux<?>) i.transformSubscribeStream(
                    context, (Flux<TopicMessage<?, ?>>) (Flux<?>) stream);
        }
        return (Flux<TopicMessage<ID, MSG>>) (Flux<?>) stream;
    }

    /**
     * 实际发送逻辑（IM 标准实现：通过 MessageSender 发送到 MQ）.
     * <p>
     * 子类可重写以自定义发送行为。参数和返回值使用 {@code TopicMessage} / {@code Mono<Serializable>}，
     * 与 SSE 管道对齐，子类无需处理泛型。
     * </p>
     *
     * @param message 消息信封
     * @return 消息 ID（异步）
     */
    protected Mono<Serializable> doSend(TopicMessage message) {
        return Mono.fromCallable(() -> {
            String bindingName = BindingNames.outputBinding(message.getTopic());
            messageSender.send(bindingName, message);
            log.debug("[ImRepeater] 发送到 Stream: topic={}, id={}", message.getTopic(), message.getId());
            return message.getId();
        });
    }

    /**
     * 实际接收逻辑（IM 标准实现：通过函数式接口转发到 SSE 连接）.
     * <p>
     * 子类可重写以自定义接收行为。
     * </p>
     *
     * @param message 来自 Stream 的消息
     */
    protected void doReceive(TopicMessage message) {
        publishFunction.accept(message);
        log.debug("[ImRepeater] 转发到 SSE: topic={}, id={}", topic, message.getId());
    }

    /**
     * 实际订阅逻辑（IM 标准实现：通过函数式接口提供实时消息信封流）.
     * <p>
     * 子类可重写以自定义订阅行为。返回的信封流保留消息 ID，
     * 供断点续传水位线比较与 SSE {@code id:} 输出；
     * 历史回放请通过 {@link cn.gmlee.tools.im.resume.MessageHistoryStore} SPI 实现，
     * 无需在此拼接。
     * </p>
     *
     * @param urlParams 客户端请求参数
     * @param metadata  连接元数据
     * @return 消息信封流
     * @since 5.7.0 返回信封流（不再剥离为载荷）
     */
    protected Flux<TopicMessage> doSubscribe(MultiValueMap<String, String> urlParams, ConnectionMetadata metadata) {
        log.debug("[ImRepeater] 订阅消息流: topic={}", topic);
        return subscribeFunction.apply(topic, metadata);
    }
}
