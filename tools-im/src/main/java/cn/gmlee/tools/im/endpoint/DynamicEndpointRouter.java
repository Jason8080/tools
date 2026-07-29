package cn.gmlee.tools.im.endpoint;

import cn.gmlee.tools.base.mod.R;
import cn.gmlee.tools.im.conf.SseProperties;
import cn.gmlee.tools.im.core.BindingNames;
import cn.gmlee.tools.im.conf.EndpointProperties;
import cn.gmlee.tools.im.core.EndpointMode;
import cn.gmlee.tools.im.core.MessageMap;
import cn.gmlee.tools.im.core.Msg;
import cn.gmlee.tools.im.core.TopicMessage;
import cn.gmlee.tools.im.sse.SseConnectionManager;
import cn.gmlee.tools.im.sse.heartbeat.SseHeartbeatHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
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
 * 替代原静态端点，
 * 根据 {@link EndpointRegistry} 中注册的端点配置动态分发请求。
 * </p>
 *
 * <h3>路由规则</h3>
 * <ul>
 *   <li>仅匹配注册表中已有映射的路径，未注册路径放行给其他 {@code @RestController}</li>
 *   <li>{@link EndpointMode#PUSH PUSH} 端点：POST 请求 → 解析 JSON 为 {@link MessageMap} → StreamBridge 发送到 MQ</li>
 *   <li>{@link EndpointMode#PULL PULL} 端点：GET 请求 → SSE 订阅 → 带心跳的事件流</li>
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
public class DynamicEndpointRouter {

    private final EndpointRegistry registry;
    private final StreamBridge streamBridge;
    private final SseConnectionManager sseConnectionManager;
    private final SseProperties sseProperties;

    /**
     * 创建动态路由器.
     *
     * @param registry            端点注册表
     * @param streamBridge        Spring Cloud Stream 桥接器
     * @param sseConnectionManager SSE 连接管理器
     * @param sseProperties        SSE 配置
     */
    public DynamicEndpointRouter(EndpointRegistry registry,
                                  StreamBridge streamBridge,
                                  SseConnectionManager sseConnectionManager,
                                  SseProperties sseProperties) {
        this.registry = registry;
        this.streamBridge = streamBridge;
        this.sseConnectionManager = sseConnectionManager;
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
        EndpointProperties config = registry.resolve(request.path());
        if (config == null) {
            return ServerResponse.notFound().build();
        }
        if (config.getMode() == EndpointMode.PUSH) {
            return handlePush(request, config);
        } else {
            return handlePull(request, config);
        }
    }

    /**
     * PUSH 处理：解析 JSON → 发送 MQ → 返回消息 ID.
     */
    private Mono<ServerResponse> handlePush(ServerRequest request, EndpointProperties props) {
        MultiValueMap<String, String> urlParams = request.queryParams();
        return request.bodyToMono(MessageMap.class)
                .defaultIfEmpty(new MessageMap())
                .flatMap(msg -> {
                    TopicMessage<MessageMap> event = msg.build(urlParams);
                    event.setTopic(props.getTopic());
                    String bindingName = BindingNames.outputBinding(props.getTopic());
                    streamBridge.send(bindingName, event);
                    Serializable id = event.getId();
                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(R.of(id));
                });
    }

    /**
     * PULL 处理：SSE 订阅 → 带心跳的事件流.
     */
    private Mono<ServerResponse> handlePull(ServerRequest request, EndpointProperties props) {
        MultiValueMap<String, String> urlParams = request.queryParams();
        Flux<TopicMessage<Msg>> flux = sseConnectionManager.subscribe(props.getTopic());
        Flux<ServerSentEvent<MessageMap>> sseFlux = flux
                .map(m -> {
                    Object payload = m.getMsg();
                    MessageMap messageMap = payload instanceof MessageMap
                            ? (MessageMap) payload
                            : new MessageMap(java.util.Collections.singletonMap("data", payload));
                    return ServerSentEvent.<MessageMap>builder()
                            .id(String.valueOf(m.getId()))
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
