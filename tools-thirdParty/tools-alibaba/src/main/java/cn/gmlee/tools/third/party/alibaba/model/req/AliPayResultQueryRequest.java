package cn.gmlee.tools.third.party.alibaba.model.req;

import cn.gmlee.tools.base.define.Reflect;
import lombok.Data;

/**
 * @author Jas°
 * @date 2021/3/12 (周五)
 */
@Data
public class AliPayResultQueryRequest implements Reflect {
    private String outTradeNo;
    private String tradeNo;

    public AliPayResultQueryRequest() {
    }

    public AliPayResultQueryRequest(String outTradeNo, String tradeNo) {
        this.outTradeNo = outTradeNo;
        this.tradeNo = tradeNo;
    }
}
