package cn.gmlee.tools.im.sse.heartbeat;

import cn.gmlee.tools.im.conf.SseProperties;
import cn.gmlee.tools.im.sse.SseConnection;
import cn.gmlee.tools.im.sse.SseConnectionManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;

import java.time.Duration;

/**
 * SSE 心跳辅助工具类.
 * <p>
 * 封装心跳相关的所有逻辑，为数据流添加心跳支持。
 * </p>
 *
 * <h3>职责</h3>
 * <ul>
 *   <li>创建心跳事件流（SSE 注释形式）</li>
 *   <li>通过 Reactor Context 获取连接引用</li>
 *   <li>心跳发送时调用 {@link SseConnection#touch()} 更新活跃时间</li>
 *   <li>将心跳流与数据流合并（autoConnect + merge）</li>
 * </ul>
 *
 * <h3>设计目的</h3>
 * <p>
 * 将心跳逻辑从端点层抽离，使端点代码简洁，心跳职责单一聚合。
 * 端点只需调用 {@link #wrapWithHeartbeat(Flux, SseProperties.HeartbeatConfig)} 即可添加心跳支持。
 * </p>
 *
 * <h3>前置条件</h3>
 * <p>
 * 数据流必须通过 Reactor Context 携带连接引用，键为 {@link SseConnectionManager#CONTEXT_KEY_CONNECTION}。
 * 这由 {@link SseConnectionManager#subscribe(String)} 自动完成。
 * </p>
 *
 * @author tools-im
 */
@Slf4j
public final class SseHeartbeatHelper {

    private SseHeartbeatHelper() {
        // 工具类禁止实例化
    }

    /**
     * 为数据流添加心跳支持.
     * <p>
     * 如果心跳未启用（{@code config.enabled = false}），直接返回原始数据流。
     * 如果心跳启用，创建心跳流并与数据流合并。心跳发送时会调用 {@link SseConnection#touch()}
     * 更新连接活跃时间，防止连接被 Reaper 误判为僵尸连接。
     * </p>
     *
     * <h4>Context 传播修复</h4>
     * <p>
     * 使用 {@code Flux.create} 手动管理订阅，确保心跳和数据在同一个订阅链中，
     * 共享相同的 Context。连接引用通过 {@code deferContextual} 在最外层读取。
     * </p>
     *
     * @param dataFlux 数据流（需通过 Context 携带连接引用）
     * @param config   心跳配置
     * @param <T>      消息类型
     * @return 合并心跳后的流
     */
    @SuppressWarnings("unchecked")
    public static <T> Flux<ServerSentEvent<T>> wrapWithHeartbeat(
            Flux<ServerSentEvent<T>> dataFlux,
            SseProperties.HeartbeatConfig config) {

        if (!config.isEnabled()) {
            return dataFlux;
        }

        // 使用 deferContextual 在最外层读取连接
        // 这确保在订阅时能正确获取 Context 中的连接引用
        return Flux.deferContextual(ctx -> {
            SseConnection conn = ctx.getOrDefault(SseConnectionManager.CONTEXT_KEY_CONNECTION, null);
            if (conn == null) {
                log.warn("[Heartbeat] Context 中缺少连接引用，心跳将不会重置空闲计时器");
            }

            // 创建心跳流，使用捕获的连接引用
            Flux<ServerSentEvent<T>> heartbeat = createHeartbeatFlux(conn, config);

            // 使用 share() 让数据和心跳共享同一个订阅链
            // share() 确保 Context 能正确传播
            Flux<ServerSentEvent<T>> shared = dataFlux.share();

            // 心跳在数据流完成时停止
            heartbeat = heartbeat.takeUntilOther(shared.ignoreElements());

            // 合并数据流和心跳流
            return Flux.merge(shared, heartbeat);
        });
    }

    /**
     * 创建心跳事件流.
     * <p>
     * 定期发送 SSE 注释（comment），保持连接活性。
     * SSE 注释以 {@code :} 开头，浏览器的 EventSource API 会忽略注释，
     * 不会触发 onmessage 回调，但能防止反向代理因空闲超时而断开连接。
     * </p>
     * <p>
     * 心跳发送时同时调用 {@link SseConnection#touch()} 更新连接活跃时间，
     * 防止连接被 Reaper 误判为僵尸连接而清理。
     * </p>
     *
     * @param conn   连接引用，可为 null（如果 Context 中未找到）
     * @param config 心跳配置
     * @param <T>    消息类型
     * @return 心跳事件流
     */
    private static <T> Flux<ServerSentEvent<T>> createHeartbeatFlux(
            SseConnection conn,
            SseProperties.HeartbeatConfig config) {

        Duration interval = config.getInterval();
        String comment = config.getComment();

        // 心跳只是 SSE 注释，不包含数据，类型参数不影响实际内容
        ServerSentEvent<T> heartbeatEvent = ServerSentEvent.<T>builder()
                .comment(comment)
                .build();

        return Flux.interval(interval)
                .map(tick -> {
                    // 心跳发送时更新连接活跃时间，防止被 Reaper 回收
                    if (conn != null) {
                        conn.touch();
                    }
                    return heartbeatEvent;
                });
    }
}
