package cn.gmlee.tools.im.sse;

import reactor.core.publisher.SignalType;

/**
 * SSE 连接生命周期监听器.
 * <p>
 * 下游项目可实现此接口并注册为 Spring Bean，以监听 SSE 连接的建立、断开和发布异常事件。
 * 所有方法均为 {@code default} 实现（空操作），实现类只需重写感兴趣的方法。
 * </p>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * @Component
 * public class AuditConnectionListener implements SseConnectionListener {
 *     @Override
 *     public void onConnected(SseConnection conn) {
 *         auditLog.info("SSE 连接建立: topic={}, id={}", conn.getTopic(), conn.getConnectionId());
 *     }
 *
 *     @Override
 *     public void onDisconnected(SseConnection conn, SignalType signal) {
 *         auditLog.info("SSE 连接断开: topic={}, id={}, signal={}",
 *                 conn.getTopic(), conn.getConnectionId(), signal);
 *     }
 * }
 * }</pre>
 *
 * <h3>线程安全</h3>
 * <p>
 * 回调方法在各自的触发线程上同步调用（Reactor 事件线程、Reaper 扫描线程、Lifecycle 线程等），
 * 实现类应避免在回调中执行阻塞操作，如需异步处理请自行调度。
 * </p>
 */
public interface SseConnectionListener {

    /**
     * 连接建立回调.
     * <p>
     * 在 Flux 的 {@code doOnSubscribe} 回调中、连接状态转为 ACTIVE 后调用。
     * 此时连接已完成注册，计数器已递增。
     * </p>
     *
     * @param conn 新建的连接
     */
    default void onConnected(SseConnection conn) {
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
     * @param conn   已断开的连接
     * @param signal 触发断开的信号类型，来自 Reaper/Shutdown/forceClose 路径时为 null
     */
    default void onDisconnected(SseConnection conn, SignalType signal) {
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
