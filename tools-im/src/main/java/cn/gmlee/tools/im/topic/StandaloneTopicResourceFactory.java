package cn.gmlee.tools.im.topic;

import cn.gmlee.tools.im.conf.EndpointProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 单机模式 Topic 资源工厂.
 * <p>
 * STANDALONE 部署模式下的 {@link TopicResourceFactory} 实现。
 * 不创建 Spring Cloud Stream binding，仅确保 Repeater 组件创建。
 * </p>
 *
 * <h3>与 CLUSTER 模式的差异</h3>
 * <table border="1">
 *   <tr>
 *     <th>操作</th>
 *     <th>CLUSTER 模式</th>
 *     <th>STANDALONE 模式</th>
 *   </tr>
 *   <tr>
 *     <td>创建 Stream binding</td>
 *     <td>✅ 创建输入/输出 binding</td>
 *     <td>❌ 不创建</td>
 *   </tr>
 *   <tr>
 *     <td>注册 Consumer Bean</td>
 *     <td>✅ 注册 ConsumerBridge</td>
 *     <td>❌ 不注册</td>
 *   </tr>
 *   <tr>
 *     <td>确保 Repeater 创建</td>
 *     <td>✅ 创建</td>
 *     <td>✅ 创建</td>
 *   </tr>
 *   <tr>
 *     <td>MQ 依赖</td>
 *     <td>✅ 需要 RabbitMQ</td>
 *     <td>❌ 不需要</td>
 *   </tr>
 * </table>
 *
 * <h3>SPI 行为一致性</h3>
 * <p>
 * 除不创建 Stream binding 外，其他行为与 {@link ClusterTopicResourceFactory} 完全一致：
 * </p>
 * <ul>
 *   <li>✅ 监听端点注册/注销事件</li>
 *   <li>✅ 确保 Repeater 组件创建</li>
 *   <li>✅ 幂等性保证</li>
 *   <li>✅ 资源清理逻辑</li>
 * </ul>
 *
 * <h3>使用场景</h3>
 * <ul>
 *   <li>单机部署，无需集群支持</li>
 *   <li>开发/测试环境，简化依赖</li>
 *   <li>对延迟极度敏感的场景（&lt;0.1ms）</li>
 * </ul>
 *
 * @since 5.6.0
 * @see TopicResourceFactory
 * @see ClusterTopicResourceFactory
 * @see cn.gmlee.tools.im.conf.DeploymentMode#STANDALONE
 */
@Slf4j
@RequiredArgsConstructor
public class StandaloneTopicResourceFactory implements TopicResourceFactory {

    private final TopicRegistry topicRegistry;

    /**
     * 已创建资源的 Topic 集合（用于幂等性保证）
     */
    private final Set<String> managedTopics = ConcurrentHashMap.newKeySet();

    // ==================== TopicResourceFactory 接口实现 ====================

    /**
     * 确保端点的资源已创建.
     * <p>
     * 单机模式下，只需确保 Repeater 组件创建，不需要 Stream binding。
     * </p>
     * <p>
     * 幂等：同一端点多次调用只创建一次资源。
     * </p>
     *
     * @param props 端点配置
     */
    @Override
    public void ensureResources(EndpointProperties props) {
        String topic = props.getTopic();
        if (managedTopics.add(topic)) {
            // 单机模式：只需确保 Repeater 创建
            topicRegistry.ensureRepeater(topic);
            log.info("[StandaloneTopicResourceFactory] Topic 资源已创建: topic={}", topic);
        }
    }

    @Override
    public void onEndpointRegistered(EndpointProperties props) {
        ensureResources(props);
    }

    @Override
    public void onEndpointUnregistered(EndpointProperties props) {
        // 资源不随端点注销而销毁，避免影响其他共享同一 Topic 的端点。
        // Sink 由 SseConnectionManager 的空 Topic TTL 机制自动清理。
        log.debug("[StandaloneTopicResourceFactory] 端点注销，保留 Topic 资源: topic={}",
                  props.getTopic());
    }

    /**
     * 清理 Topic 的资源.
     * <p>
     * 单机模式下，仅清理内部记录，无需清理 Stream binding。
     * </p>
     *
     * @param topic Topic 名称
     */
    @Override
    public void cleanupResources(String topic) {
        if (managedTopics.remove(topic)) {
            log.info("[StandaloneTopicResourceFactory] Topic 资源已清理: topic={}", topic);
        }
    }

    /**
     * 检查 Topic 是否存在资源.
     *
     * @param topic Topic 名称
     * @return 存在资源返回 true
     */
    @Override
    public boolean hasResources(String topic) {
        return managedTopics.contains(topic);
    }
}
