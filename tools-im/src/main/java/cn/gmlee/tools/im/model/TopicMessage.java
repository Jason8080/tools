package cn.gmlee.tools.im.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.MultiValueMap;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopicMessage<MSG> implements Serializable {

    /**
     * 全局自增 ID 生成器.
     * <p>
     * 使用 AtomicLong 替代 System.currentTimeMillis()，避免同一毫秒内
     * 创建多条消息时 ID 冲突。序列号从当前时间戳开始，保证跨 JVM 重启后
     * ID 仍然单调递增（大部分情况下）。
     * </p>
     * <p>
     * <b>唯一性范围</b>：仅保证单 JVM 生命周期内唯一。分布式环境下可能存在 ID 冲突，
     * 建议开发者根据场景自定义 ID 生成策略（如 UUID、雪花算法），通过
     * {@code TopicMessage.builder().id(customId).build()} 覆盖默认值。
     * </p>
     */
    private static final AtomicLong ID_GENERATOR = new AtomicLong(System.currentTimeMillis());

    /**
     * 消息 ID.
     * <p>
     * 默认使用全局自增 ID（单 JVM 唯一）。分布式场景建议自定义。
     * </p>
     */
    @Builder.Default
    private Serializable id = ID_GENERATOR.incrementAndGet();
    private String topic;
    /**
     * 路由目标集合（定向投递）.
     * <p>
     * null 或空集 = 广播到 Topic 下所有连接；
     * 非空 = 仅投递给 routingKey 匹配的连接。
     * </p>
     */
    private Set<String> routingKey;
    private MultiValueMap<String, String> urlParams;
    private MultiValueMap<String, String> headers;
    private MSG msg;
    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();
    private boolean state; // 消息状态: true-已消费, false-未消费
    public String topic() {
        return topic;
    }
}
