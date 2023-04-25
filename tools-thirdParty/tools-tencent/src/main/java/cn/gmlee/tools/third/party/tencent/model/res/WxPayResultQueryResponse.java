package cn.gmlee.tools.third.party.tencent.model.res;


import cn.gmlee.tools.base.define.Reflect;
import lombok.Data;

/**
 * Xml版本支付结果通知请求.
 *
 * @author Jas°
 */
@Data
public class WxPayResultQueryResponse implements Reflect {
    private String return_code;
    private String return_msg;

    /**
     * return_code: SUCCESS的时候有返回
     */
    private String appid;
    private String mch_id;
    private String nonce_str;
    private String sign;
    private String result_code;
    private String err_code;
    private String err_code_des;
    /**
     * return_code 、result_code、trade_state: 都是SUCCESS时有返回
     * trade_state不为SUCCESS，则只返回out_trade_no、trade_state_desc和attach
     */
    private String device_info;
    private String openid;
    private String is_subscribe;
    private String trade_type;
    private String trade_state;
    private String bank_type;
    private String total_fee;
    private String settlement_total_fee;
    private String fee_type;
    private String cash_fee;
    private String cash_fee_type;
    private String coupon_fee;
    private String coupon_count;
    private String transaction_id;
    private String out_trade_no;
    private String attach;
    private String time_end;
    private String trade_state_desc;
}
