package cn.gmlee.tools.third.party.tencent.kit;

import cn.gmlee.tools.base.util.BoolUtil;
import cn.gmlee.tools.base.util.TimeUtil;
import cn.gmlee.tools.third.party.tencent.config.WxPayConfiguration;
import cn.gmlee.tools.third.party.tencent.model.req.*;
import com.github.wxpay.sdk.WXPay;
import com.github.wxpay.sdk.WXPayConstants;
import com.github.wxpay.sdk.WXPayUtil;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 通用微信支付工具
 *
 * @author Jas °
 * @date 2021 /3/6 (周六)
 */
public class WxPayKit {


    /**
     * 统一下单.
     *
     * @param wxPayConfiguration 微信支付配置
     * @param data               请求数据
     * @return map map
     * @throws Exception the exception
     */
    public static Map<String, String> unifiedOrder(WxPayConfiguration wxPayConfiguration, Map<String, String> data) throws Exception {
        return new WXPay(wxPayConfiguration, WXPayConstants.SignType.MD5, wxPayConfiguration.getUseSandbox()).unifiedOrder(data);
    }

    /**
     * 订单查询
     *
     * @param wxPayConfiguration the wx pay configuration
     * @param data               the data
     * @return map map
     * @throws Exception the exception
     */
    public static Map<String, String> orderQuery(WxPayConfiguration wxPayConfiguration, Map<String, String> data) throws Exception {
        return new WXPay(wxPayConfiguration, WXPayConstants.SignType.MD5, wxPayConfiguration.getUseSandbox()).orderQuery(data);
    }


    /**
     * 申请退款.
     *
     * @param wxPayConfiguration the wx pay configuration
     * @param data               the data
     * @return the map
     * @throws Exception the exception
     */
    public static Map<String, String> applyRefund(WxPayConfiguration wxPayConfiguration, Map<String, String> data) throws Exception {
        return new WXPay(wxPayConfiguration, WXPayConstants.SignType.MD5, wxPayConfiguration.getUseSandbox()).refund(data);
    }

    /**
     * 退款查询.
     *
     * @param wxPayConfiguration the wx pay configuration
     * @param data               the data
     * @return the map
     * @throws Exception the exception
     */
    public static Map<String, String> refundQuery(WxPayConfiguration wxPayConfiguration, Map<String, String> data) throws Exception {
        return new WXPay(wxPayConfiguration, WXPayConstants.SignType.MD5, wxPayConfiguration.getUseSandbox()).refundQuery(data);
    }


    /**
     * Gets request data.
     * 仅针对老资金流商户使用
     * REFUND_SOURCE_UNSETTLED_FUNDS---未结算资金退款（默认使用未结算资金退款）
     * REFUND_SOURCE_RECHARGE_FUNDS---可用余额退款
     *
     * @param wxPayConfiguration the wx pay configuration
     * @param wxRefundRequest    the wx refund request
     * @return the request data
     * @throws Exception the exception
     */
    public static Map<String, String> getRequestData(WxPayConfiguration wxPayConfiguration, WxRefundRequest wxRefundRequest) throws Exception {
        Map<String, String> data = new HashMap<>(7);
        // 回调地址: 支付结果通知接口
        if (BoolUtil.notEmpty(wxRefundRequest.getNotifyUrl())) {
            data.put("notify_url", wxRefundRequest.getNotifyUrl());
        }
        // 订单总金额
        data.put("total_fee", wxRefundRequest.getTotalFee().toString());
        // 默认人民币
        data.put("fee_type", wxPayConfiguration.getCurrency());
        // 商户订单号
        data.put("out_trade_no", wxRefundRequest.getOutTradeNo());
        // 商户退单号
        data.put("out_refund_no", wxRefundRequest.getOutRefundNo());
        // 退款金额
        data.put("refund_fee", wxRefundRequest.getRefundFee().toString());
        // 退款原因
        data.put("refund_desc", wxRefundRequest.getRefundDesc());
        // 退款资金来源
//        data.put("refund_account", "REFUND_SOURCE_RECHARGE_FUNDS")
        return data;
    }

