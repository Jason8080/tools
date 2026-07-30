package cn.gmlee.tools.im.util;

/**
 * Spring Cloud Stream binding 与 Bean 命名规则.
 * <p>
 * 集中管理所有 binding 名称和 Bean 名称的生成逻辑，避免命名规则散落在各处导致不一致。
 * 所有名称均基于 topic 衍生，修改命名规则只需改此类。
 * </p>
 *
 * <h3>命名规则</h3>
 * <table>
 *   <tr><th>用途</th><th>格式</th><th>示例（topic=order.update）</th></tr>
 *   <tr><td>输出 binding（Publisher 发送）</td><td>{@code {topic}-out-0}</td><td>{@code order.update-out-0}</td></tr>
 *   <tr><td>输入 binding（Consumer 接收）</td><td>{@code {topic}.consumer-in-0}</td><td>{@code order.update.consumer-in-0}</td></tr>
 *   <tr><td>Publisher Bean</td><td>{@code {topic}.publisher}</td><td>{@code order.update.publisher}</td></tr>
 *   <tr><td>Consumer Bean</td><td>{@code {topic}.consumer}</td><td>{@code order.update.consumer}</td></tr>
 *   <tr><td>Subscriber Bean</td><td>{@code {topic}.subscriber}</td><td>{@code order.update.subscriber}</td></tr>
 * </table>
 *
 * <p>
 * 输入 binding 名称由 Consumer Bean 名称派生（{@code {beanName}-in-0}），
 * 符合 Spring Cloud Stream 函数式绑定规范。
 * </p>
 *
 * @since 5.6.0
 */
public final class BindingNames {

    private BindingNames() {
    }

    /**
     * 输出 binding 名称（Publisher 发送端）.
     * <p>
     * 用于 {@code StreamBridge.send()} 和注册输出 binding 配置。
     * </p>
     *
     * @param topic Topic 名称
     * @return {@code {topic}-out-0}
     */
    public static String outputBinding(String topic) {
        return topic + "-out-0";
    }

    /**
     * 输入 binding 名称（Consumer 接收端）.
     * <p>
     * 由 {@link #consumerBean(String)} 派生，符合 Spring Cloud Stream 函数式绑定规范
     * （{@code {beanName}-in-0}）。用于注册输入 binding 配置。
     * </p>
     *
     * @param topic Topic 名称
     * @return {@code {topic}.consumer-in-0}
     */
    public static String inputBinding(String topic) {
        return consumerBean(topic) + "-in-0";
    }

    /**
     * Publisher Bean 名称.
     *
     * @param topic Topic 名称
     * @return {@code {topic}.publisher}
     */
    public static String publisherBean(String topic) {
        return topic + ".publisher";
    }

    /**
     * Consumer Bean 名称.
     * <p>
     * 同时作为输入 binding 名称的前缀（{@code {consumerBean}-in-0}）。
     * </p>
     *
     * @param topic Topic 名称
     * @return {@code {topic}.consumer}
     */
    public static String consumerBean(String topic) {
        return topic + ".consumer";
    }

    /**
     * Subscriber Bean 名称.
     *
     * @param topic Topic 名称
     * @return {@code {topic}.subscriber}
     */
    public static String subscriberBean(String topic) {
        return topic + ".subscriber";
    }
}
