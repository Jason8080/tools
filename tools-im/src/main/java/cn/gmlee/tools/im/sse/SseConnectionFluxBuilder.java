package cn.gmlee.tools.im.sse;

import cn.gmlee.tools.im.conf.SseProperties;
import cn.gmlee.tools.im.model.ConnectionMetadata;
import cn.gmlee.tools.im.model.Msg;
import cn.gmlee.tools.im.model.TopicMessage;
import cn.gmlee.tools.im.ex.SseShutdownException;
import cn.gmlee.tools.im.sse.metrics.SseMetrics;
import cn.gmlee.tools.im.spi.listener.SseConnectionListener;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Subscription;
import reactor.core.publisher.Flux;
import reactor.core.publisher.SignalType;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.util.List;
import java.util.Set;

/**
 * SSE 连接 Flux 构建器.
 * <p>
 * 负责构建带有完整生命周期钩子的 Reactor Flux，将 Flux 构建细节
 * （Sink 创建、连接注册、回调处理）从 {@link SseConnectionManager} 中分离，
 * 使管理器专注于协调职责（预检查、计数器管理、生命周期控制）。
 * </p>
 *
 * <h3>职责</h3>
 * <ul>
 *   <li>获取或创建 Topic 对应的 Sink</li>
 *   <li>创建连接记录并注册到 {@link SseConnectionRegistry}</li>
 *   <li>构建带 {@code doOnSubscribe}/{@code doOnNext}/{@code doFinally} 的 Flux</li>
 *   <li>处理异常路径的计数器回滚</li>
 *   <li>触发 {@link SseConnectionListener} 回调</li>
 * </ul>
 *
 * <h3>线程模型</h3>
 * <p>
 * 此类是无状态工具类，所有方法均为静态方法。
 * Flux 的回调在 Reactor 的事件线程上执行。
 * </p>
 */
@Slf4j
final class SseConnectionFluxBuilder {

    private SseConnectionFluxBuilder() {
        // 工具类禁止实例化
    }

    /**
     * 构建连接订阅结果.
     * <p>
     * 完整流程：
     * <ol>
     *   <li>获取或创建 Sink（失败时回滚计数器）</li>
     *   <li>创建连接记录并注册</li>
     *   <li>构建带生命周期钩子的 Flux</li>
     *   <li>异常时回滚计数器并清理连接</li>
     * </ol>
     * 此方法在 {@link SseConnectionManager#subscribe} 的预检查通过后调用，
     * 调用方已保证计数器已递增（tryAcquire 成功）。
     * </p>
     *
     * @param topic      Topic 名称
     * @param metadata   连接元数据（身份标识等）
     * @param registry   连接注册表
     * @param metrics    指标收集器
     * @param properties SSE 配置
     * @param listeners  连接监听器列表
     * @return 订阅结果（包含消息流和连接引用）
     */
    static SseSubscription build(String topic,
                                  ConnectionMetadata metadata,
                                  SseConnectionRegistry registry,
                                  SseMetrics metrics,
                                  SseProperties properties,
                                  List<SseConnectionListener> listeners) {
        SseConnection conn = null;
        try {
            // 1. 获取或创建 Sink
            Sinks.Many<TopicMessage<Msg>> sink = registry.getOrCreateSink(topic);
            if (sink == null) {
                registry.getCounter().rollback(topic);
                metrics.recordSubscribe(topic, "REJECTED_SHUTDOWN");
                return new SseSubscription(Flux.error(SseShutdownException.INSTANCE), null);
            }

            // 2. 创建连接记录并注册
            conn = new SseConnection(topic);
            conn.setMetadata(metadata);
            registry.register(conn);
            metrics.recordSubscribe(topic, "SUCCESS");
            log.info("[Subscribe] 成功: topic={}, connectionId={}", topic, conn.getConnectionId());

            // 3. 构建带生命周期钩子的 Flux
            Flux<TopicMessage<Msg>> flux = attachLifecycle(sink, conn, topic, metadata, registry, metrics, properties, listeners);
            return new SseSubscription(flux, conn);

        } catch (Exception e) {
            // 异常回滚：递减计数器、注销连接、清理空 Topic
            registry.getCounter().rollback(topic);
            if (conn != null) {
                registry.unregister(conn);
                if (registry.cleanupIfEmpty(topic)) {
                    metrics.cleanupTopic(topic);
                }
            }
            metrics.recordError("subscribe_init");
            log.error("[Subscribe] 初始化失败: topic={}", topic, e);
            return new SseSubscription(Flux.error(e), null);
        }
    }

