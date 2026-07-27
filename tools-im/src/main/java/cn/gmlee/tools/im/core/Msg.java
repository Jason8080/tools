package cn.gmlee.tools.im.core;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * 消息
 */
@Data
public class Msg<T> implements Serializable {
    private Serializable id;
    private Map<String, Object> headers;
    private T body;
    private boolean state; // 消息状态: true-已消费, false-未消费
}
