package cn.gmlee.tools.im.model;

/**
 * 续传信号消息（sentinel）.
 * <p>
 * 框架通过消息流本身传递「续传协议信号」，而非旁路通道，
 * 保证信号与业务消息的严格顺序性（信号一定出现在它描述的位置）。
 * </p>
 *
 * <h3>resync 信号</h3>
 * <p>
 * 当存储无法覆盖客户端位点（历史已被逐出、回放超限、待缓冲队列溢出等），
 * 框架向客户端发送 {@code event: resync} 命名事件。客户端收到后应丢弃本地
 * 增量状态，发起全量刷新（重新拉取完整数据）。
 * </p>
 * <p>
 * 该信号以 {@link Msg} 形式承载于 {@link TopicMessage} 信封中
 * （{@code id=null}），随正常订阅流下行；拦截器实现应保持透传
 * （不要丢弃或改写载荷为该类型的信封）。
 * </p>
 *
 * @since 5.7.0
 */
public final class ResumeSignal implements Msg {

    /**
     * SSE 事件名称：提示客户端全量刷新.
     */
    public static final String EVENT_RESYNC = "resync";

    private static final ResumeSignal RESYNC = new ResumeSignal(EVENT_RESYNC);

    private final String eventName;

    private ResumeSignal(String eventName) {
        this.eventName = eventName;
    }

    /**
     * 获取 resync 信号单例.
     *
     * @return resync 信号
     */
    public static ResumeSignal resync() {
        return RESYNC;
    }

    /**
     * 构建承载该信号的信封（框架内部使用）.
     * <p>
     * 信号信封 {@code id} 为 null：不参与水位线比较（水位线仅在 ID 非 null 时推进），
     * 不写入历史存储。
     * </p>
     *
     * @param topic Topic 名称
     * @return 信号信封
     */
    public static TopicMessage<java.io.Serializable, ResumeSignal> envelope(String topic) {
        TopicMessage<java.io.Serializable, ResumeSignal> env = new TopicMessage<>();
        env.setId(null);
        env.setTopic(topic);
        env.setMsg(RESYNC);
        return env;
    }

    /**
     * SSE 事件名称（写入 {@code event:} 字段）.
     *
     * @return 事件名称
     */
    public String eventName() {
        return eventName;
    }

    @Override
    public String toString() {
        return "ResumeSignal[" + eventName + "]";
    }
}
