package cn.gmlee.tools.im.core;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

@Data
public class MsgEvent<T> implements Serializable {
    private Serializable id = System.currentTimeMillis();
    private Map<String, Object> headers;
    private T body;
    private boolean state; // 消息状态: true-已消费, false-未消费
}
