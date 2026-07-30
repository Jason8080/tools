package cn.gmlee.tools.im.spi.access;

import cn.gmlee.tools.im.ex.AccessDeniedException;

/**
 * 端点访问过滤器.
 * <p>
 * 单一职责：每个过滤器专注于一个安全关注点（认证、授权、限流等）。
 * 多个过滤器通过 {@link AccessFilterChain} 组合，按顺序执行。
 * </p>
 *
 * <h3>设计原则</h3>
 * <ul>
 *   <li><b>单一职责</b>：每个过滤器只关注一个安全方面</li>
 *   <li><b>链式调用</b>：通过 {@code chain.doFilter(context)} 继续执行下一个过滤器</li>
 *   <li><b>异常中断</b>：抛出 {@link AccessDeniedException} 中断过滤链</li>
 *   <li><b>上下文共享</b>：通过 {@link AccessContext} 在过滤器间传递数据</li>
 * </ul>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * @Component
 * public class JwtAuthFilter implements AccessFilter {
 *     @Autowired
 *     private JwtService jwtService;
 *
 *     @Override
 *     public void doFilter(AccessContext context, AccessFilterChain chain) {
 *         String token = context.getHeader("Authorization")
 *                 .filter(h -> h.startsWith("Bearer "))
 *                 .map(h -> h.substring(7))
 *                 .orElseThrow(() -> new AccessDeniedException("Missing token", HttpStatus.UNAUTHORIZED));
 *
 *         UserDetails user = jwtService.validate(token);
 *         context.setPrincipal(user);
 *         chain.doFilter(context); // 继续下一个过滤器
 *     }
 * }
 * }</pre>
 *
 * <h3>过滤器顺序</h3>
 * <p>
 * 通过 {@link #getOrder()} 或 Spring 的 {@code @Order} 注解控制执行顺序。
 * 值越小优先级越高。典型顺序：
 * </p>
 * <ol>
 *   <li>认证过滤器（Order=10）</li>
 *   <li>授权过滤器（Order=20）</li>
 *   <li>限流过滤器（Order=30）</li>
 *   <li>审计日志过滤器（Order=MAX）</li>
 * </ol>
 *
 * @since 5.6.0
 * @see AccessContext
 * @see AccessFilterChain
 */
public interface AccessFilter {

    /**
     * 执行过滤逻辑.
     * <p>
     * 过滤器可以：
     * </p>
     * <ul>
     *   <li>检查请求，决定是否允许访问</li>
     *   <li>修改上下文（如设置认证信息）</li>
     *   <li>调用 {@code chain.doFilter(context)} 继续下一个过滤器</li>
     *   <li>抛出 {@link AccessDeniedException} 中断过滤链</li>
     * </ul>
     *
     * @param context 访问上下文
     * @param chain   过滤器链
     * @throws AccessDeniedException 拒绝访问
     */
    void doFilter(AccessContext context, AccessFilterChain chain);

    /**
     * 获取过滤器顺序.
     * <p>
     * 值越小优先级越高。默认使用 Spring Bean 的注册顺序。
     * </p>
     * <p>
     * 推荐顺序：
     * </p>
     * <ul>
     *   <li>认证过滤器：10-19</li>
     *   <li>授权过滤器：20-29</li>
     *   <li>限流过滤器：30-39</li>
     *   <li>业务过滤器：40-99</li>
     *   <li>审计日志：Integer.MAX_VALUE</li>
     * </ul>
     *
     * @return 顺序值
     */
    default int getOrder() {
        return 0;
    }
}
