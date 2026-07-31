package cn.gmlee.tools.im.topic;

import java.util.Set;

/**
 * Topic 生命周期管理器.
 * <p>
 * 统一管理 Topic 的完整生命周期：
 * <ul>
 *   <li><b>状态管理</b>：跟踪每个 Topic 的状态（CREATED → ACTIVE → DESTROYING → DESTROYED）</li>
 *   <li><b>引用计数</b>：跟踪每个 Topic 的使用者数量（端点、连接等）</li>
 *   <li><b>资源协调</b>：协调 {@link TopicRegistry} 和 {@link TopicFactory} 的创建/销毁</li>
 *   <li><b>自动清理</b>：支持 TTL、LRU 等策略自动清理不活跃的 Topic</li>
 * </ul>
 * </p>
 *
 * <h3>设计目标</h3>
 * <ul>
 *   <li>解决资源泄露：确保不再使用的 Topic 资源被正确释放</li>
 *   <li>并发安全：所有操作都是线程安全的</li>
 *   <li>向后兼容：不破坏现有 API，渐进式改进</li>
 *   <li>可扩展：支持自定义清理策略和监听器</li>
 * </ul>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * // 1. 确保 Topic 存在（增加引用计数）
 * lifecycleManager.acquire("im.chat");
 *
 * // 2. 使用 Topic（获取 Publisher/Repeater/Subscriber）
 * Publisher publisher = topicRegistry.getPublisher("im.chat");
 *
 * // 3. 释放 Topic（减少引用计数）
 * lifecycleManager.release("im.chat");
 *
 * // 4. 强制销毁 Topic（忽略引用计数）
 * lifecycleManager.destroy("im.chat");
 * }</pre>
 *
 * @since 5.6.0
 */
public interface TopicLifecycleManager {

    /**
     * 获取 Topic（增加引用计数）.
     * <p>
     * 如果 Topic 不存在，会创建新的 Topic 并初始化状态为 CREATED。
     * 如果 Topic 已存在，增加引用计数。
     * </p>
     *
     * @param topic Topic 名称
     */
    void acquire(String topic);

    /**
     * 释放 Topic（减少引用计数）.
     * <p>
     * 减少引用计数。如果引用计数变为 0，不会立即销毁 Topic，
     * 而是进入 TTL 倒计时（如果配置了 TTL）。TTL 到期后自动销毁。
     * </p>
     *
     * @param topic Topic 名称
     */
    void release(String topic);

    /**
     * 强制销毁 Topic（忽略引用计数）.
     * <p>
     * 立即开始销毁流程，清理所有资源。
     * 适用于：
     * <ul>
     *   <li>端点注销时，确认 Topic 不再被任何端点使用</li>
     *   <li>管理 API 手动清理 Topic</li>
     *   <li>测试场景</li>
     * </ul>
     * </p>
     *
     * @param topic Topic 名称
     * @return 如果执行了销毁返回 true，Topic 不存在或已销毁返回 false
     */
    boolean destroy(String topic);

    /**
     * 获取 Topic 当前状态.
     *
     * @param topic Topic 名称
     * @return Topic 状态，不存在返回 null
     */
    TopicState getState(String topic);

    /**
     * 获取 Topic 引用计数.
     *
     * @param topic Topic 名称
     * @return 引用计数，不存在返回 0
     */
    int getRefCount(String topic);

    /**
     * 获取所有活跃 Topic 名称.
     *
     * @return Topic 名称集合（不可变）
     */
    Set<String> getAllTopics();

    /**
     * 获取指定状态的 Topic 名称.
     *
     * @param state 目标状态
     * @return Topic 名称集合（不可变）
     */
    Set<String> getTopicsByState(TopicState state);

    /**
     * 添加生命周期监听器.
     *
     * @param listener 监听器
     */
    void addListener(TopicLifecycleListener listener);

    /**
     * 移除生命周期监听器.
     *
     * @param listener 监听器
     */
    void removeListener(TopicLifecycleListener listener);

    /**
     * 执行一次自动清理（检查 TTL、LRU 等策略）.
     * <p>
     * 通常由定时任务调用（如 {@link cn.gmlee.tools.im.sse.cleanup.ConnectionReaper}）。
     * </p>
     *
     * @return 被清理的 Topic 数量
     */
    int cleanup();
}
