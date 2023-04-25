package cn.gmlee.tools.third.party.tencent.model.req;


import lombok.Data;

import javax.validation.constraints.NotEmpty;

/**
 * 微信支付JS下单请求.
 *
 * @author Jas °
 */
@Data
public class WxPayJsRequest extends WxPayRequest {
    /**
     * 用户编号
     */
    @NotEmpty(message = "用户编号是空")
    private String openId;
}
