package cn.gmlee.tools.third.party.tencent.model.req;

import lombok.Data;

import java.io.Serializable;

/**
 * 微信支付订单查询请求.
 *
 * @author Jas °
 * @date 2021 /3/12 (周五)
 */
@Data
public class WxPayResultQueryRequest implements Serializable {
    private String outTradeNo;
    private String transactionId;

    public WxPayResultQueryRequest() {
    }

    public WxPayResultQueryRequest(String outTradeNo, String transactionId) {
        this.outTradeNo = outTradeNo;
        this.transactionId = transactionId;
    }
}
