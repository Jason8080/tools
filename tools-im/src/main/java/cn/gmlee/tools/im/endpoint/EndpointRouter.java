package cn.gmlee.tools.im.endpoint;

import cn.gmlee.tools.base.mod.R;
import cn.gmlee.tools.im.conf.SseProperties;
import cn.gmlee.tools.im.conf.EndpointProperties;
import cn.gmlee.tools.im.model.EndpointMode;
import cn.gmlee.tools.im.model.MessageMap;
import cn.gmlee.tools.im.model.Msg;
import cn.gmlee.tools.im.core.Publisher;
import cn.gmlee.tools.im.core.Subscriber;
import cn.gmlee.tools.im.sse.heartbeat.SseHeartbeatHelper;
import cn.gmlee.tools.im.spi.access.AccessContext;
import cn.gmlee.tools.im.ex.AccessDeniedException;
import cn.gmlee.tools.im.spi.access.AccessFilter;
import cn.gmlee.tools.im.spi.access.AccessFilterChain;
import cn.gmlee.tools.im.topic.TopicRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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
 * <h3>访问控制</h3>
 * <p>
 * 支持通过 {@link AccessFilter} 实现端点级别的安全控制（认证、授权、限流等）。
 * 过滤器在请求路由到处理器之前执行，按 Order 排序。
 * </p>
 *
 * @since 5.6.0
 */
@Slf4j
public class EndpointRouter {

    private final EndpointRegistry registry;
    private final TopicRegistry topicRegistry;
    private final SseProperties sseProperties;
    private final List<AccessFilter> filters;

    /**
     * 创建动态路由器.
     *
     * @param registry      端点注册表
     * @param topicRegistry Topic 组件注册表
     * @param sseProperties SSE 配置
     * @param filters       访问过滤器列表（可为 null）
     */
    public EndpointRouter(EndpointRegistry registry,
                          TopicRegistry topicRegistry,
                          SseProperties sseProperties,
                          List<AccessFilter> filters) {
        this.registry = registry;
        this.topicRegistry = topicRegistry;
        this.sseProperties = sseProperties;

        // 按 Order 排序过滤器，存储为不可变列表
        if (filters != null && !filters.isEmpty()) {
            this.filters = filters.stream()
                    .sorted(Comparator.comparingInt(AccessFilter::getOrder)).toList();
            log.info("[EndpointRouter] 加载 {} 个访问过滤器", this.filters.size());
        } else {
            this.filters = Collections.emptyList();
        }
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

        // 创建访问上下文
        AccessContext context = new AccessContext(request, props);

        try {
            // 执行过滤器链
            if (!filters.isEmpty()) {
                AccessFilterChain chain = AccessFilterChain.create(filters);
                chain.doFilter(context);
            }

            // 过滤器链执行成功，路由到处理器
            if (props.getMode() == EndpointMode.PUSH) {
                return handlePush(request, props, context);
            } else {
                return handlePull(request, props, context);
            }
        } catch (AccessDeniedException e) {
            log.warn("[EndpointRouter] 访问被拒绝: path={}, reason={}, status={}",
                    request.path(), e.getMessage(), e.getStatus());
            return ServerResponse.status(e.getStatus()).build();
        } catch (Exception e) {
            log.error("[EndpointRouter] 过滤器执行异常: path={}", request.path(), e);
            return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * PUSH 处理：Publisher 发布消息 → 返回消息 ID.
     */
    private Mono<ServerResponse> handlePush(ServerRequest request, EndpointProperties props, AccessContext context) {
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
    private Mono<ServerResponse> handlePull(ServerRequest request, EndpointProperties props, AccessContext context) {
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
