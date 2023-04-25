package cn.gmlee.tools.third.party.tencent.model.req;

import java.io.Serializable;

/**
 * 消息类型枚举
 *
 * @author Jas°
 * @date 2020/12/8 (周二)
 */
public class MessageType implements Serializable {
    public static final String TEXT = "text";
    public static final String IMAGE = "image";
    public static final String LINK = "link";
    public static final String MINI_PROGRAM_PAGE = "miniprogrampage";
}
