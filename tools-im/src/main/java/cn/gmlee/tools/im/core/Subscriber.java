package cn.gmlee.tools.im.core;

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
     * @param msg the msg
     * @return Boolean 是否处理成功，true: 处理成功，false: 处理失败， null: 忽略不处理
     */
    Boolean pull(Msg<T> msg);
}
