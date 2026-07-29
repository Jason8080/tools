package cn.gmlee.tools.im.core;

import lombok.Data;

/**
 * 端点配置模型.
 * <p>
 * 一个端点由三要素定义：
 * <ul>
 *   <li>{@code path} — 对外暴露的 URL 路径（如 {@code /api/chat/stream}）</li>
 *   <li>{@code topic} — 内部 Topic 名称（如 {@code im.chat}），不暴露在 URL 中</li>
 *   <li>{@code mode} — 端点模式：{@link EndpointMode#PUSH 推送} 或 {@link EndpointMode#PULL 拉取}</li>
 * </ul>
 * </p>
 * <p>
 * 路径与 Topic 完全解耦，开发者可自由定义 URL 结构，内部 Topic 名称对访问者不可见。
 * </p>
 *
 * @since 5.6.0
 */
@Data
public class EndpointConfig {

    /**
     * 请求路径（如 /api/chat/stream）
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
}
