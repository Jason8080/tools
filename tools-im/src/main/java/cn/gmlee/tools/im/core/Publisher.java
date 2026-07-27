package cn.gmlee.tools.im.core;

import org.springframework.util.MultiValueMap;

import java.io.Serializable;

/**
 * 发布者
 *
 * @param <ID>  the type parameter
 * @param <MSG> the type parameter
 */
public interface Publisher<ID, MSG> extends Topic, Serializable {
    /**
     * 推送消息.
     *
     * @param urlParams the url params
     * @param msg       the msg
     * @return id 消息ID
     */
    ID push(MultiValueMap<String, String> urlParams, MSG msg);
}
