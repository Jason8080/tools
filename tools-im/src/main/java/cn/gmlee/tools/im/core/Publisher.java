package cn.gmlee.tools.im.core;

import org.springframework.util.MultiValueMap;

import java.io.Serializable;

/**
 * 发布者
 *
 * @param <T> 消息内容
 */
public interface Publisher<T> extends Serializable {
    /**
     * 推送消息.
     *
     * @param topic     the topic
     * @param urlParams
     * @param msg       the t
     * @return Serializable 消息ID
     */
    Serializable push(String topic, MultiValueMap<String, String> urlParams, Msg msg);
}
