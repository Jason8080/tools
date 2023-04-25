package cn.gmlee.tools.third.party.alibaba.model.req;

import cn.gmlee.tools.base.define.Reflect;
import lombok.Data;

/**
 * @author Jas°
 * @date 2021/3/12 (周五)
 */
@Data
public class AliRefundResultQueryRequest implements Reflect {
    private String outTradeNo;
    private String outRefundNo;
    private String tradeNo;

    public AliRefundResultQueryRequest() {
    }

    public AliRefundResultQueryRequest(String outTradeNo, String outRefundNo, String tradeNo) {
        this.outTradeNo = outTradeNo;
        this.outRefundNo = outRefundNo;
        this.tradeNo = tradeNo;
    }
}
