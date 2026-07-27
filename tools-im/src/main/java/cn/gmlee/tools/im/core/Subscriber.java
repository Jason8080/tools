package cn.gmlee.tools.im.core;

import org.springframework.util.MultiValueMap;

import java.io.Serializable;

/**
 * 订阅者
 *
 * @param <T> 消息内容
 */
public interface Subscriber<T> extends Serializable {
    /**
     * 拉取消息.
     *
     * @param topic     the topic
     * @param urlParams the url params
     * @return t 消息内容
     */
    T pull(String topic, MultiValueMap<String, String> urlParams);
}
