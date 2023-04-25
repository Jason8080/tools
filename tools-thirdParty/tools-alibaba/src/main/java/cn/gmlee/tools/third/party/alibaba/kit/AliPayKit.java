package cn.gmlee.tools.third.party.alibaba.kit;

import cn.gmlee.tools.base.util.BoolUtil;
import cn.gmlee.tools.third.party.alibaba.config.AliPayConfiguration;
import cn.gmlee.tools.third.party.alibaba.model.req.*;
import com.alipay.api.AlipayClient;
import com.alipay.api.AlipayObject;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.*;
import com.alipay.api.request.*;
import com.alipay.api.response.*;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;

/**
 * 通用支付宝支付工具.
 *
 * @author Jas °
 * @date 2021 /3/3 (周三)
 */
public class AliPayKit {
    /**
     * 支付宝订单查询接口
     *
     * @param aliPayConfiguration
     * @param aliPayResultQueryRequest
     * @return
     * @throws Exception
     */
    public static AlipayTradeQueryResponse orderQuery(AliPayConfiguration aliPayConfiguration, AliPayResultQueryRequest aliPayResultQueryRequest) throws Exception {
        // 调用RSA签名方式
        // SDK 公共请求类，包含公共请求参数，以及封装了签名与验签，开发者无需关注签名与验签
        AlipayClient client = new DefaultAlipayClient(aliPayConfiguration.getUrl(), aliPayConfiguration.getAppId(), aliPayConfiguration.getRsaPrivateKey(),
                "json", "UTF-8", aliPayConfiguration.getAliPayPublicKey(), aliPayConfiguration.getSignType());
        // 封装支付请求
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        // 设置支付业务信息
        request.setBizModel(getAlipayTradeQueryModel(aliPayConfiguration, aliPayResultQueryRequest));
        // 设置异步通知地址
        request.setNotifyUrl(aliPayConfiguration.getNotifyUrl());
        // 设置同步地址
        request.setReturnUrl(aliPayConfiguration.getReturnUrl());
        // 调用SDK生成表单
        return client.pageExecute(request);
    }

    public static AlipayTradeFastpayRefundQueryResponse refundQuery(AliPayConfiguration aliPayConfiguration, AliRefundResultQueryRequest aliRefundResultQueryRequest) throws Exception {
        // 调用RSA签名方式
        // SDK 公共请求类，包含公共请求参数，以及封装了签名与验签，开发者无需关注签名与验签
        AlipayClient client = new DefaultAlipayClient(aliPayConfiguration.getUrl(), aliPayConfiguration.getAppId(), aliPayConfiguration.getRsaPrivateKey(),
                "json", "UTF-8", aliPayConfiguration.getAliPayPublicKey(), aliPayConfiguration.getSignType());
        // 封装支付请求
        AlipayTradeFastpayRefundQueryRequest request = new AlipayTradeFastpayRefundQueryRequest();
        // 设置支付业务信息
        request.setBizModel(getAlipayTradeFastPayRefundQueryModel(aliPayConfiguration, aliRefundResultQueryRequest));
        // 设置异步通知地址
        request.setNotifyUrl(aliPayConfiguration.getNotifyUrl());
        // 设置同步地址
        request.setReturnUrl(aliPayConfiguration.getReturnUrl());
        // 调用SDK生成表单
        return client.execute(request);
    }

    /**
     * 支付宝h5-wap支付接口
     *
     * @param aliPayConfiguration the ali pay config
     * @param aliPayH5Request     the ali pay h 5 request
     * @return the string
     * @throws Exception the exception
     */
    public static AlipayTradeWapPayResponse execute(AliPayConfiguration aliPayConfiguration, AliPayH5Request aliPayH5Request) throws Exception {
        // 调用RSA签名方式
        // SDK 公共请求类，包含公共请求参数，以及封装了签名与验签，开发者无需关注签名与验签
        AlipayClient client = new DefaultAlipayClient(aliPayConfiguration.getUrl(), aliPayConfiguration.getAppId(), aliPayConfiguration.getRsaPrivateKey(),
                "json", "UTF-8", aliPayConfiguration.getAliPayPublicKey(), aliPayConfiguration.getSignType());
        // 封装支付请求
        AlipayTradeWapPayRequest request = new AlipayTradeWapPayRequest();
        // 设置支付业务信息
        request.setBizModel(getAlipayTradeWapPayModel(aliPayConfiguration, aliPayH5Request));
        // 设置异步通知地址
        request.setNotifyUrl(aliPayConfiguration.getNotifyUrl());
        // 设置同步地址
        request.setReturnUrl(aliPayConfiguration.getReturnUrl());
        // 调用SDK生成表单
        return client.pageExecute(request);
    }


