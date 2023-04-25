package cn.gmlee.tools.third.party.alibaba.model.req;

import cn.gmlee.tools.base.define.Reflect;
import lombok.Data;

/**
 * @author Jas°
 * @date 2021/3/9 (周二)
 */
@Data
public class AliPayResultNotifyRequest implements Reflect {
    private String out_trade_no;
    private String trade_no;
    /**
     * TRADE_FINISHED: 交易完成, 不可退款了 (后续还会收到这个通知)
     * TRADE_SUCCESS: 交易成功, 还可以退款 (先收此通知)
     */
    private String trade_status;
}
