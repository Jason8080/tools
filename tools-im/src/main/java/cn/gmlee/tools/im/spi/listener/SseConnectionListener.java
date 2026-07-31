package cn.gmlee.tools.im.spi.listener;

import cn.gmlee.tools.im.model.ConnectionMetadata;
import reactor.core.publisher.SignalType;

/**
 * SSE 连接生命周期监听器.
 * <p>
 * 下游项目可实现此接口并注册为 Spring Bean，以监听 SSE 连接的建立、断开和发布异常事件。
 * 所有方法均为 {@code default} 实现（空操作），实现类只需重写感兴趣的方法。
 * </p>
 *
 * @since 5.6.0
 */
public interface SseConnectionListener {

    /**
     * 连接建立回调.
     *
     * @param metadata 连接元数据（含 routingKey 等身份信息）
     */
    default void onConnected(ConnectionMetadata metadata) {
    }

    /**
     * 连接断开回调.
     *
     * @param metadata 连接元数据
     * @param signal   触发断开的信号类型，来自 Reaper/Shutdown/forceClose 路径时为 null
     */
    default void onDisconnected(ConnectionMetadata metadata, SignalType signal) {
    }

    /**
     * 发布异常回调.
     *
     * @param topic  目标 Topic
     * @param result 发布失败的结果
     */
    default void onPublishError(String topic, reactor.core.publisher.Sinks.EmitResult result) {
    }
}
