package cn.gmlee.tools.third.party.tencent.model.req;


import lombok.Data;

import javax.validation.constraints.*;
import java.io.Serializable;

/**
 * 微信支付请求.
 *
 * @author Jas °
 */
@Data
public class WxRefundRequest implements Serializable {
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
     * 支付总金额
     */
    @NotNull(message = "订单金额是空 (单位: 分)")
    @Min(value = 1, message = "订单金额小于1分")
    private Long totalFee;
    /**
     * 退款金额
     */
    @NotNull(message = "退款金额是空 (单位: 分)")
    @Min(value = 1, message = "退款金额小于1分")
    private Long refundFee;
    /**
     * 退款原因
     */
    private String refundDesc;
    /**
     * 通知地址: 用于覆盖商户号的后台配置
     */
    private String notifyUrl;
}