    /**
     * 附加生命周期钩子到 Sink Flux.
     * <p>
     * 四个钩子构成响应式连接管理：
     * <ul>
     *   <li>{@code doOnSubscribe}：保存订阅引用、激活连接、触发监听器</li>
     *   <li>{@code doOnNext}：更新活跃时间（供 Reaper 判断空闲超时）</li>
     *   <li>{@code take(maxConnectionLifetime)}：限制连接最大存活时间（可选）</li>
     *   <li>{@code doFinally}：清理连接（三层清理的第一层，处理 ~95% 的正常断开）</li>
     * </ul>
     * </p>
     *
     * @param sink       Topic 对应的 Sink
     * @param conn       连接记录
     * @param topic      Topic 名称
     * @param metadata   连接元数据
     * @param registry   连接注册表
     * @param metrics    指标收集器
     * @param properties SSE 配置
     * @param listeners  连接监听器列表
     * @return 带生命周期钩子的 Flux
     */
    private static Flux<TopicMessage<Msg>> attachLifecycle(
            Sinks.Many<TopicMessage<Msg>> sink,
            SseConnection conn,
            String topic,
            ConnectionMetadata metadata,
            SseConnectionRegistry registry,
            SseMetrics metrics,
            SseProperties properties,
            List<SseConnectionListener> listeners) {

        Flux<TopicMessage<Msg>> flux = sink.asFlux();

        // 定向投递过滤：广播消息（to 为空）通过所有连接；
        // 定向消息仅通过 userId 匹配的连接
        String userId = metadata != null ? metadata.getUserId() : null;
        flux = flux.filter(msg -> {
            Set<String> to = msg.getTo();
            return (to == null || to.isEmpty())
                    || (userId != null && to.contains(userId));
        });

        flux = flux.doOnSubscribe(sub -> handleOnSubscribe(conn, sub, listeners))
                .doOnNext(msg -> conn.touch());

        // 限制连接最大存活时间：到期后发送 onComplete，触发 doFinally 清理
        // 客户端的 EventSource 会自动重连（SSE 标准行为）
        Duration maxLifetime = properties.getMaxConnectionLifetime();
        if (maxLifetime != null && !maxLifetime.isNegative() && !maxLifetime.isZero()) {
            flux = flux.take(maxLifetime);
        }

        return flux.doFinally(signal -> handleFinally(conn, topic, signal, registry, metrics, listeners));
    }

    /**
     * 处理 onSubscribe 回调.
     * <p>
     * 在 Flux 被实际订阅时执行：
     * <ol>
     *   <li>保存 Subscription 引用（供 Reaper/forceClose 主动取消）</li>
     *   <li>补偿检查：如果在 doOnSubscribe 前已被关闭（如 Shutdown 竞态），立即取消</li>
     *   <li>激活连接（CREATED → ACTIVE）</li>
     *   <li>初始化活跃时间</li>
     *   <li>触发监听器 onConnected 回调</li>
     * </ol>
     * </p>
     *
     * @param conn      连接记录
     * @param sub       Reactive Streams 订阅引用
     * @param listeners 连接监听器列表
     */
    private static void handleOnSubscribe(SseConnection conn,
                                           Subscription sub,
                                           List<SseConnectionListener> listeners) {
        conn.setSubscription(sub);
        // 补偿检查：如果在 doOnSubscribe 前已被关闭（Shutdown 竞态）
        if (conn.isClosed()) {
            sub.cancel();
            return;
        }
        conn.activate();
        conn.touch();
        fireConnected(conn, listeners);
    }

