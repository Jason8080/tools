package cn.gmlee.tools.im.resume;

import cn.gmlee.tools.im.model.TopicMessage;
import reactor.core.publisher.Flux;

/**
 * 历史消息加载结果.
 *
 * @param messages 按 ID 严格升序的历史消息流（可为空流，不可为 null）
 * @param gapKnown 存储已知无法覆盖客户端位点（如历史已被逐出）；
 *                 true 时框架会向客户端发送 {@code event: resync} 信号提示全量刷新
 * @since 5.7.0
 */
public record HistoryLoadResult(Flux<TopicMessage<?, ?>> messages, boolean gapKnown) {

    /**
     * 空结果（无历史消息，无间隙）.
     */
    public static HistoryLoadResult empty() {
        return new HistoryLoadResult(Flux.empty(), false);
    }

    /**
     * 仅消息（无间隙）.
     *
     * @param messages 历史消息流
     */
    public static HistoryLoadResult of(Flux<TopicMessage<?, ?>> messages) {
        return new HistoryLoadResult(messages, false);
    }

    /**
     * 间隙结果（无法覆盖客户端位点）.
     */
    public static HistoryLoadResult gap() {
        return new HistoryLoadResult(Flux.empty(), true);
    }
}
