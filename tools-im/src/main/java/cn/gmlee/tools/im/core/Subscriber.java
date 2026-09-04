package cn.gmlee.tools.im.core;

import cn.gmlee.tools.im.model.ConnectionMetadata;
import cn.gmlee.tools.im.model.Msg;
import cn.gmlee.tools.im.model.TopicMessage;
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
 * @param <MSG> 消息载荷类型
 * @since 5.6.0
 */
public interface Subscriber<MSG extends Msg> extends Topic {

    /**
     * 订阅消息流.
     * <p>
     * 返回完整消息信封（含 ID），供下游输出 SSE {@code id:} 字段与断点续传；
     * 载荷通过 {@link TopicMessage#getMsg()} 获取。
     * </p>
     *
     * @param urlParams URL 查询参数（来自 HTTP 请求）
     * @param metadata  连接元数据（身份标识、续传位点等，来自 HTTP Headers）
     * @return 消息信封流
     * @since 5.7.0 由 {@code Flux<MSG>} 升级为信封流（断点续传契约）
     */
    Flux<TopicMessage<?, MSG>> pull(MultiValueMap<String, String> urlParams, ConnectionMetadata metadata);
}
