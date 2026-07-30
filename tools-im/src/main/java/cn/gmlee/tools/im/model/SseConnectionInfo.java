package cn.gmlee.tools.im.model;

/**
 * SSE 连接信息（轻量 DTO）.
 * <p>
 * 供 {@link cn.gmlee.tools.im.spi.SseConnectionListener} 回调使用，仅暴露监听器所需的连接标识信息，
 * 不泄露内部实现细节（状态机、位域标志、Sink 等）。
 * </p>
 *
 * @since 5.6.0
 */
public class SseConnectionInfo {

    private final String topic;
    private final String connectionId;

    public SseConnectionInfo(String topic, String connectionId) {
        this.topic = topic;
        this.connectionId = connectionId;
    }

    /**
     * 所属 Topic.
     */
    public String getTopic() {
        return topic;
    }

    /**
     * 连接唯一 ID.
     */
    public String getConnectionId() {
        return connectionId;
    }

    @Override
    public String toString() {
        return "SseConnectionInfo{topic='" + topic + "', connectionId='" + connectionId + "'}";
    }
}
