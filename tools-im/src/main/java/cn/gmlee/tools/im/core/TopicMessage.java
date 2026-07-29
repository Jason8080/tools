package cn.gmlee.tools.im.core;

import lombok.Data;
import org.springframework.util.MultiValueMap;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Data
public class TopicMessage<MSG> implements Serializable {

    /**
     * 全局自增 ID 生成器.
     * <p>
     * 使用 AtomicLong 替代 System.currentTimeMillis()，避免同一毫秒内
     * 创建多条消息时 ID 冲突。序列号从当前时间戳开始，保证跨 JVM 重启后
     *  ID 仍然单调递增（大部分情况下）。
     * </p>
     */
    private static final AtomicLong ID_GENERATOR = new AtomicLong(System.currentTimeMillis());

    private Serializable id = ID_GENERATOR.incrementAndGet();
    private String topic;
    private MultiValueMap<String, String> urlParams;
    private MultiValueMap<String, String> headers;
    private MSG msg;
    private Map<String, Object> metadata = new HashMap<>();
    private boolean state; // 消息状态: true-已消费, false-未消费
    public String topic() {
        return topic;
    }
}
