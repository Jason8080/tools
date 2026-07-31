package cn.gmlee.tools.im.endpoint;

import cn.gmlee.tools.base.mod.R;
import cn.gmlee.tools.im.conf.SseProperties;
import cn.gmlee.tools.im.conf.EndpointProperties;
import cn.gmlee.tools.im.model.ConnectionMetadata;
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
import cn.gmlee.tools.im.spi.converter.PrincipalRoutingKeyConverter;
import cn.gmlee.tools.im.topic.TopicRegistry;
import cn.gmlee.tools.im.spi.routing.DefaultRoutingKeyComposer;
import cn.gmlee.tools.im.spi.routing.RoutingKeyComposer;
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

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

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
 * 例如 {@code /api/chat/pull} 可映射到 Topic {@code im.chat}。
 * </p>
 *
 * <h3>访问控制</h3>
 * <p>
 * 支持通过 {@link cn.gmlee.tools.im.spi.access.AccessFilter} 实现端点级别的安全控制（认证、授权、限流等）。
 * 过滤器在请求路由到处理器之前执行，按 Order 排序。
 * </p>
 *
 * <h3>routingKey 提取</h3>
 * <p>
 * PULL 端点的连接 routingKey 按以下优先级提取：
 * </p>
 * <ol>
 *   <li>String 类型的 principal（由 AccessFilter 设置）</li>
 *   <li>{@link PrincipalRoutingKeyConverter} 从非 String principal 转换</li>
 *   <li>{@code X-Me} 请求头</li>
 *   <li>URL 参数按 {@code im.sse.routing-keys} 配置组合（支持端点级覆盖）</li>
 * </ol>
 *
 * @since 5.6.0
 */
@Slf4j
public class EndpointRouter {

