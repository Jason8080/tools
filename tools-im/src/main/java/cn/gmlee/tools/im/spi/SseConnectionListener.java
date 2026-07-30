package cn.gmlee.tools.im.spi;

import cn.gmlee.tools.im.model.SseConnectionInfo;
import reactor.core.publisher.SignalType;

/**
 * SSE 连接生命周期监听器.
 * <p>
 * 下游项目可实现此接口并注册为 Spring Bean，以监听 SSE 连接的建立、断开和发布异常事件。
 * 所有方法均为 {@code default} 实现（空操作），实现类只需重写感兴趣的方法。
 * </p>
 * <p>
 * <b>注意</b>：监听器异常不会中断回调链（与观察者模式语义一致），
 * 框架会对每个监听器回调进行 try-catch 容错。
 * </p>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * @Component
 * public class AuditConnectionListener implements SseConnectionListener {
 *     @Override
 *     public void onConnected(SseConnectionInfo info) {
 *         auditLog.info("SSE 连接建立: topic={}, id={}", info.getTopic(), info.getConnectionId());
 *     }
 *
 *     @Override
 *     public void onDisconnected(SseConnectionInfo info, SignalType signal) {
 *         auditLog.info("SSE 连接断开: topic={}, id={}, signal={}",
 *                 info.getTopic(), info.getConnectionId(), signal);
 *     }
 * }
 * }</pre>
 *
 * @since 5.6.0
 */
public interface SseConnectionListener {

    /**
     * 连接建立回调.
     * <p>
     * 在 Flux 的 {@code doOnSubscribe} 回调中、连接状态转为 ACTIVE 后调用。
     * 此时连接已完成注册，计数器已递增。
     * </p>
     *
     * @param info 连接信息
     */
    default void onConnected(SseConnectionInfo info) {
    }

    /**
     * 连接断开回调.
     * <p>
     * 在连接清理完成后调用（计数器已递减，连接已从注册表移除）。
     * 可能由以下场景触发：
     * <ul>
     *   <li>客户端主动断开（signal = {@link SignalType#ON_COMPLETE} 或 {@link SignalType#CANCEL}）</li>
     *   <li>Reaper 检测到僵尸连接强制关闭（signal = null）</li>
     *   <li>服务关闭排空（signal = null）</li>
     *   <li>管理员手动调用 {@code forceClose}（signal = null）</li>
     * </ul>
     * </p>
     *
     * @param info   连接信息
     * @param signal 触发断开的信号类型，来自 Reaper/Shutdown/forceClose 路径时为 null
     */
    default void onDisconnected(SseConnectionInfo info, SignalType signal) {
    }

    /**
     * 发布异常回调.
     * <p>
     * 当 {@code Sink.tryEmitNext()} 返回失败结果时调用。
     * 常见原因：背压缓冲区满（{@link reactor.core.publisher.Sinks.EmitResult#FAIL_OVERFLOW}）、
     * Sink 已完成（{@link reactor.core.publisher.Sinks.EmitResult#FAIL_TERMINATED}）等。
     * </p>
     *
     * @param topic  目标 Topic
     * @param result 发布失败的结果
     */
    default void onPublishError(String topic, reactor.core.publisher.Sinks.EmitResult result) {
    }
}
