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
public class XmlWxRefundResultNotifyRequest implements Reflect {
    private String return_code;
    private String return_msg;

    /**
     * return_code: SUCCESS有以下返回, 否则通知失败(是接口的失败)
     * result_code: SUCCESS:是支付结果: SUCCESS/FAIL, 失败提示: err_code_des
     */
    private String appid;
    private String mch_id;
    private String nonce_str;
    private String req_info;

    @Data
    public static class CiphertextInfo {
        private String transaction_id;
        private String out_trade_no;
        private String refund_id;
        private String out_refund_no;
        private Long total_fee;
        private Long settlement_total_fee;
        private Long refund_fee;
        private Long settlement_refund_fee;
        private Long refund_status;
        /**
         * 2017-12-15 09:46:01
         */
        private String success_time;
        private String refund_recv_accout;
        private String refund_account;
        private String refund_request_source;
    }
}
