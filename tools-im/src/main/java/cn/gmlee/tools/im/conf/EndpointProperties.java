package cn.gmlee.tools.im.conf;

import cn.gmlee.tools.im.model.EndpointMode;
import lombok.Data;

import java.util.List;

/**
 * 端点配置模型.
 * <p>
 * 一个端点由以下要素定义：
 * <ul>
 *   <li>{@code path} — 对外暴露的 URL 路径（如 {@code /api/chat/pull}）</li>
 *   <li>{@code topic} — 内部 Topic 名称（如 {@code im.chat}），不暴露在 URL 中</li>
 *   <li>{@code mode} — 端点模式：{@link EndpointMode#PUSH 推送} 或 {@link EndpointMode#PULL 拉取}</li>
 *   <li>{@code routingKeys} — 可选，按端点覆盖全局路由键配置</li>
 * </ul>
 * </p>
 * <p>
 * 路径与 Topic 完全解耦，开发者可自由定义 URL 结构，内部 Topic 名称对访问者不可见。
 * </p>
 *
 * @since 5.6.0
 */
@Data
public class EndpointProperties {

    /**
     * 请求路径（如 /api/chat/pull）
     */
    private String path;

    /**
     * 内部 Topic 名称（如 im.chat）
     */
    private String topic;

    /**
     * 端点模式
     */
    private EndpointMode mode;

    /**
     * 路由键配置（按端点覆盖）.
     * <p>
     * 覆盖全局 {@link SseProperties#getRoutingKeys()} 配置，仅对此端点生效。
     * {@code null}（默认）表示继承全局配置。
     * </p>
     * <p>
     * 语义与全局配置一致：
     * </p>
     * <ul>
     *   <li>{@code null} — 继承全局配置</li>
     *   <li>{@code []} / {@code ["*"]} — 全部 URL 参数参与</li>
     *   <li>{@code ["to"]} — 仅提取 to 参数（发布方示例）</li>
     *   <li>{@code ["tenant", "room"]} — 提取指定字段</li>
     * </ul>
     *
     * <h3>示例</h3>
     * <pre>
     * im:
     *   sse:
     *     routing-keys: ["room"]            # 全局默认（按房间路由）
     *   endpoints:
     *     - path: /api/chat/pull
     *       topic: im.chat
     *       mode: pull
     *                                       # routingKeys 未设置 → 继承全局 ["room"]
     *     - path: /api/chat/push
     *       topic: im.chat
     *       mode: push
     *                                       # routingKeys 未设置 → 继承全局 ["room"]
     *     - path: /api/room/pull
     *       topic: im.room
     *       mode: pull
     *       routing-keys: ["tenant", "room"]  # 端点覆盖：多租户聊天室
     *     - path: /api/room/push
     *       topic: im.room
     *       mode: push
     *       routing-keys: ["tenant", "room"]  # 必须与对应的 pull 端点配置一致
     * </pre>
     *
     * @since 5.6.0
     */
    private List<String> routingKeys;
}
