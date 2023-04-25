package cn.gmlee.tools.third.party.tencent;

import cn.gmlee.tools.base.util.AssertUtil;
import cn.gmlee.tools.third.party.tencent.config.WxPayConfiguration;
import com.github.wxpay.sdk.WXPayConfig;
import com.github.wxpay.sdk.WXPayUtil;

/**
 * .
 *
 * @author Jas°
 * @date 2021/7/21 (周三)
 */
public class SignTests {
    public static void main(String[] args) throws Exception {
        xml2Bean();
    }

    public static void xml2Bean() throws Exception {
        String xml = getXml();
        boolean signVerified = WXPayUtil.isSignatureValid(xml, getConfiguration().getKey());
        AssertUtil.isTrue(signVerified, String.format("微信支付结果通知验签不通过: {}", "map"));
    }

    private static WXPayConfig getConfiguration() {
        WxPayConfiguration wc = new WxPayConfiguration();
        wc.setAppId("wxda89b9793e36f2d4");
        wc.setMchId("1568379541");
        wc.setKey("3ee754037e6e4e3db5d1d070bfb2d157");
        wc.setCert("/home/data/images/upload/cert/lala-pay/1568379541/apiclient_cert.p12");
        wc.setHttpConnectTimeoutMs(2000);
        wc.setHttpReadTimeoutMs(3000);
        wc.setCurrency("CNY");
        wc.setReturnUrl("https://lala-pay.gmleezuche.cn/api/cashier-desk/wx-pay/notify");
        wc.setNotifyUrl("https://lala-pay.gmleezuche.cn/api/cashier-desk/wx-pay/notify");
        wc.setUseSandbox(false);
        return wc;
    }

    private static String getXml() {
        String xml = "<xml>\n" +
                "<appid><![CDATA[wxda89b9793e36f2d4]]></appid>\n" +
                "<bank_type><![CDATA[PAB_CREDIT]]></bank_type>\n" +
                "<cash_fee><![CDATA[49800]]></cash_fee>\n" +
                "<coupon_count><![CDATA[1]]></coupon_count>\n" +
                "<coupon_fee>200</coupon_fee>\n" +
                "<coupon_fee_0><![CDATA[200]]></coupon_fee_0>\n" +
                "<coupon_id_0><![CDATA[25357873732]]></coupon_id_0>\n" +
                "<fee_type><![CDATA[CNY]]></fee_type>\n" +
                "<is_subscribe><![CDATA[N]]></is_subscribe>\n" +
                "<mch_id><![CDATA[1568379541]]></mch_id>\n" +
                "<nonce_str><![CDATA[e35a06b17b6d4966aa4e8e0ed131dbeb]]></nonce_str>\n" +
                "<openid><![CDATA[omokywS6sqZ83rHPloB7lMz07wBA]]></openid>\n" +
                "<out_trade_no><![CDATA[PP90039056902786]]></out_trade_no>\n" +
                "<result_code><![CDATA[SUCCESS]]></result_code>\n" +
                "<return_code><![CDATA[SUCCESS]]></return_code>\n" +
                "<sign><![CDATA[3698FD03035B6AA5CCCA950017ACE237]]></sign>\n" +
                "<time_end><![CDATA[20210721102125]]></time_end>\n" +
                "<total_fee>50000</total_fee>\n" +
                "<trade_type><![CDATA[JSAPI]]></trade_type>\n" +
                "<transaction_id><![CDATA[4200001158202107216995219711]]></transaction_id>\n" +
                "</xml>";
        return xml;
    }
}
