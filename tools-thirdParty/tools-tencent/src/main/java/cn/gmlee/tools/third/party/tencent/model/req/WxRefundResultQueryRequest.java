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
public class WxRefundResultQueryRequest implements Serializable {
    private String outTradeNo;
    private String outRefundNo;
    private String transactionId;
    private String refundId;

    public WxRefundResultQueryRequest() {
    }

    public WxRefundResultQueryRequest(String outTradeNo, String outRefundNo, String transactionId, String refundId) {
        this.outTradeNo = outTradeNo;
        this.outRefundNo = outRefundNo;
        this.transactionId = transactionId;
        this.refundId = refundId;
    }
}