    /**
     * Gets request data.
     *
     * @param wxPayConfiguration the h 5 pay configuration
     * @param wxPayH5Request     the h 5 unified order request
     * @return the request data
     * @throws Exception the exception
     */
    public static Map<String, String> getRequestData(WxPayConfiguration wxPayConfiguration, WxPayH5Request wxPayH5Request) throws Exception {
        Map<String, String> data = new HashMap<>(7);
        // 支付场景 APP 微信app支付 JSAPI 公众号支付  NATIVE 扫码支付 MWEB H5支付
        data.put("trade_type", "MWEB");
        // 回调地址: 支付结果通知接口
        data.put("notify_url", wxPayConfiguration.getNotifyUrl());
        // 客户端地址
        data.put("spbill_create_ip", wxPayH5Request.getIp());
        // 订单总金额
        data.put("total_fee", wxPayH5Request.getTotalFee().toString());
        // 默认人民币
        data.put("fee_type", wxPayConfiguration.getCurrency());
        // 商户订单号
        data.put("out_trade_no", wxPayH5Request.getOutTradeNo());
        // 商品信息
        data.put("body", wxPayH5Request.getBody());
        return data;
    }

    /**
     * Gets request data.
     *
     * @param wxPayConfiguration the native pay configuration
     * @param wxPayNativeRequest the native unified order request
     * @return the request data
     * @throws Exception the exception
     */
    public static Map<String, String> getRequestData(WxPayConfiguration wxPayConfiguration, WxPayNativeRequest wxPayNativeRequest) throws Exception {
        Map<String, String> data = new HashMap<>(7);
        // 支付场景 APP 微信app支付 JSAPI 公众号支付  NATIVE 扫码支付 MWEB H5支付
        data.put("trade_type", "NATIVE");
        // 回调地址: 支付结果通知接口
        data.put("notify_url", wxPayConfiguration.getNotifyUrl());
        // 客户端地址
        data.put("spbill_create_ip", wxPayNativeRequest.getIp());
        // 订单总金额
        data.put("total_fee", wxPayNativeRequest.getTotalFee().toString());
        // 默认人民币
        data.put("fee_type", wxPayConfiguration.getCurrency());
        // 商户订单号
        data.put("out_trade_no", wxPayNativeRequest.getOutTradeNo());
        // 商品信息
        data.put("body", wxPayNativeRequest.getBody());
        return data;
    }

    /**
     * Gets request data.
     *
     * @param wxPayConfiguration the js pay configuration
     * @param wxPayJsRequest     the js unified order request
     * @return the request data
     * @throws Exception the exception
     */
    public static Map<String, String> getRequestData(WxPayConfiguration wxPayConfiguration, WxPayJsRequest wxPayJsRequest) throws Exception {
        Map<String, String> data = new HashMap(12);
        // 用户编号
        data.put("openid", wxPayJsRequest.getOpenId());
        // 支付场景 APP 微信app支付 JSAPI 公众号支付  NATIVE 扫码支付 MWEB H5支付
        data.put("trade_type", "JSAPI");
        // 回调地址: 支付结果通知接口
        data.put("notify_url", wxPayConfiguration.getNotifyUrl());
        // 客户端地址
        data.put("spbill_create_ip", wxPayJsRequest.getIp());
        // 订单总金额
        data.put("total_fee", wxPayJsRequest.getTotalFee().toString());
        // 默认人民币
        data.put("fee_type", wxPayConfiguration.getCurrency());
        // 商户订单号
        data.put("out_trade_no", wxPayJsRequest.getOutTradeNo());
        // 商品信息
        data.put("body", wxPayJsRequest.getBody());
        return data;
    }

    /**
     * Gets request data.
     *
     * @param wxPayConfiguration the wx pay configuration
     * @param wxPayAppRequest    the wx pay app request
     * @return the request data
     * @throws Exception the exception
     */
    public static Map<String, String> getRequestData(WxPayConfiguration wxPayConfiguration, WxPayAppRequest wxPayAppRequest) throws Exception {
        Map<String, String> data = new HashMap(12);
        // 支付场景 APP 微信app支付 JSAPI 公众号支付  NATIVE 扫码支付 MWEB H5支付
        data.put("trade_type", "APP");
        // 回调地址: 支付结果通知接口
        data.put("notify_url", wxPayConfiguration.getNotifyUrl());
        // 客户端地址
        data.put("spbill_create_ip", wxPayAppRequest.getIp());
        // 订单总金额
        data.put("total_fee", wxPayAppRequest.getTotalFee().toString());
        // 默认人民币
        data.put("fee_type", wxPayConfiguration.getCurrency());
        // 商户订单号
        data.put("out_trade_no", wxPayAppRequest.getOutTradeNo());
        // 商品信息
        data.put("body", wxPayAppRequest.getBody());
        return data;
    }


