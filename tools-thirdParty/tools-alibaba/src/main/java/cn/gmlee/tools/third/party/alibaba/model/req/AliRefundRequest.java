package cn.gmlee.tools.third.party.alibaba.model.req;


import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * 支付宝支付请求.
 *
 * @author Jas °
 */
@Data
public class AliRefundRequest implements Serializable {
    /**
     * 商户退款单号
     */
    @NotEmpty(message = "商户退款单号是空")
    @Size(min = 8, max = 64, message = "商户退款单号长度是8~64位")
    private String outRefundNo;
    /**
     * 商户订单号
     */
    @NotEmpty(message = "商户订单号是空")
    @Size(min = 8, max = 64, message = "商户订单号长度是8~64位")
    private String outTradeNo;
    /**
     * 退款金额
     */
    @NotNull(message = "退款金额是空 (单位: 分)")
    @Min(value = 1, message = "退款金额小于1分")
    private Long refundAmount;
    /**
     * 退款原因
     */
    private String refundReason;
}
