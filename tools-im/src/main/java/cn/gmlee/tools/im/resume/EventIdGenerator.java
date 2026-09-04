package cn.gmlee.tools.im.resume;

import java.io.Serializable;

/**
 * 事件 ID 生成器.
 * <p>
 * 为 {@code Publisher.push()} 路径自动生成的 {@link cn.gmlee.tools.im.model.TopicMessage}
 * 分配消息 ID。框架默认使用单 JVM 自增序列（{@code TopicMessage} 内置），
 * 该序列仅保证单实例内唯一有序。
 * </p>
 *
 * <h3>何时需要自定义</h3>
 * <p>
 * <b>CLUSTER 多实例 + 断点续传场景必须配置全局有序 ID</b>（如
 * {@link SnowflakeEventIdGenerator}，{@code im.sse.resume.snowflake.enabled=true}），
 * 否则各实例自增序列交叉，水位线过滤与「按 ID 升序回放」都会出错。
 * </p>
 * <p>
 * 注意：通过 {@code Repeater.send(TopicMessage)} 直接投递的消息携带调用方
 * 显式构造的 ID，不受本生成器影响。
 * </p>
 *
 * @since 5.7.0
 */
public interface EventIdGenerator {

    /**
     * 生成下一个消息 ID.
     * <p>
     * 实现必须保证同 Topic（理想情况下全局）唯一且单调递增。
     * 此方法在发布热路径上调用，实现应无阻塞、低争用。
     * </p>
     *
     * @param topic Topic 名称（允许按 Topic 分派不同策略）
     * @return 消息 ID（非 null）
     */
    Serializable nextId(String topic);
}