    /**
     * 查询订单需要的请求参数
     *
     * @param wxPayConfiguration      the wx pay configuration
     * @param wxPayResultQueryRequest the wx pay query request
     * @return request data
     * @throws Exception the exception
     */
    public static Map<String, String> getRequestData(WxPayConfiguration wxPayConfiguration, WxPayResultQueryRequest wxPayResultQueryRequest) throws Exception {
        Map<String, String> data = new HashMap(12);
        data.put("out_trade_no", wxPayResultQueryRequest.getOutTradeNo());
        return data;
    }

    public static Map<String, String> getRequestData(WxPayConfiguration wxPayConfiguration, WxRefundResultQueryRequest wxRefundResultQueryRequest) throws Exception {
        Map<String, String> data = new HashMap(10);
        // 商户订单号
        data.put("out_trade_no", wxRefundResultQueryRequest.getOutTradeNo());
        return data;
    }


    /**
     * Gets h 5 response.
     *
     * @param res         the res
     * @param redirectUrl the redirect url
     * @return the h 5 response
     */
    public static String getH5Response(Map<String, String> res, String redirectUrl) {
        String mweb_url = res.get("mweb_url");
        if (StringUtils.isEmpty(redirectUrl)) {
            return mweb_url;
        }
        return mweb_url + "&redirect_url=" + redirectUrl;
    }

    /**
     * Gets native response.
     *
     * @param res         the res
     * @param redirectUrl the redirect url
     * @return the native response
     */
    public static String getNativeResponse(Map<String, String> res, String redirectUrl) {
        String code_url = res.get("code_url");
        if (StringUtils.isEmpty(redirectUrl)) {
            return code_url;
        }
        return code_url + "&redirect_url=" + redirectUrl;
    }

    /**
     * Gets js response.
     *
     * @param res                the res
     * @param wxPayConfiguration the wx pay configuration
     * @return the js response
     * @throws Exception the exception
     */
    public static Map<String, String> getJsResponse(Map<String, String> res, WxPayConfiguration wxPayConfiguration) throws Exception {
        String prepay_id = res.get("prepay_id");
        String nonce_str = res.get("nonce_str");
        Map<String, String> parameterMap = new HashMap<>(6);
        parameterMap.put("appId", wxPayConfiguration.getAppID());
        parameterMap.put("timeStamp", TimeUtil.getCurrentMs());
        parameterMap.put("nonceStr", nonce_str);
        parameterMap.put("package", "prepay_id=" + prepay_id);
        parameterMap.put("signType", WXPayConstants.MD5);
        // 生成支付签名
        String paySign = WXPayUtil.generateSignature(parameterMap, wxPayConfiguration.getKey());
        parameterMap.put("paySign", paySign);
        return parameterMap;
    }

    /**
     * Gets app response.
     *
     * @param res                the res
     * @param wxPayConfiguration the wx pay configuration
     * @return the app response
     * @throws Exception the exception
     */
    public static Map<String, String> getAppResponse(Map<String, String> res, WxPayConfiguration wxPayConfiguration) throws Exception {
        String prepay_id = res.get("prepay_id");
        String nonce_str = res.get("nonce_str");
        Map<String, String> parameterMap = new HashMap<>(6);
        parameterMap.put("appid", wxPayConfiguration.getAppID());
        parameterMap.put("partnerid", wxPayConfiguration.getMchID());
        parameterMap.put("prepayid", prepay_id);
        parameterMap.put("noncestr", nonce_str);
        parameterMap.put("timestamp", TimeUtil.getCurrentMs());
        parameterMap.put("package", "Sign=WXPay");
        // 生成支付签名
        String sign = WXPayUtil.generateSignature(parameterMap, wxPayConfiguration.getKey());
        parameterMap.put("sign", sign);
        return parameterMap;
    }

    /**
     * Gets refund response.
     *
     * @param res                the res
     * @param wxPayConfiguration the wx pay configuration
     * @return the refund response
     * @throws Exception the exception
     */
    public static Map<String, String> getRefundResponse(Map<String, String> res, WxPayConfiguration wxPayConfiguration) throws Exception {
        Map<String, String> parameterMap = new HashMap<>(3);
        String returnCode = res.get("return_code");
        String errCodeDes = res.get("err_code_des");
        String resultCode = res.get("result_code");
        parameterMap.put("returnCode", returnCode);
        parameterMap.put("errCodeDes", errCodeDes);
        parameterMap.put("resultCode", resultCode);
        return parameterMap;
    }
}