    /**
     * 支付宝native支付接口
     *
     * @param aliPayConfiguration the ali pay config
     * @param aliPayNativeRequest the ali pay native request
     * @return the alipay trade wap pay response
     * @throws Exception the exception
     */
    public static AlipayTradePagePayResponse execute(AliPayConfiguration aliPayConfiguration, AliPayNativeRequest aliPayNativeRequest) throws Exception {
        // 调用RSA签名方式
        // SDK 公共请求类，包含公共请求参数，以及封装了签名与验签，开发者无需关注签名与验签
        AlipayClient client = new DefaultAlipayClient(aliPayConfiguration.getUrl(), aliPayConfiguration.getAppId(), aliPayConfiguration.getRsaPrivateKey(),
                "json", "UTF-8", aliPayConfiguration.getAliPayPublicKey(), aliPayConfiguration.getSignType());
        // 封装支付请求
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        // 设置支付业务信息
        request.setBizModel(getAlipayTradePagePayModel(aliPayConfiguration, aliPayNativeRequest));
        // 设置异步通知地址
        request.setNotifyUrl(aliPayConfiguration.getNotifyUrl());
        // 设置同步地址
        request.setReturnUrl(aliPayConfiguration.getReturnUrl());
        // 调用SDK生成表单
        return client.pageExecute(request);
    }

    public static AlipayTradeAppPayResponse execute(AliPayConfiguration aliPayConfiguration, AliPayAppRequest aliPayAppRequest) throws Exception {
        // 调用RSA签名方式
        // SDK 公共请求类，包含公共请求参数，以及封装了签名与验签，开发者无需关注签名与验签
        AlipayClient client = new DefaultAlipayClient(aliPayConfiguration.getUrl(), aliPayConfiguration.getAppId(), aliPayConfiguration.getRsaPrivateKey(),
                "json", "UTF-8", aliPayConfiguration.getAliPayPublicKey(), aliPayConfiguration.getSignType());
        // 封装支付请求
        AlipayTradeAppPayRequest request = new AlipayTradeAppPayRequest();
        // 设置支付业务信息
        request.setBizModel(getAlipayTradeAppPayModel(aliPayConfiguration, aliPayAppRequest));
        // 设置异步通知地址
        request.setNotifyUrl(aliPayConfiguration.getNotifyUrl());
        // 设置同步地址
        request.setReturnUrl(aliPayConfiguration.getReturnUrl());
        // 调用SDK生成表单
        return client.pageExecute(request);
    }

    public static AlipayTradeRefundResponse execute(AliPayConfiguration aliPayConfiguration, AliRefundRequest aliRefundRequest) throws Exception {
        // 调用RSA签名方式
        // SDK 公共请求类，包含公共请求参数，以及封装了签名与验签，开发者无需关注签名与验签
        AlipayClient client = new DefaultAlipayClient(aliPayConfiguration.getUrl(), aliPayConfiguration.getAppId(), aliPayConfiguration.getRsaPrivateKey(),
                "json", "UTF-8", aliPayConfiguration.getAliPayPublicKey(), aliPayConfiguration.getSignType());
        // 封装支付请求
        AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();
        // 设置支付业务信息
        request.setBizModel(getAlipayTradeRefundModel(aliPayConfiguration, aliRefundRequest));
        // 设置异步通知地址
        request.setNotifyUrl(aliPayConfiguration.getNotifyUrl());
        // 设置同步地址
        request.setReturnUrl(aliPayConfiguration.getReturnUrl());
        // 调用SDK生成表单
        return client.execute(request);
    }

    private static AlipayObject getAlipayTradeQueryModel(AliPayConfiguration aliPayConfiguration, AliPayResultQueryRequest aliPayResultQueryRequest) {
        //设置请求参数
        AlipayTradeQueryModel model = new AlipayTradeQueryModel();
        //商户订单号，商户网站订单系统中唯一订单号
        model.setOutTradeNo(aliPayResultQueryRequest.getOutTradeNo());
        return model;
    }

