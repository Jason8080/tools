package cn.gmlee.tools.im.core;

import java.io.Serializable;

/**
 * 消息
 */
public interface Msg extends Serializable {
    /**
     * Build msg event.
     *
     * @return the msg event
     */
    MsgEvent<Msg> build();
}
