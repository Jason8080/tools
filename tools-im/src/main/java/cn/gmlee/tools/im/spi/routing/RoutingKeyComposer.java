package cn.gmlee.tools.im.spi.routing;

import cn.gmlee.tools.im.model.Msg;
import cn.gmlee.tools.im.model.TopicMessage;
import org.springframework.util.MultiValueMap;
import reactor.core.publisher.Mono;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

/**
 * 路由键组合器（SPI 扩展点）.
 * <p>
 * 负责将 URL 参数组合为路由键字符串，决定订阅方的身份标识和发布方的投递目标。
 * 框架提供 {@link DefaultRoutingKeyComposer} 默认实现（规范化查询字符串格式，key 按字母排序），
 * 开发者可实现此接口注册为 Spring Bean 以自定义组合策略。
 * </p>
 *
 * <h3>扩展方式</h3>
 * <pre>{@code
 * @Component
 * public class CustomRoutingKeyComposer implements RoutingKeyComposer {
 *     @Override
 *     public String composeRoutingKey(List<String> routingKeys, MultiValueMap<String, String> params) {
 *         // 自定义逻辑：如 HMAC 签名、自定义拼接格式等
 *     }
 *
 *     @Override
 *     public Set<String> extractRoutingTargets(List<String> routingKeys, MultiValueMap<String, String> params) {
 *         // 自定义逻辑：需与 composeRoutingKey 保持一致的寻址语义
 *     }
 * }
 * }</pre>
 *
 * <h3>routingKeys 配置归一化</h3>
 * <p>
 * 通过 {@link #resolve(List)} 静态方法统一处理配置值的语义：
 * </p>
 * <ul>
 *   <li>{@code null} / 空列表 / {@code ["*"]} → 返回 {@code null}，表示「全部 URL 参数参与」</li>
 *   <li>其他 → 原样返回，表示「指定字段参与」</li>
 * </ul>
 *
 * <h3>默认实现的 routingKey 格式</h3>
 * <p>
 * 规范化查询字符串：key 按字母排序，{@code key=value} 以 {@code &} 拼接，值取 {@code getFirst()}。
 * 例如：{@code ?tenant=acme&room=lobby} → {@code "room=lobby&tenant=acme"}。
 * 该格式保证参数顺序无关性（{@code ?a=1&b=2} 与 {@code ?b=2&a=1} 产生相同的 routingKey）。
 * </p>
 *
 * @since 5.6.0
 */
public interface RoutingKeyComposer {

    /**
     * 从 URL 参数组合单个路由键（订阅方使用）.
     * <p>
     * 将 URL 参数按 {@code routingKeys} 配置提取并组合为单个字符串，
     * 作为 SSE 连接的身份标识（{@link cn.gmlee.tools.im.model.ConnectionMetadata#getRoutingKey}）。
     * </p>
     * <p>
     * {@code routingKeys} 参数已经过 {@link #resolve(List)} 归一化：
     * </p>
     * <ul>
     *   <li>{@code null} → 使用全部 URL 参数</li>
     *   <li>非 null → 使用指定字段</li>
     * </ul>
     *
     * @param routingKeys 归一化后的路由键配置（null 表示全部参数）
     * @param params      URL 查询参数
     * @return 路由键字符串，无匹配参数时返回 null（纯广播连接）
     */
    String composeRoutingKey(List<String> routingKeys, MultiValueMap<String, String> params);

    /**
     * 从 URL 参数提取定向投递目标集合（发布方使用）.
     * <p>
     * 将 URL 参数按 {@code routingKeys} 配置提取为路由键集合，
     * 作为 {@link TopicMessage#getRoutingKeys()} 的值，决定消息投递给哪些连接。
     * </p>
     * <p>
     * 与 {@link #composeRoutingKey} 保持寻址语义一致：
     * 发布方的某个 target 等于订阅方的 routingKey 时，消息投递到该连接。
     * </p>
     *
     * @param routingKeys 归一化后的路由键配置（null 表示全部参数）
     * @param params      URL 查询参数
     * @return 路由目标集合（不可变），空集表示广播
     */
    Set<String> extractRoutingTargets(List<String> routingKeys, MultiValueMap<String, String> params);

    /**
     * 归一化 routingKeys 配置.
     * <p>
     * 将配置值统一为两种语义：
     * </p>
     * <ul>
     *   <li>{@code null} → 「全部参数」模式：实现类从 URL 参数中收集所有键</li>
     *   <li>非 null 列表 → 「指定字段」模式：仅使用列表中的字段</li>
     * </ul>
     * <p>
     * 以下输入均归一化为 {@code null}（全部参数）：
     * </p>
     * <ul>
     *   <li>{@code null}</li>
     *   <li>空列表</li>
     *   <li>{@code ["*"]}（显式关键字）</li>
     * </ul>
     *
     * @param routingKeys 原始路由键配置
     * @return 归一化后的配置（null 表示全部参数）
     */
    static List<String> resolve(List<String> routingKeys) {
        if (routingKeys == null || routingKeys.isEmpty()) {
            return null;
        }
        if (routingKeys.size() == 1 && "*".equals(routingKeys.getFirst())) {
            return null;
        }
        return routingKeys;
    }
}
