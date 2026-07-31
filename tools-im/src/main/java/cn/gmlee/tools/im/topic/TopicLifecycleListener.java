package cn.gmlee.tools.im.topic;

/**
 * Topic 生命周期监听器.
 * <p>
 * 监听 Topic 生命周期事件，可用于：
 * <ul>
 *   <li>资源监控和统计</li>
 *   <li>自定义清理逻辑（如缓存失效、日志记录等）</li>
 *   <li>外部系统集成（如通知配置中心、服务注册等）</li>
 * </ul>
 * </p>
 *
 * @since 5.6.0
 */
public interface TopicLifecycleListener {

    /**
     * Topic 创建后触发.
     *
     * @param topic Topic 名称
     */
    default void onTopicCreated(String topic) {
    }

    /**
     * Topic 激活后触发（首次有连接或资源被使用）.
     *
     * @param topic Topic 名称
     */
    default void onTopicActivated(String topic) {
    }

    /**
     * Topic 销毁前触发（开始清理资源）.
     *
     * @param topic Topic 名称
     */
    default void onTopicDestroying(String topic) {
    }

    /**
     * Topic 销毁后触发（所有资源已释放）.
     *
     * @param topic Topic 名称
     */
    default void onTopicDestroyed(String topic) {
    }
}
