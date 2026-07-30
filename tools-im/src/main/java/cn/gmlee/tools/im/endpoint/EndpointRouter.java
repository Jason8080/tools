package cn.gmlee.tools.im.endpoint;

import cn.gmlee.tools.base.mod.R;
import cn.gmlee.tools.im.conf.SseProperties;
import cn.gmlee.tools.im.conf.EndpointProperties;
import cn.gmlee.tools.im.core.EndpointMode;
import cn.gmlee.tools.im.core.MessageMap;
import cn.gmlee.tools.im.core.Msg;
import cn.gmlee.tools.im.core.Publisher;
import cn.gmlee.tools.im.core.Subscriber;
import cn.gmlee.tools.im.sse.heartbeat.SseHeartbeatHelper;
import cn.gmlee.tools.im.topic.TopicRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.Serializable;

/**
 * 动态端点路由.
 * <p>
 * 根据 {@link EndpointRegistry} 中注册的端点配置动态分发请求。
 * 路由通过 {@link TopicRegistry} 获取每个 Topic 的 {@link Publisher} 和 {@link Subscriber}，
 * 实现进-转-出的三段式架构。
 * </p>
 *
 * <h3>路由规则</h3>
 * <ul>
 *   <li>仅匹配注册表中已有映射的路径，未注册路径放行给其他 {@code @RestController}</li>
 *   <li>{@link EndpointMode#PUSH PUSH} 端点：POST 请求 → {@link Publisher#push} 发布消息</li>
 *   <li>{@link EndpointMode#PULL PULL} 端点：GET 请求 → {@link Subscriber#pull} 订阅 SSE 流</li>
 * </ul>
 *
 * <h3>路径与 Topic 解耦</h3>
 * <p>
 * URL 路径由开发者自定义，内部 Topic 名称不暴露在请求中。
 * 例如 {@code /api/chat/stream} 可映射到 Topic {@code im.chat}。
 * </p>
 *
 * @since 5.6.0
 */
@Slf4j
public class EndpointRouter {

    private final EndpointRegistry registry;
    private final TopicRegistry topicRegistry;
    private final SseProperties sseProperties;

    /**
     * 创建动态路由器.
     *
     * @param registry      端点注册表
     * @param topicRegistry Topic 组件注册表
     * @param sseProperties SSE 配置
     */
    public EndpointRouter(EndpointRegistry registry,
                          TopicRegistry topicRegistry,
                          SseProperties sseProperties) {
        this.registry = registry;
        this.topicRegistry = topicRegistry;
        this.sseProperties = sseProperties;
    }

    /**
     * 构建 RouterFunction.
     * <p>
     * 仅匹配 {@link EndpointRegistry} 中已注册的路径，未注册路径返回 {@code Mono.empty()}
     * 以便放行给其他处理器。
     * </p>
     *
     * @return 路由函数
     */
    public RouterFunction<ServerResponse> build() {
        return RouterFunctions.route()
                .route(this::matchEndpoint, this::dispatch)
                .build();
    }

    /**
     * 路由匹配：检查请求路径是否在注册表中.
     */
    private boolean matchEndpoint(ServerRequest request) {
        return registry.resolve(request.path()) != null;
    }

    /**
     * 请求分发：根据端点模式路由到 PUSH 或 PULL 处理器.
     */
    private Mono<ServerResponse> dispatch(ServerRequest request) {
        EndpointProperties props = registry.resolve(request.path());
        if (props == null) {
            return ServerResponse.notFound().build();
        }
        if (props.getMode() == EndpointMode.PUSH) {
            return handlePush(request, props);
        } else {
            return handlePull(request, props);
        }
    }

    /**
     * PUSH 处理：Publisher 发布消息 → 返回消息 ID.
     */
    private Mono<ServerResponse> handlePush(ServerRequest request, EndpointProperties props) {
        MultiValueMap<String, String> urlParams = request.queryParams();
        return request.bodyToMono(MessageMap.class)
                .defaultIfEmpty(new MessageMap())
                .flatMap(msg -> {
                    Publisher publisher = topicRegistry.ensurePublisher(props.getTopic());
                    Serializable id = publisher.push(urlParams, msg);
                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(R.of(id));
                });
    }

    /**
     * PULL 处理：Subscriber 订阅 → 带心跳的事件流.
     */
    private Mono<ServerResponse> handlePull(ServerRequest request, EndpointProperties props) {
        Subscriber subscriber = topicRegistry.ensureSubscriber(props.getTopic());
        Flux<Msg> msgFlux = subscriber.pull(request.queryParams());
        Flux<ServerSentEvent<MessageMap>> sseFlux = msgFlux
                .map(payload -> {
                    MessageMap messageMap = payload instanceof MessageMap
                            ? (MessageMap) payload
                            : new MessageMap(java.util.Collections.singletonMap("data", payload));
                    return ServerSentEvent.<MessageMap>builder()
                            .data(messageMap)
                            .build();
                });
        Flux<ServerSentEvent<MessageMap>> withHeartbeat = SseHeartbeatHelper.wrapWithHeartbeat(
                sseFlux, sseProperties.getHeartbeat());
        return ServerResponse.ok()
                .contentType(MediaType.TEXT_EVENT_STREAM)
                .body(withHeartbeat, ServerSentEvent.class);
    }
}
