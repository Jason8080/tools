package cn.gmlee.tools.im.model;

import cn.gmlee.tools.im.spi.listener.SseConnectionListener;
import lombok.Value;

/**
 * SSE 连接信息（轻量 DTO）.
 * <p>
 * 供 {@link SseConnectionListener} 回调使用，仅暴露监听器所需的连接标识信息，
 * 不泄露内部实现细节（状态机、位域标志、Sink 等）。
 * </p>
 *
 * @since 5.6.0
 */
@Value
public class SseConnectionInfo {

    /**
     * 所属 Topic
     */
    String topic;

    /**
     * 连接唯一 ID
     */
    String connectionId;
}
