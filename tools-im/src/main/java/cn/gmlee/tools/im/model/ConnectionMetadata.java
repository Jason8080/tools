package cn.gmlee.tools.im.model;

import lombok.Builder;
import lombok.Value;

import java.util.Map;

/**
 * 连接元数据.
 * <p>
 * 订阅时从 HTTP 请求提取（JWT principal / 自定义请求头 / URL 参数等），绑定到连接生命周期内。
 * 不可变，线程安全。既是连接的运行时身份，也是 {@link cn.gmlee.tools.im.spi.listener.SseConnectionListener}
 * 的事件载荷。
 * </p>
 *
 * @since 5.6.0
 */
@Value
@Builder
public class ConnectionMetadata {

    /**
     * 所属 Topic
     */
    String topic;

    /**
     * 路由标识.
     * <p>
     * 由框架根据配置的路由键（{@code im.sse.routing-keys}，默认 null = 全部 URL 参数）从请求中提取。
     * 格式为规范化查询字符串：key 按字母排序，{@code key=value} 以 {@code &} 拼接
     * （如 {@code room=lobby&tenant=acme}）。无 URL 参数时为 null（纯广播连接）。
     * 框架不绑定任何业务概念——可以是 userId、deviceId、roomId 等任意标识。
     * </p>
     */
    String routingKey;

    /**
     * 会话标识
     */
    String sessionId;

    /**
     * 连接唯一 ID
     */
    String connectionId;

    /**
     * 断点续传位点（Last-Event-ID）.
     * <p>
     * 客户端断线重连时由浏览器 {@code EventSource} 自动携带（仅通过
     * {@code Last-Event-ID} 请求头，<b>不接受 URL 参数</b>——避免污染全参数模式
     * 下的 routingKey 提取）。值为客户端最后收到的消息 ID 的字符串形式，
     * 由 {@link cn.gmlee.tools.im.resume.EventIdCodec} 解码后用于历史回放。
     * 首次订阅为 null。
     * </p>
     *
     * @since 5.7.0
     */
    String lastEventId;

    /**
     * 自定义扩展属性
     */
    @Builder.Default
    Map<String, String> attributes = Map.of();

    /**
     * 获取自定义属性.
     *
     * @param key 属性键
     * @return 属性值，不存在返回 null
     */
    public String get(String key) {
        return attributes.get(key);
    }

    /**
     * 便捷构造（无身份，纯广播场景）.
     *
     * @param topic        Topic 名称
     * @param connectionId 连接 ID
     * @return 连接元数据
     */
    public static ConnectionMetadata of(String topic, String connectionId) {
        return ConnectionMetadata.builder()
                .topic(topic)
                .connectionId(connectionId)
                .build();
    }
}
