package cn.gmlee.tools.im.core;

import lombok.Data;
import org.springframework.util.MultiValueMap;

import java.io.Serializable;

@Data
public class MsgEvent<T> implements Serializable {
    private Serializable id = System.currentTimeMillis();
    private MultiValueMap<String, String> urlParams;
    private MultiValueMap<String, String> headers;
    private T body;
    private boolean state; // 消息状态: true-已消费, false-未消费
}
