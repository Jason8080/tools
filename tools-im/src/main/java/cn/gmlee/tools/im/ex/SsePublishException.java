package cn.gmlee.tools.im.ex;

import reactor.core.publisher.Sinks;

/**
 * 消息发布异常.
 * <p>
 * 当消息发布到 Sink 失败时抛出。
 * </p>
 */
public class SsePublishException extends SseException {

    private final String topic;
    private final Sinks.EmitResult emitResult;

    public SsePublishException(String topic, Sinks.EmitResult emitResult) {
        super(String.format("SSE 消息发布到主题 [%s] 失败: %s", topic, emitResult));
        this.topic = topic;
        this.emitResult = emitResult;
    }

    public SsePublishException(String topic, Sinks.EmitResult emitResult, Throwable cause) {
        super(String.format("SSE 消息发布到主题 [%s] 失败: %s", topic, emitResult), cause);
        this.topic = topic;
        this.emitResult = emitResult;
    }

    public String getTopic() {
        return topic;
    }

    public Sinks.EmitResult getEmitResult() {
        return emitResult;
    }
}
