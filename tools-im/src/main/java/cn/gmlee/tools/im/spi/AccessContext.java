package cn.gmlee.tools.im.spi;

import cn.gmlee.tools.im.conf.EndpointProperties;
import lombok.Getter;
import org.springframework.web.reactive.function.server.ServerRequest;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 端点访问上下文.
 * <p>
 * 封装请求信息和中间状态，在过滤器链中传递。
 * 支持过滤器之间共享数据（如认证后的用户信息）。
 * </p>
 *
 * <h3>线程安全</h3>
 * <p>
 * 属性映射使用 {@link ConcurrentHashMap}，支持并发访问。
 * 但 {@code principal} 字段使用 volatile 保证可见性，不提供原子更新。
 * 建议在过滤器链中按顺序修改 principal，避免并发修改。
 * </p>
 *
 * @since 5.6.0
 */
public class AccessContext {

    /**
     * HTTP 请求
     */
    @Getter
    private final ServerRequest request;

    /**
     * 端点配置
     */
    @Getter
    private final EndpointProperties endpoint;

    /**
     * 过滤器间共享的属性.
     * <p>
     * 使用 ConcurrentHashMap 保证线程安全。
     * </p>
     */
    private final Map<String, Object> attributes = new ConcurrentHashMap<>();

    /**
     * 认证主体（用户信息）.
     * <p>
     * volatile 保证跨线程可见性。
     * </p>
     * -- GETTER --
     *  获取认证主体.
     *

     */
    @Getter
    private volatile Object principal;

    /**
     * 创建访问上下文.
     *
     * @param request  HTTP 请求
     * @param endpoint 端点配置
     */
    public AccessContext(ServerRequest request, EndpointProperties endpoint) {
        this.request = request;
        this.endpoint = endpoint;
    }

    // ==================== 属性操作 ====================

    /**
     * 设置属性.
     *
     * @param key   属性键
     * @param value 属性值
     */
    public void setAttribute(String key, Object value) {
        if (key == null) {
            throw new IllegalArgumentException("Attribute key cannot be null");
        }
        if (value == null) {
            attributes.remove(key);
        } else {
            attributes.put(key, value);
        }
    }

    /**
     * 获取属性.
     *
     * @param key 属性键
     * @param <T> 属性类型
     * @return 属性值，不存在返回 null
     */
    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String key) {
        return (T) attributes.get(key);
    }

    /**
     * 查找属性（Optional 包装）.
     *
     * @param key 属性键
     * @return Optional 包装的属性值
     */
    public Optional<Object> findAttribute(String key) {
        return Optional.ofNullable(attributes.get(key));
    }

    /**
     * 获取所有属性（不可变视图）.
     *
     * @return 属性映射的不可变视图
     */
    public Map<String, Object> getAttributes() {
        return java.util.Collections.unmodifiableMap(attributes);
    }

    // ==================== 认证状态 ====================

    /**
     * 检查是否已认证.
     *
     * @return 如果 principal 不为 null 返回 true
     */
    public boolean isAuthenticated() {
        return principal != null;
    }

    /**
     * 设置认证主体.
     * <p>
     * 设置非 null 值表示已认证，设置 null 表示未认证。
     * </p>
     *
     * @param principal 认证主体
     */
    public void setPrincipal(Object principal) {
        this.principal = principal;
    }

    /**
     * 获取认证主体（类型安全）.
     *
     * @param type 期望的类型
     * @param <T>  主体类型
     * @return Optional 包装的主体
     */
    @SuppressWarnings("unchecked")
    public <T> Optional<T> getPrincipal(Class<T> type) {
        return type.isInstance(principal)
                ? Optional.of((T) principal)
                : Optional.empty();
    }

    // ==================== 便捷方法 ====================

    /**
     * 获取客户端 IP.
     * <p>
     * 优先从 {@code X-Forwarded-For} 头获取（支持反向代理），
     * 否则使用 {@link ServerRequest#remoteAddress()}。
     * </p>
     *
     * @return 客户端 IP，无法获取返回 "unknown"
     */
    public String getClientIp() {
        // 优先从 X-Forwarded-For 获取（支持反向代理）
        String forwarded = request.headers().firstHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isEmpty()) {
            return forwarded.split(",")[0].trim();
        }

        // 否则使用 remoteAddress
        return request.remoteAddress()
                .map(InetSocketAddress::getHostString)
                .orElse("unknown");
    }

    /**
     * 获取请求头.
     *
     * @param name 请求头名称
     * @return Optional 包装的请求头值
     */
    public Optional<String> getHeader(String name) {
        return Optional.ofNullable(request.headers().firstHeader(name));
    }

    /**
     * 获取查询参数.
     *
     * @param name 参数名
     * @return Optional 包装的参数值
     */
    public Optional<String> getQueryParam(String name) {
        return Optional.ofNullable(request.queryParams().getFirst(name));
    }

    /**
     * 获取请求路径.
     *
     * @return 请求路径
     */
    public String getPath() {
        return request.path();
    }

    /**
     * 获取请求方法.
     *
     * @return 请求方法（GET/POST 等）
     */
    public String getMethod() {
        return request.method().name();
    }
}
