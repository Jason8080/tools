package cn.gmlee.tools.im.core;

/**
 * 消息传输抽象 — 解耦核心类与 Spring Cloud Stream.
 * <p>
 * 框架内部使用，将 {@code StreamBridge.send()} 抽象为函数接口。
 * CLUSTER 模式由 {@code ClusterAutoConfiguration} 通过方法引用适配；
 * STANDALONE 模式传 {@code null}（不需要 MQ 传输）。
 * </p>
 *
 * @since 5.6.0
 */
@FunctionalInterface
public interface MessageSender {

    /**
     * 发送消息到指定目标.
     *
     * @param destination 目标名称（如 Spring Cloud Stream 的 output binding 名）
     * @param message     消息对象
     */
    void send(String destination, Object message);
}
