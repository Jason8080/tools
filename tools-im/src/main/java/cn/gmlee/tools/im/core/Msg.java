package cn.gmlee.tools.im.core;

import org.springframework.util.MultiValueMap;

import java.io.Serializable;

/**
 * 消息
 */
public interface Msg extends Serializable {
    /**
     * Build msg event.
     *
     * @param urlParams the url params
     * @return the msg event
     */
    TopicMessage<Msg> build(MultiValueMap<String, String> urlParams);

}
