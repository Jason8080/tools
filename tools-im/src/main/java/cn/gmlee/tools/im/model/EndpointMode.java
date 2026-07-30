package cn.gmlee.tools.im.model;

/**
 * 端点模式.
 *
 * <ul>
 *   <li>{@link #PUSH} — 接收 HTTP POST 请求，将消息发送到 MQ</li>
 *   <li>{@link #PULL} — 接收 HTTP GET 请求，返回 SSE 事件流</li>
 * </ul>
 *
 * @since 5.6.0
 */
public enum EndpointMode {

    /**
     * 推送端点：POST 请求 → MQ
     */
    PUSH,

    /**
     * 拉取端点：GET 请求 → SSE 事件流
     */
    PULL
}
