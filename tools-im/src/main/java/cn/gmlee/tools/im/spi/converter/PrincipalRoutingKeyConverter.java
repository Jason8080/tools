package cn.gmlee.tools.im.spi.converter;

import cn.gmlee.tools.im.spi.access.AccessContext;
import cn.gmlee.tools.im.spi.access.AccessFilter;

/**
 * Principal 路由键转换器.
 * <p>
 * 当 {@link AccessFilter} 设置的 {@code principal} 不是 {@link String} 类型时，
 * 框架通过此转换器将其提取为路由键（routingKey），用于 SSE 连接的定向投递。
 * </p>
 *
 * <h3>使用场景</h3>
 * <p>
 * 典型场景：认证过滤器将 {@code UserDetails}、{@code JwtUser} 等复杂对象设置为
 * principal，但未将其 username 设为 routingKey。通过实现此转换器，可从复杂对象中
 * 提取标识字段作为 routingKey，无需修改过滤器代码。
 * </p>
 *
 * <h3>提取优先级</h3>
 * <p>
 * {@code EndpointRouter} 按以下顺序提取 routingKey：
 * </p>
 * <ol>
 *   <li>{@code principal} 为 {@link String} 类型 → 直接使用</li>
 *   <li>按 Order 遍历 {@code PrincipalRoutingKeyConverter}，首个 {@link #supports} 返回 {@code true} 的转换器执行转换</li>
 *   <li>{@code X-Me} 请求头</li>
 *   <li>从 URL 参数按 {@code im.routing-keys} 配置提取并组合</li>
 * </ol>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * @Component
 * public class UserDetailsRoutingKeyConverter implements PrincipalRoutingKeyConverter {
 *     @Override
 *     public boolean supports(Class<?> principalType) {
 *         return UserDetails.class.isAssignableFrom(principalType);
 *     }
 *
 *     @Override
 *     public String convert(Object principal, AccessContext context) {
 *         return ((UserDetails) principal).getUsername();
 *     }
 * }
 * }</pre>
 *
 * @since 5.6.0
 * @see AccessContext#setPrincipal(Object)
 * @see cn.gmlee.tools.im.endpoint.EndpointRouter
 */
public interface PrincipalRoutingKeyConverter {

    /**
     * 判断是否支持给定的 principal 类型.
     *
     * @param principalType principal 的实际类型（非 null）
     * @return {@code true} 表示可转换
     */
    boolean supports(Class<?> principalType);

    /**
     * 将 principal 转换为路由键字符串.
     * <p>
     * 仅在 {@link #supports(Class)} 返回 {@code true} 时调用。
     * 返回 {@code null} 或空字符串表示无法转换，框架将尝试下一个转换器或回退到后续提取策略。
     * </p>
     *
     * @param principal 认证主体（非 null，非 String）
     * @param context   访问上下文（可提供额外信息，如请求头、端点配置）
     * @return 路由键字符串，无法转换时返回 {@code null}
     */
    String convert(Object principal, AccessContext context);

    /**
     * 获取转换器顺序.
     * <p>
     * 值越小优先级越高。多个转换器都支持同一 principal 类型时，按 Order 排序依次尝试。
     * </p>
     *
     * @return 顺序值，默认 0
     */
    default int getOrder() {
        return 0;
    }
}
