package cn.gmlee.tools.im.topic;

import cn.gmlee.tools.im.conf.EndpointProperties;
import cn.gmlee.tools.im.spi.listener.EndpointChangeListener;

/**
 * Topic 资源工厂接口.
 * <p>
 * 管理 Topic 的物理资源创建和销毁。不同的部署模式有不同的实现：
 * </p>
 * <ul>
 *   <li><b>CLUSTER 模式</b>：{@link ClusterTopicResourceFactory} - 创建 Spring Cloud Stream binding</li>
 *   <li><b>STANDALONE 模式</b>：{@link StandaloneTopicResourceFactory} - 仅创建内存资源，无 MQ binding</li>
 * </ul>
 *
 * <h3>设计原则</h3>
 * <ul>
 *   <li><b>策略模式</b>：不同部署模式使用不同的资源管理策略</li>
 *   <li><b>零开销</b>：未使用的模式不加载任何资源</li>
 *   <li><b>幂等性</b>：同一 Topic 的资源只创建一次</li>
 *   <li><b>监听器模式</b>：实现 {@link EndpointChangeListener}，响应端点注册/注销事件</li>
 * </ul>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * // 框架内部使用，通过 Spring 自动注入
 * @Autowired
 * private TopicResourceFactory topicResourceFactory;
 *
 * // 端点注册时自动创建资源
 * topicResourceFactory.onEndpointRegistered(endpointProps);
 *
 * // Topic 销毁时清理资源
 * topicResourceFactory.cleanupResources(topic);
 * }</pre>
 *
 * @since 5.6.0
 * @see ClusterTopicResourceFactory
 * @see StandaloneTopicResourceFactory
 */
public interface TopicResourceFactory extends EndpointChangeListener {

    /**
     * 确保端点的物理资源已创建.
     * <p>
     * 根据端点模式（PUSH/PULL）创建相应的资源：
     * </p>
     * <ul>
     *   <li><b>CLUSTER 模式</b>：创建 Spring Cloud Stream binding + Consumer Bean</li>
     *   <li><b>STANDALONE 模式</b>：仅确保 Repeater 组件创建</li>
     * </ul>
     * <p>
     * 幂等：同一端点多次调用只创建一次资源。
     * </p>
     *
     * @param props 端点配置
     */
    void ensureResources(EndpointProperties props);

    /**
     * 清理 Topic 的物理资源.
     * <p>
     * 销毁 Topic 对应的所有物理资源：
     * </p>
     * <ul>
     *   <li><b>CLUSTER 模式</b>：移除 Stream binding 配置和 Consumer Bean</li>
     *   <li><b>STANDALONE 模式</b>：清理内部记录</li>
     * </ul>
     * <p>
     * <b>注意</b>：此方法不检查引用计数，调用方需确保 Topic 不再被使用。
     * </p>
     *
     * @param topic Topic 名称
     */
    void cleanupResources(String topic);

    /**
     * 检查 Topic 是否存在物理资源.
     *
     * @param topic Topic 名称
     * @return 存在任意资源返回 true
     */
    boolean hasResources(String topic);
}
