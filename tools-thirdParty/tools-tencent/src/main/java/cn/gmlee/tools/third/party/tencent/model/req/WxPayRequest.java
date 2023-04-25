package cn.gmlee.tools.third.party.tencent.model.req;


import cn.gmlee.tools.base.util.IdUtil;
import lombok.Data;

import javax.validation.constraints.*;
import java.io.Serializable;

/**
 * 微信支付请求.
 *
 * @author Jas °
 */
@Data
public class WxPayRequest implements Serializable {
    /**
     * 商户信息-商品信息
     */
    @NotEmpty(message = "商品名称是空 {参考: 店铺名-商品名}")
    private String body = "1號店-商品";
    /**
     * 支付总金额
     */
    @NotNull(message = "支付金额是空 (单位: 分)")
    @Min(value = 1, message = "支付金额小于1分")
    private Long totalFee = 100L;
    /**
     * 商户订单号
     */
    @NotEmpty(message = "店铺订单号是空")
    @Size(min = 8, max = 64, message = "商户订单号长度是8~64位")
    private String outTradeNo = IdUtil.uuidReplace();
    /**
     * 跳转链接
     */
    private String redirectUrl;
    /**
     * 客户IP
     */
    @Null(message = "不能自定义客户端编号")
    private String ip;
}
