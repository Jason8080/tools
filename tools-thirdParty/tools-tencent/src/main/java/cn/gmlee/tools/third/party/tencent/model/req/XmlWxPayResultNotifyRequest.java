package cn.gmlee.tools.third.party.tencent.model.req;


import cn.gmlee.tools.base.define.Reflect;
import lombok.Data;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Xml版本支付结果通知请求.
 *
 * @author Jas°
 */
@Data
@XmlRootElement(name = "xml")
public class XmlWxPayResultNotifyRequest implements Reflect {
    private String return_code;
    private String return_msg;

    /**
     * return_code: SUCCESS有以下返回, 否则通知失败(是接口的失败)
     * result_code: SUCCESS:是支付结果: SUCCESS/FAIL, 失败提示: err_code_des
     */
    private String appid;
    private String mch_id;
    private String device_info;
    private String nonce_str;
    private String sign;
    private String sign_type;
    private String result_code;
    private String err_code;
    private String err_code_des;
    private String openid;
    private String is_subscribe;
    private String trade_type;
    private String bank_type;
    private Long total_fee;
    private Long settlement_total_fee;
    private String fee_type;
    private Long cash_fee;
    private String cash_fee_type;
    private Long coupon_fee;
    private Long coupon_count;

//    private String coupon_type_0;
//    private String coupon_id_0;
//    private Integer coupon_fee_0;

    private String transaction_id;
    private String out_trade_no;
    private String attach;
    private String time_end;
}