    @SuppressWarnings("all")
    private static AlipayTradeWapPayModel getAlipayTradeWapPayModel(AliPayConfiguration aliPayConfiguration, AliPayRequest aliPayRequest) throws UnsupportedEncodingException {
        // 封装请求支付信息
        AlipayTradeWapPayModel model = new AlipayTradeWapPayModel();
        // 商户订单号，商户网站订单系统中唯一订单号，必填
        model.setOutTradeNo(aliPayRequest.getOutTradeNo());
        // 订单名称，必填
        model.setSubject(aliPayRequest.getSubject());
        // 付款金额，必填
        BigDecimal totalAmount = (new BigDecimal(aliPayRequest.getMoney()).divide(new BigDecimal(100)));
        model.setTotalAmount(totalAmount.setScale(2, RoundingMode.CEILING).toString());
        // 销售产品码 必填
        model.setProductCode(aliPayConfiguration.getProductCode());
        // 商品描述，可空
        model.setBody(aliPayRequest.getBody());
        // 超时时间 可空
        model.setTimeoutExpress(aliPayConfiguration.getTimeoutExpress().toString());
        // 自定义参数，可空
        model.setPassbackParams(URLEncoder.encode(aliPayRequest.getParams(), "UTF-8"));
        return model;
    }

    @SuppressWarnings("all")
    private static AlipayTradePagePayModel getAlipayTradePagePayModel(AliPayConfiguration aliPayConfiguration, AliPayRequest aliPayRequest) throws UnsupportedEncodingException {
        // 封装请求支付信息
        AlipayTradePagePayModel model = new AlipayTradePagePayModel();
        // 商户订单号，商户网站订单系统中唯一订单号，必填
        model.setOutTradeNo(aliPayRequest.getOutTradeNo());
        // 订单名称，必填
        model.setSubject(aliPayRequest.getSubject());
        // 付款金额，必填
        BigDecimal totalAmount = (new BigDecimal(aliPayRequest.getMoney()).divide(new BigDecimal(100)));
        model.setTotalAmount(totalAmount.setScale(2, RoundingMode.CEILING).toString());
        // 销售产品码 必填
        model.setProductCode(aliPayConfiguration.getProductCode());
        return model;
    }

    @SuppressWarnings("all")
    private static AlipayTradeAppPayModel getAlipayTradeAppPayModel(AliPayConfiguration aliPayConfiguration, AliPayRequest aliPayRequest) throws UnsupportedEncodingException {
        // 封装请求支付信息
        AlipayTradeAppPayModel model = new AlipayTradeAppPayModel();
        // 商户订单号，商户网站订单系统中唯一订单号，必填
        model.setOutTradeNo(aliPayRequest.getOutTradeNo());
        // 订单名称，必填
        model.setSubject(aliPayRequest.getSubject());
        // 付款金额，必填
        BigDecimal totalAmount = (new BigDecimal(aliPayRequest.getMoney()).divide(new BigDecimal(100)));
        model.setTotalAmount(totalAmount.setScale(2, RoundingMode.CEILING).toString());
        // 销售产品码 必填
        model.setProductCode("QUICK_MSECURITY_PAY");
        return model;
    }

    @SuppressWarnings("all")
    private static AlipayTradeRefundModel getAlipayTradeRefundModel(AliPayConfiguration aliPayConfiguration, AliRefundRequest aliRefundRequest) throws UnsupportedEncodingException {
        // 封装请求退款信息
        AlipayTradeRefundModel model = new AlipayTradeRefundModel();
        model.setOutRequestNo(aliRefundRequest.getOutRefundNo());
        model.setOutTradeNo(aliRefundRequest.getOutTradeNo());
        BigDecimal refundAmount = (new BigDecimal(aliRefundRequest.getRefundAmount()).divide(new BigDecimal(100)));
        model.setRefundAmount(refundAmount.setScale(2, RoundingMode.CEILING).toString());
        model.setRefundReason(aliRefundRequest.getRefundReason());
        return model;
    }

    private static AlipayTradeFastpayRefundQueryModel getAlipayTradeFastPayRefundQueryModel(AliPayConfiguration aliPayConfiguration, AliRefundResultQueryRequest aliRefundResultQueryRequest) {
        // 封装查询退款信息
        AlipayTradeFastpayRefundQueryModel model = new AlipayTradeFastpayRefundQueryModel();
        model.setOutTradeNo(aliRefundResultQueryRequest.getOutTradeNo());
        model.setOutRequestNo(aliRefundResultQueryRequest.getOutRefundNo());
        // 以下参数必填: 退款时没传则需要填充OutTradeNo的值
        if(BoolUtil.isEmpty(aliRefundResultQueryRequest.getOutRefundNo())){
            model.setOutRequestNo(aliRefundResultQueryRequest.getOutTradeNo());
        }
        return model;
    }
}
