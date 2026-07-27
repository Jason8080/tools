package cn.gmlee.tools.im.core;

import lombok.Data;
import org.springframework.util.MultiValueMap;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@Data
public class TopicMessage<T> implements Topic, Serializable {
    private Serializable id = System.currentTimeMillis();
    private String topic;
    private MultiValueMap<String, String> urlParams;
    private MultiValueMap<String, String> headers;
    private T body;
    private Map<String, Object> metadata = new HashMap<>();
    private boolean state; // 消息状态: true-已消费, false-未消费
    @Override
    public String topic() {
        return topic;
    }
}
