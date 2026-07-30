package cn.gmlee.tools.im.core;

import cn.gmlee.tools.im.model.Msg;
import cn.gmlee.tools.im.model.TopicMessage;
import cn.gmlee.tools.im.spi.RepeaterInterceptor;
import org.springframework.util.MultiValueMap;
import reactor.core.publisher.Flux;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * Repeater 框架骨架.
 * <p>
 * 使用模板方法模式（Template Method Pattern），通过 {@code final} 方法保证
 * {@link RepeaterInterceptor} 拦截器链不可被绕过：
 * </p>
 * <ul>
 *   <li>{@link #send(TopicMessage)} — 织入 {@code beforeSend} → 调用 {@link #doSend(TopicMessage)}</li>
 *   <li>{@link #receive(TopicMessage)} — 调用 {@link #doReceive(TopicMessage)} → 织入 {@code afterReceive}</li>
 *   <li>{@link #subscribe(MultiValueMap)} — 调用 {@link #doSubscribe(MultiValueMap)} → 织入 {@code transformSubscribeStream}</li>
 * </ul>
 *
 * <h3>扩展方式</h3>
 * <p>
 * 继承此类并实现 {@code doSend} / {@code doReceive} / {@code doSubscribe}，
 * 拦截器自动生效。如需完全绕过拦截器，直接实现 {@link Repeater} 接口。
 * </p>
 *
 * @since 5.6.0
 */
public abstract class ImRepeater implements Repeater {

    private final List<RepeaterInterceptor> interceptors;

    /**
     * @param interceptors 拦截器列表（可为 null）
     */
    protected ImRepeater(List<RepeaterInterceptor> interceptors) {
        this.interceptors = interceptors != null ? interceptors : Collections.emptyList();
    }

    /**
     * 发送消息（final，保证拦截器织入）.
     * <p>
     * 调用链：{@code beforeSend} → {@link #doSend(TopicMessage)}
     * </p>
     */
    @Override
    public final Serializable send(TopicMessage<Msg> message) {
        for (RepeaterInterceptor i : interceptors) {
            i.beforeSend(message);
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
            stream = i.transformSubscribeStream(topic(), stream, urlParams);
        }
        return stream;
    }

    /**
     * 实际发送逻辑，由子类实现.
     *
     * @param message 消息信封
     * @return 消息 ID
     */
    protected abstract Serializable doSend(TopicMessage<Msg> message);

    /**
     * 实际接收逻辑，由子类实现.
     *
     * @param message 来自 Stream 的消息
     */
    protected abstract void doReceive(TopicMessage<Msg> message);

    /**
     * 实际订阅逻辑，由子类实现.
     *
     * @param urlParams 客户端请求参数
     * @return 消息流
     */
    protected abstract Flux<Msg> doSubscribe(MultiValueMap<String, String> urlParams);
}
