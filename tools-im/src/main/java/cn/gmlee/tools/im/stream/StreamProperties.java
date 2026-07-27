package cn.gmlee.tools.im.stream;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Stream 集成配置属性
 *
 * @author SseIm Framework
 * @since 1.0.0
 */
@Data
@ConfigurationProperties(prefix = "sse-im.stream")
public class StreamProperties {

    /**
     * 是否启用多实例广播（通过 Spring Cloud Stream）
     */
    private boolean enabled = false;

    /**
     * 广播目标名称（对应 Spring Cloud Stream 的 destination）
     */
    private String broadcastDestination = "sse-im-broadcast";

    /**
     * 消费者预取数（性能关键参数）
     * <p>
     * 注意：Spring Cloud Stream 默认 prefetch=1，会严重限制吞吐量。
     * 建议设为 100-500。
     * </p>
     */
    private int consumerPrefetch = 250;

    /**
     * 消费者并发数
     */
    private int consumerConcurrency = 4;

    /**
     * 是否跳过本实例发出的消息（避免重复处理）
     */
    private boolean skipSelfMessages = true;
}
