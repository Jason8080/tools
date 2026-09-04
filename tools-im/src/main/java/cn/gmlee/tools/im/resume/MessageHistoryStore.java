package cn.gmlee.tools.im.resume;

import cn.gmlee.tools.im.model.TopicMessage;
import reactor.core.publisher.Mono;

import java.io.Serializable;

/**
 * 消息历史存储（断点续传数据源）.
 * <p>
 * 框架本身不持久化消息。实现本接口并注册为 Spring Bean，
 * 即可为客户端提供基于 {@code Last-Event-ID} 的断线续传能力：
 * </p>
 * <ul>
 *   <li><b>写入侧</b>：{@link #store} 在消息成功发送后由框架异步调用（{@code ImRepeater.send} 挂点，
 *       CLUSTER / STANDALONE 两种模式一致）</li>
 *   <li><b>读取侧</b>：客户端重连携带 {@code Last-Event-ID} 时，框架调用 {@link #loadSince}
 *       回放缺失消息，再无缝衔接实时流（由 {@link ResumeSupport} 保证零间隙）</li>
 * </ul>
 *
 * <h3>实现契约</h3>
 * <ul>
 *   <li>{@link #loadSince} 返回的消息必须按 ID <b>严格升序</b>（续传去重的基础）</li>
 *   <li>存储须按 ID 索引，容忍消息乱序到达（框架异步写入，不保证顺序）</li>
 *   <li>历史覆盖不全（如已逐出）时通过 {@code gapKnown=true} 告知，
 *       框架会向客户端发送 {@code event: resync} 信号</li>
 *   <li>多个存储 Bean 共存时，按 {@link #getOrder} 排序，首个 {@link #supports} 返回
 *       {@code true} 的存储生效</li>
 *   <li>读取失败抛出异常或阻塞超时不会中断订阅——框架降级为仅实时流</li>
 * </ul>
 *
 * <h3>ID 顺序性前提</h3>
 * <p>
 * 续传正确性要求同 Topic 内 ID 全局唯一且全序。CLUSTER 多实例部署必须配置
 * {@link EventIdGenerator}（如 {@code im.sse.resume.snowflake.enabled=true}）。
 * </p>
 *
 * @since 5.7.0
 * @see ResumeSupport
 */
public interface MessageHistoryStore {

    /**
     * 是否服务指定 Topic.
     *
     * @param topic Topic 名称
     * @return true 表示该存储负责此 Topic 的历史读写
     */
    boolean supports(String topic);

    /**
     * 持久化消息（写入侧）.
     * <p>
     * 框架在消息成功进入传输通道后 fire-and-forget 调用，异常仅记录指标与日志，
     * 不影响发布结果。实现应快速返回（异步落盘），避免拖累发布吞吐。
     * </p>
     *
     * @param message 消息信封（含 ID）
     * @return 完成信号
     */
    default Mono<Void> store(TopicMessage<?, ?> message) {
        return Mono.empty();
    }

    /**
     * 加载指定客户端位点之后的历史消息（读取侧）.
     * <p>
     * 返回 ID 严格大于 {@code lastEventId} 的消息，按 ID 升序。
     * 框架会对结果应用回放上限（{@code im.sse.resume.max-replay}）截断保护。
     * </p>
     *
     * @param topic       Topic 名称
     * @param lastEventId 客户端最后收到的消息 ID（已解码，非 null）
     * @return 加载结果（消息流 + 是否已知存在间隙）
     */
    HistoryLoadResult loadSince(String topic, Serializable lastEventId);

    /**
     * 存储优先级（多存储共存时）.
     *
     * @return Order 值，小的优先
     */
    default int getOrder() {
        return 0;
    }
}