    /**
     * 处理 finally 回调.
     * <p>
     * 三层清理的第一层（响应式层），处理 ~95% 的正常断开。
     * 通过 {@link SseConnection#markClosed()} 与其他层竞争清理权：
     * <ul>
     *   <li>赢得竞争：执行完整清理（计数器递减 + 注销 + 空 Topic 清理）</li>
     *   <li>未赢得：其他层已处理清理，此处跳过</li>
     * </ul>
     * </p>
     *
     * @param conn      连接记录
     * @param topic     Topic 名称
     * @param signal    终止信号类型（ON_COMPLETE / ON_ERROR / CANCEL）
     * @param registry  连接注册表
     * @param metrics   指标收集器
     * @param listeners 连接监听器列表
     */
    private static void handleFinally(SseConnection conn,
                                       String topic,
                                       SignalType signal,
                                       SseConnectionRegistry registry,
                                       SseMetrics metrics,
                                       List<SseConnectionListener> listeners) {
        if (conn.markClosed()) {
            // doFinally 赢得清理权
            registry.cleanupConnection(conn, metrics);
            conn.completeClose();
            log.debug("[Cleanup] 完成: topic={}, connectionId={}, signal={}",
                    topic, conn.getConnectionId(), signal);
            fireDisconnected(conn, signal, listeners);
        } else {
            // 其他层（Reaper / Shutdown / forceClose）已清理
            // 防御性检查：如果连接仍在注册表中（理论上不应发生），补偿清理
            if (registry.isRegistered(conn)) {
                registry.cleanupConnection(conn, metrics);
            }
            // 仍需触发监听器：doFinally 是连接断开的最终确认点，
            // signal 携带实际的终止信号（CANCEL / ON_COMPLETE 等）
            fireDisconnected(conn, signal, listeners);
        }
    }

    /**
     * 触发连接建立监听器回调.
     *
     * @param conn      连接记录
     * @param listeners 监听器列表
     */
    private static void fireConnected(SseConnection conn, List<SseConnectionListener> listeners) {
        if (listeners == null || listeners.isEmpty()) {
            return;
        }
        ConnectionMetadata metadata = conn.getMetadata();
        for (SseConnectionListener listener : listeners) {
            try {
                listener.onConnected(metadata);
            } catch (Exception e) {
                log.warn("[Listener] onConnected 回调异常: listener={}, connectionId={}",
                        listener.getClass().getSimpleName(), conn.getConnectionId(), e);
            }
        }
    }

    /**
     * 触发连接断开监听器回调.
     *
     * @param conn      连接记录
     * @param signal    终止信号类型
     * @param listeners 监听器列表
     */
    private static void fireDisconnected(SseConnection conn,
                                          SignalType signal,
                                          List<SseConnectionListener> listeners) {
        if (listeners == null || listeners.isEmpty()) {
            return;
        }
        ConnectionMetadata metadata = conn.getMetadata();
        for (SseConnectionListener listener : listeners) {
            try {
                listener.onDisconnected(metadata, signal);
            } catch (Exception e) {
                log.warn("[Listener] onDisconnected 回调异常: listener={}, connectionId={}",
                        listener.getClass().getSimpleName(), conn.getConnectionId(), e);
            }
        }
    }

    /**
     * 触发发布异常监听器回调.
     *
     * @param topic     Topic 名称
     * @param result    发布失败的结果
     * @param listeners 监听器列表
     */
    static void firePublishError(String topic,
                                  Sinks.EmitResult result,
                                  List<SseConnectionListener> listeners) {
        if (listeners == null || listeners.isEmpty()) {
            return;
        }
        for (SseConnectionListener listener : listeners) {
            try {
                listener.onPublishError(topic, result);
            } catch (Exception e) {
                log.warn("[Listener] onPublishError 回调异常: listener={}, topic={}",
                        listener.getClass().getSimpleName(), topic, e);
            }
        }
    }
}
