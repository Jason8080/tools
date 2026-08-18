package cn.gmlee.tools.im.conf;

/**
 * 部署模式枚举.
 * <p>
 * 定义框架的两种部署模式，决定消息传递路径和组件加载策略。
 * </p>
 *
 * <h3>模式对比</h3>
 * <table border="1">
 *   <tr>
 *     <th>特性</th>
 *     <th>CLUSTER（集群模式）</th>
 *     <th>STANDALONE（单机模式）</th>
 *   </tr>
 *   <tr>
 *     <td>MQ 依赖</td>
 *     <td>✅ 需要（RabbitMQ）</td>
 *     <td>❌ 不需要</td>
 *   </tr>
 *   <tr>
 *     <td>部署架构</td>
 *     <td>支持多实例集群</td>
 *     <td>仅单机部署</td>
 *   </tr>
 *   <tr>
 *     <td>消息传递</td>
 *     <td>通过 MQ 广播</td>
 *     <td>直接调用（绕过 MQ）</td>
 *   </tr>
 *   <tr>
 *     <td>延迟</td>
 *     <td>~1-5ms（MQ 网络开销）</td>
 *     <td>&lt;0.1ms（直接调用）</td>
 *   </tr>
 *   <tr>
 *     <td>SPI 行为</td>
 *     <td colspan="2" style="text-align:center">✅ 完全一致（除 MQ 传递路径外）</td>
 *   </tr>
 * </table>
 *
 * <h3>配置示例</h3>
 * <pre>
 * # CLUSTER 模式（默认）
 * im:
 *   mode: cluster
 *
 * # STANDALONE 模式（单机部署）
 * im:
 *   mode: standalone
 * </pre>
 *
 * @since 5.6.0
 * @see ImProperties#getMode()
 */
public enum DeploymentMode {

    /**
     * 集群模式（默认）.
     * <p>
     * 使用 Spring Cloud Stream + MQ（RabbitMQ）进行消息传递。
     * 支持多实例集群部署，消息通过 MQ 广播到所有实例。
     * </p>
     * <p>
     * 适用场景：
     * </p>
     * <ul>
     *   <li>多实例部署，需要实例间消息同步</li>
     *   <li>高可用场景，需要消息持久化</li>
     *   <li>大规模并发，需要 MQ 的缓冲能力</li>
     * </ul>
     */
    CLUSTER,

    /**
     * 单机模式.
     * <p>
     * 不依赖 MQ，消息直接通过内存传递。
     * 仅支持单机部署，但延迟极低（&lt;0.1ms）。
     * </p>
     * <p>
     * 适用场景：
     * </p>
     * <ul>
     *   <li>单机部署，无需集群支持</li>
     *   <li>对延迟极度敏感的场景</li>
     *   <li>开发/测试环境，简化依赖</li>
     *   <li>轻量级应用，避免 MQ  overhead</li>
     * </ul>
     * <p>
     * <b>注意</b>：此模式下消息不会持久化，应用重启后消息丢失。
     * </p>
     */
    STANDALONE
}
