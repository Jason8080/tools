package cn.gmlee.tools.im.core;

import lombok.Data;

import java.io.Serializable;

@Data
public class MsgEvent implements Serializable {
    private long timestamp = System.currentTimeMillis();
}
