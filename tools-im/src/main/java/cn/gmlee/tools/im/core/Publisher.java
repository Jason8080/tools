package cn.gmlee.tools.im.core;

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
     * @param t the t
     * @return Serializable 消息ID
     */
    Serializable push(T t);
}
