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
     * 由框架根据配置的路由键（{@code im.routing-keys}，默认 {@code me}）从请求中提取。
     * 多维路由键按配置顺序以 {@code |} 拼接（如 {@code A|B}）。
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
