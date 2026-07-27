package cn.gmlee.tools.im.core;

import org.springframework.util.MultiValueMap;

import java.io.Serializable;

/**
 * 订阅者
 *
 * @param <MSG> the type parameter
 */
public interface Subscriber<MSG> extends Topic, Serializable {
    /**
     * 拉取消息.
     *
     * @param urlParams the url params
     * @return msg 消息内容
     */
    MSG pull(MultiValueMap<String, String> urlParams);
}
