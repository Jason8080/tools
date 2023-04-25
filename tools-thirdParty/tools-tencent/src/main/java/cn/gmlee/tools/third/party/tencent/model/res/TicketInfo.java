package cn.gmlee.tools.third.party.tencent.model.res;

import lombok.Data;

import java.io.Serializable;

/**
 * 获取的Ticket信息
 *
 * @author Jas°
 * @date 2020/10/12 (周一)
 */
@Data
public class TicketInfo implements Serializable {
    private String ticket;
    private Integer expires_in;
    private Integer errcode;
    private String errmsg;
}
