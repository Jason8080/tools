package cn.gmlee.tools.im.spi;

import java.util.List;

/**
 * 访问过滤器链.
 * <p>
 * 管理过滤器的执行顺序，支持动态组合。
 * 采用责任链模式（Chain of Responsibility Pattern），每个过滤器决定是否继续执行下一个。
 * </p>
 *
 * <h3>使用方式</h3>
 * <pre>{@code
 * // 在 AccessFilter 中调用
 * public void doFilter(AccessContext context, AccessFilterChain chain) {
 *     // 执行当前过滤器的逻辑
 *     if (!checkPermission(context)) {
 *         throw new AccessDeniedException("Permission denied");
 *     }
 *     // 继续下一个过滤器
 *     chain.doFilter(context);
 * }
 * }</pre>
 *
 * @since 5.6.0
 * @see AccessFilter
 */
public interface AccessFilterChain {

    /**
     * 执行下一个过滤器.
     * <p>
     * 如果还有过滤器，执行下一个；否则过滤链结束，请求继续路由到处理器。
     * </p>
     *
     * @param context 访问上下文
     */
    void doFilter(AccessContext context);

    /**
     * 创建过滤器链.
     * <p>
     * 工厂方法，创建默认的过滤器链实现。
     * </p>
     *
     * @param filters 过滤器列表（应已按 Order 排序）
     * @return 过滤器链
     */
    static AccessFilterChain create(List<AccessFilter> filters) {
        return new DefaultAccessFilterChain(filters, 0);
    }
}

/**
 * 默认过滤器链实现.
 * <p>
 * 采用递归方式执行过滤器链，每个过滤器持有链的引用，决定是否继续。
 * </p>
 *
 * @since 5.6.0
 */
class DefaultAccessFilterChain implements AccessFilterChain {

    /**
     * 过滤器列表
     */
    private final List<AccessFilter> filters;

    /**
     * 当前执行位置
     */
    private final int currentPosition;

    /**
     * 创建过滤器链.
     *
     * @param filters         过滤器列表
     * @param currentPosition 当前执行位置
     */
    DefaultAccessFilterChain(List<AccessFilter> filters, int currentPosition) {
        this.filters = filters;
        this.currentPosition = currentPosition;
    }

    @Override
    public void doFilter(AccessContext context) {
        // 所有过滤器执行完毕，过滤链结束
        if (currentPosition >= filters.size()) {
            return;
        }

        // 获取当前过滤器
        AccessFilter currentFilter = filters.get(currentPosition);

        // 创建下一个链（位置 +1）
        AccessFilterChain nextChain = new DefaultAccessFilterChain(filters, currentPosition + 1);

        // 执行当前过滤器
        currentFilter.doFilter(context, nextChain);
    }
}
