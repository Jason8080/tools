package cn.gmlee.tools.im.spi.interceptor;

import cn.gmlee.tools.im.core.Repeater;
import cn.gmlee.tools.im.model.TopicMessage;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Repeater 拦截器.
 * <p>
 * 拦截消息流的关键节点，用于审计、持久化、回放等横切关注点。
 * 遵循拦截器模式（Interceptor Pattern），所有方法提供默认空实现，按需重写。
 * </p>
 * <p>
 * <b>注意</b>：{@code beforeSend} 返回 {@code false} 可阻止消息发送，适合参数校验、限流等场景。
 * 拦截器异常会中断调用链（与 Servlet Filter 语义一致）。
 * </p>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * @Component
 * public class AuditInterceptor implements RepeaterInterceptor {
 *
 *     @Override
 *     public Mono<Boolean> beforeSend(TopicMessage<?, ?> message) {
 *         auditService.record(message);   // 审计
 *         return Mono.just(true);         // 允许发送
 *     }
 *
 *     @Override
 *     public Flux<TopicMessage<?, ?>> transformSubscribeStream(
 *             SubscribeContext context, Flux<TopicMessage<?, ?>> stream) {
 *         // 订阅流转换：过滤 / 脱敏 / 富化
 *         return stream.filter(env -> visibleTo(context, env));
 *     }
 * }
 * }</pre>
 *
 * <h3>续传相关约定（v5.7.0+）</h3>
 * <ul>
 *   <li>{@link #transformSubscribeStream} 收到的是<b>信封流</b>（含消息 ID），
 *       且在断点续传包装之后执行——回放消息与实时消息都会经过拦截器。
 *       <b>历史消息持久化/回放请优先使用
 *       {@link cn.gmlee.tools.im.resume.MessageHistoryStore} SPI</b>，
 *       它提供零间隙续接、水位线去重与降级保护，拦截器不再需要自行拼接历史。</li>
 *   <li>流中可能出现 {@link cn.gmlee.tools.im.model.ResumeSignal} 信号信封
 *       （{@code id=null}），拦截器应保持透传，不要丢弃或改写。</li>
 * </ul>
 * <p>
 * 实现类注册为 Spring Bean 后，框架自动织入所有 {@link Repeater}，零配置生效。
 * </p>
 *
 * @since 5.6.0
 */
public interface RepeaterInterceptor {

    /**
     * 发送前拦截（响应式）.
     * <p>
     * 在消息发送到 MQ 之前调用。可用于：消息持久化、审计日志、参数校验。
     * </p>
     * <p>
     * 返回 {@code Mono.just(true)} 允许发送，返回 {@code Mono.just(false)} 拦截消息（不再发送）。
     * 多个拦截器任一返回 {@code false} 即终止发送。
     * </p>
     *
     * @param message 待发送的消息
     * @return {@code Mono<Boolean>} - true 允许发送，false 拦截消息
     */
    default Mono<Boolean> beforeSend(TopicMessage<?, ?> message) {
        return Mono.just(true);
    }

    /**
     * 接收后拦截（响应式）（MQ 消费后、推送 SSE 前）.
     * <p>
     * 在从 MQ 接收到消息后、推送到 SSE 连接之前调用。
     * 可用于：指标收集、日志记录、旁路通知。
     * </p>
     * <p>
     * 消息持久化（用于断点续传）请使用
     * {@link cn.gmlee.tools.im.resume.MessageHistoryStore#store}，
     * 它在发送成功挂点触发，覆盖 CLUSTER / STANDALONE 两种模式。
     * </p>
     *
     * @param message 接收到的消息
     * @return {@code Mono<Void>} 表示异步操作完成
     */
    default Mono<Void> afterReceive(TopicMessage<?, ?> message) {
        return Mono.empty();
    }

    /**
     * 转换订阅流（信封流签名，v5.7.0）.
     * <p>
     * 在返回 SSE 消息流之前调用，可对信封流进行转换（过滤、脱敏、富化）。
     * 默认直接返回原始流。
     * </p>
     * <p>
     * 执行时机位于断点续传包装之后：入参流已包含回放消息（若客户端在续传）。
     * 需要消息上下文的实现可从 {@link SubscribeContext} 获取 Topic、URL 参数与
     * 连接元数据（含续传位点 {@link SubscribeContext#lastEventId()}）。
     * </p>
     *
     * @param context 订阅上下文（Topic / URL 参数 / 连接元数据）
     * @param stream  原始信封流（可能含回放消息与 {@code ResumeSignal} 信号）
     * @return 转换后的信封流
     */
    default Flux<TopicMessage<?, ?>> transformSubscribeStream(
            SubscribeContext context,
            Flux<TopicMessage<?, ?>> stream) {
        return stream;
    }
}
