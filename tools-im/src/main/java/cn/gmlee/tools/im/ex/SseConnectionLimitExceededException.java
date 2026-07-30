package cn.gmlee.tools.im.ex;

import lombok.Getter;

/**
 * 连接数超限异常.
 * <p>
 * 当全局连接数或单 Topic 连接数达到上限时抛出。
 * </p>
 */
@Getter
public class SseConnectionLimitExceededException extends SseException {

    /**
     * 限制范围
     */
    public enum Scope {
        /** 全局限制 */
        GLOBAL,
        /** 单 Topic 限制 */
        PER_TOPIC
    }

    private final String topic;
    private final int currentCount;
    private final int maxAllowed;
    private final Scope scope;

    public SseConnectionLimitExceededException(String topic, int currentCount, int maxAllowed, Scope scope) {
        super(formatMessage(topic, currentCount, maxAllowed, scope));
        this.topic = topic;
        this.currentCount = currentCount;
        this.maxAllowed = maxAllowed;
        this.scope = scope;
    }

    private static String formatMessage(String topic, int currentCount, int maxAllowed, Scope scope) {
        return switch (scope) {
            case GLOBAL -> String.format("SSE 全局连接数已达上限: %d/%d", currentCount, maxAllowed);
            case PER_TOPIC -> String.format("SSE 主题 [%s] 连接数已达上限: %d/%d", topic, currentCount, maxAllowed);
        };
    }
}
