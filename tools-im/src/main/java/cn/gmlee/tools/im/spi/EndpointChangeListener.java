package cn.gmlee.tools.im.spi;

import cn.gmlee.tools.im.conf.EndpointProperties;

/**
 * 端点变更监听器.
 * <p>
 * 监听 {@link cn.gmlee.tools.im.endpoint.EndpointRegistry} 的端点注册/注销事件。
 * 所有方法均为 {@code default} 实现（空操作），实现类只需重写感兴趣的方法。
 * </p>
 * <p>
 * <b>注意</b>：监听器异常不会中断回调链，框架会对每个监听器回调进行 try-catch 容错。
 * </p>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * @Component
 * public class EndpointAuditListener implements EndpointChangeListener {
 *     @Override
 *     public void onEndpointRegistered(EndpointProperties props) {
 *         auditLog.info("端点注册: {} → {}", props.getPath(), props.getTopic());
 *     }
 * }
 * }</pre>
 *
 * @since 5.6.0
 */
public interface EndpointChangeListener {

    /**
     * 端点注册后触发.
     *
     * @param props 新注册的端点配置
     */
    default void onEndpointRegistered(EndpointProperties props) {
    }

    /**
     * 端点注销后触发.
     *
     * @param props 被注销的端点配置
     */
    default void onEndpointUnregistered(EndpointProperties props) {
    }
}