    private final EndpointRegistry registry;
    private final TopicRegistry topicRegistry;
    private final SseProperties sseProperties;
    private final List<AccessFilter> filters;
    private final List<PrincipalRoutingKeyConverter> converters;
    private final RoutingKeyComposer composer;

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
        this(registry, topicRegistry, sseProperties, filters, null, null);
    }

    /**
     * 创建动态路由器（含 principal 路由键转换器）.
     *
     * @param registry      端点注册表
     * @param topicRegistry Topic 组件注册表
     * @param sseProperties SSE 配置
     * @param filters       访问过滤器列表（可为 null）
     * @param converters    principal 路由键转换器列表（可为 null）
     */
    public EndpointRouter(EndpointRegistry registry,
                          TopicRegistry topicRegistry,
                          SseProperties sseProperties,
                          List<AccessFilter> filters,
                          List<PrincipalRoutingKeyConverter> converters) {
        this(registry, topicRegistry, sseProperties, filters, converters, null);
    }

    /**
     * 创建动态路由器（含 principal 路由键转换器 + 路由键组合器）.
     *
     * @param registry      端点注册表
     * @param topicRegistry Topic 组件注册表
     * @param sseProperties SSE 配置
     * @param filters       访问过滤器列表（可为 null）
     * @param converters    principal 路由键转换器列表（可为 null）
     * @param composer      路由键组合器（可为 null 使用默认实现）
     */
    public EndpointRouter(EndpointRegistry registry,
                          TopicRegistry topicRegistry,
                          SseProperties sseProperties,
                          List<AccessFilter> filters,
                          List<PrincipalRoutingKeyConverter> converters,
                          RoutingKeyComposer composer) {
        this.registry = registry;
        this.topicRegistry = topicRegistry;
        this.sseProperties = sseProperties;
        this.composer = composer != null ? composer : new DefaultRoutingKeyComposer();

        // 按 Order 排序过滤器，存储为不可变列表
        if (filters != null && !filters.isEmpty()) {
            this.filters = filters.stream()
                    .sorted(Comparator.comparingInt(AccessFilter::getOrder)).toList();
            log.info("[EndpointRouter] 加载 {} 个访问过滤器", this.filters.size());
        } else {
            this.filters = Collections.emptyList();
        }

        // 按 Order 排序转换器，存储为不可变列表
        if (converters != null && !converters.isEmpty()) {
            this.converters = converters.stream()
                    .sorted(Comparator.comparingInt(PrincipalRoutingKeyConverter::getOrder)).toList();
            log.info("[EndpointRouter] 加载 {} 个 principal 路由键转换器", this.converters.size());
        } else {
            this.converters = Collections.emptyList();
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
     * 请求分发：根据端点模式路由到 PUSH 或 PULL 处理器（响应式）.
     */
    private Mono<ServerResponse> dispatch(ServerRequest request) {
        EndpointProperties props = registry.resolve(request.path());
        // 创建访问上下文
        AccessContext context = new AccessContext(request, props);

        // 执行过滤器链（响应式）
        Mono<Void> filterChain = filters.isEmpty()
                ? Mono.empty()
                : AccessFilterChain.create(filters).doFilter(context);

        return filterChain
                .then(Mono.defer(() -> {
                    // 过滤器链执行成功，路由到处理器
                    if (props.getMode() == EndpointMode.PUSH) {
                        return handlePush(request, props, context);
                    } else {
                        return handlePull(request, props, context);
                    }
                }))
                .onErrorResume(AccessDeniedException.class, e -> {
                    log.warn("[EndpointRouter] 访问被拒绝: path={}, reason={}, status={}",
                            request.path(), e.getMessage(), e.getStatus());
                    return ServerResponse.status(e.getStatus()).build();
                })
                .onErrorResume(Exception.class, e -> {
                    log.error("[EndpointRouter] 过滤器执行异常: path={}", request.path(), e);
                    return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
                });
    }

    /**
     * PUSH 处理：Publisher 发布消息 → 返回消息 ID.
     * <p>
     * 使用 raw {@code Publisher} 调用 {@code push()}，绕过 {@code Publisher<?, ?>} 双 wildcard
     * 无法与具体 {@code MessageMap} 类型统一的问题。运行时类型安全由 TopicRegistry 保证。
     * </p>
     * <p>
     * 按端点级 routingKeys 配置提取定向投递目标，传递给 {@code Publisher.push(urlParams, msg, targets)}。
     * </p>
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private Mono<ServerResponse> handlePush(ServerRequest request, EndpointProperties props, AccessContext context) {
        MultiValueMap<String, String> urlParams = request.queryParams();
        return request.bodyToMono(MessageMap.class)
                .defaultIfEmpty(new MessageMap())
                .flatMap(msg -> {
                    Publisher publisher = topicRegistry.ensurePublisher(props.getTopic());
                    // 按端点级 routingKeys 提取定向投递目标
                    Set<String> targets = extractRoutingTargets(props, urlParams);
                    Mono<java.io.Serializable> idMono = publisher.push(urlParams, msg, targets);
                    return idMono.flatMap(id -> ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(R.of(id)));
                });
    }

    /**
     * PULL 处理：Subscriber 订阅 → 带心跳的事件流.
     */
    private Mono<ServerResponse> handlePull(ServerRequest request, EndpointProperties props, AccessContext context) {
        ConnectionMetadata metadata = buildMetadata(props, context);
        return dispatchPull(
                topicRegistry.ensureSubscriber(props.getTopic()),
                request.queryParams(), metadata);
    }

    /**
     * PULL 分发（wildcard capture 辅助方法）.
     * <p>
     * 通过泛型方法参数捕获 {@code Subscriber<?>} 的 wildcard，
     * 保证 {@code pull()} 返回类型安全的 {@code Flux<MSG>}。
     * </p>
     */
    private <MSG extends Msg> Mono<ServerResponse> dispatchPull(
            Subscriber<MSG> subscriber, MultiValueMap<String, String> urlParams, ConnectionMetadata metadata) {
        Flux<MSG> msgFlux = subscriber.pull(urlParams, metadata);
        Flux<ServerSentEvent<MessageMap>> sseFlux = msgFlux
                .map(payload -> {
                    MessageMap messageMap = payload instanceof MessageMap
                            ? (MessageMap) payload
                            : new MessageMap(Collections.singletonMap("data", payload));
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

    /**
     * 从 AccessContext 构建连接元数据.
     * <p>
     * 路由标识（routingKey）提取，优先级从高到低：
     * <ol>
     *   <li>{@code principal} 为 {@link String} 类型 → 直接使用</li>
     *   <li>按 Order 遍历 {@link PrincipalRoutingKeyConverter}，首个 {@link PrincipalRoutingKeyConverter#supports} 返回 {@code true} 的转换器执行转换</li>
     *   <li>{@code X-Me} 请求头（服务端调用、fetch-based SSE 客户端）</li>
     *   <li>从 URL 参数按端点级/全局 {@code routing-keys} 配置提取并组合</li>
     * </ol>
     * routingKey 格式为规范化查询字符串（key 按字母排序，{@code key=value&key=value}）。
     * </p>
     *
     * @param props   端点配置（含端点级 routingKeys 覆盖）
     * @param context 访问上下文
     * @return 连接元数据
     */
    private ConnectionMetadata buildMetadata(EndpointProperties props, AccessContext context) {
        String routingKey = null;

        // 1. AccessFilter 设置的 principal
        Object principal = context.getPrincipal();
        if (principal instanceof String s) {
            routingKey = s;
        } else if (principal != null) {
            // 2. 通过转换器从非 String principal 提取
            routingKey = convertPrincipal(principal, context);
        }

        // 3. X-Me 请求头
        if (routingKey == null) {
            routingKey = context.getHeader("X-Me").orElse(null);
        }

        // 4. 按端点级/全局配置从 URL 参数组合
        if (routingKey == null) {
            List<String> resolvedKeys = resolveRoutingKeys(props);
            routingKey = composer.composeRoutingKey(resolvedKeys, context.getRequest().queryParams());
        }

        return ConnectionMetadata.builder()
                .topic(props.getTopic())
                .routingKey(routingKey)
                .build();
    }

    /**
     * 通过转换器将非 String principal 转换为路由键.
     * <p>
     * 按 Order 排序依次尝试，首个 {@link PrincipalRoutingKeyConverter#supports} 返回 {@code true}
     * 且转换结果非空的转换器获胜。转换异常被捕获并记录日志，不影响后续提取策略。
     * </p>
     *
     * @param principal 认证主体（非 null，非 String）
     * @param context   访问上下文
     * @return 路由键字符串，无转换器匹配时返回 null
     */
    private String convertPrincipal(Object principal, AccessContext context) {
        if (converters.isEmpty()) {
            log.debug("[EndpointRouter] principal 类型为 {}，非 String 且无转换器，跳过 routingKey 提取。" +
                    "请注册 PrincipalRoutingKeyConverter Bean 或在 AccessFilter 中设置 String 类型的 principal",
                    principal.getClass().getName());
            return null;
        }
        for (PrincipalRoutingKeyConverter converter : converters) {
            if (converter.supports(principal.getClass())) {
                try {
                    String key = converter.convert(principal, context);
                    if (key != null && !key.isEmpty()) {
                        log.debug("[EndpointRouter] 通过 {} 将 {} 转换为 routingKey: {}",
                                converter.getClass().getSimpleName(), principal.getClass().getSimpleName(), key);
                        return key;
                    }
                } catch (Exception e) {
                    log.warn("[EndpointRouter] {} 转换 principal 异常: {}",
                            converter.getClass().getSimpleName(), e.getMessage(), e);
                }
            }
        }
        log.debug("[EndpointRouter] 无转换器支持 {} 类型的 principal，跳过 routingKey 提取",
                principal.getClass().getName());
        return null;
    }

    /**
     * 解析端点的有效 routingKeys 配置.
     * <p>
     * 端点级配置优先，未设置则回退到全局配置。
     * 结果经过 {@link RoutingKeyComposer#resolve(List)} 归一化。
     * </p>
     *
     * @param props 端点配置
     * @return 归一化后的 routingKeys（null 表示全部参数）
     */
    private List<String> resolveRoutingKeys(EndpointProperties props) {
        List<String> keys = props.getRoutingKeys();
        if (keys == null) {
            keys = sseProperties.getRoutingKeys();
        }
        return RoutingKeyComposer.resolve(keys);
    }

    /**
     * 从 URL 参数提取端点级定向投递目标.
     *
     * @param props     端点配置
     * @param urlParams URL 参数
     * @return 路由目标集合（不可变），空集表示广播
     */
    private Set<String> extractRoutingTargets(EndpointProperties props, MultiValueMap<String, String> urlParams) {
        List<String> resolvedKeys = resolveRoutingKeys(props);
        return composer.extractRoutingTargets(resolvedKeys, urlParams);
    }
}
