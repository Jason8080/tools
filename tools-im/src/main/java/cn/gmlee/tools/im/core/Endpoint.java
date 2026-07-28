package cn.gmlee.tools.im.core;

import cn.gmlee.tools.base.mod.R;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.util.MultiValueMap;
import reactor.core.publisher.Flux;

import java.io.Serializable;

/**
 * 端点接口.
 * <p>
 * 提供 push/pull 默认桩方法。
 * 对于 SSE 场景，提供 {@link #sse(String, MultiValueMap)} 方法返回
 * {@code Flux<ServerSentEvent<Object>>}，支持心跳注释等 SSE 特性。
 * </p>
 *
 * @param <T> 消息类型
 */
public interface Endpoint<T> {

    /**
     * 推送.
     *
     * @param queue     队列名称
     * @param urlParams 路径参数
     * @param t         推送内容
     * @return r 返回结果
     */
    default R<Serializable> push(String queue, MultiValueMap<String, String> urlParams, T t) {
        return R.of(-1);
    }

    /**
     * 拉取.
     *
     * @param queue     队列名称
     * @param urlParams 路径参数
     * @return flux 拉取内容
     */
    default Flux<R<T>> pull(String queue, MultiValueMap<String, String> urlParams) {
        return Flux.empty();
    }

    /**
     * SSE 拉取.
     * <p>
     * 返回 ServerSentEvent 流，支持数据事件和心跳注释。
     * 默认实现委托给 {@link #pull(String, MultiValueMap)}，
     * SSE 端点可重写此方法以支持心跳等特性。
     * </p>
     *
     * @param queue     队列名称
     * @param urlParams 路径参数
     * @return SSE 事件流
     */
    default Flux<ServerSentEvent<T>> sse(String queue, MultiValueMap<String, String> urlParams) {
        return pull(queue, urlParams)
                .map(data -> ServerSentEvent.<T>builder()
                        .data(data.getData())
                        .build());
    }
}
