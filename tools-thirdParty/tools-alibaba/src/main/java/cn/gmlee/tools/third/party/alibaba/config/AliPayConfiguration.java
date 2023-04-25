package cn.gmlee.tools.third.party.alibaba.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * 支付宝配置
 *
 * @author Jas°
 * @date 2021/3/4 (周四)
 */
@Configuration
@PropertySource(value = {"classpath:ali-pay.properties"}, ignoreResourceNotFound = true)
public class AliPayConfiguration {
    /**
     * 商户应用编号
     */
    @Value("${tools.ali.pay.appId:#{null}}")
    private String appId;
    /**
     * 应用私钥
     */
    @Value("${tools.ali.pay.rsaPrivateKey:#{null}}")
    private String rsaPrivateKey;
    /**
     * 应用公钥
     */
    @Value("${tools.ali.pay.rsaPublicKey:#{null}}")
    private String rsaPublicKey;
    /**
     * 应用公钥
     */
    @Value("${tools.ali.pay.aliPayPublicKey:#{null}}")
    private String aliPayPublicKey;
    /**
     * 订单通知接口
     * 服务器异步通知页面路径 需http://或者https://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
     */
    @Value("${tools.ali.pay.notifyUrl:}")
    private String notifyUrl;
    /**
     * 订单跳转接口
     * 页面跳转同步通知页面路径 需http://或者https://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问 商户可以自定义同步跳转地址
     */
    @Value("${tools.ali.pay.returnUrl:}")
    private String returnUrl;
    /**
     * 支付宝网关: 请求网关地址
     */
    @Value("${tools.ali.pay.url:https://openapi.alipay.com/gateway.do}")
    private String url;

    /**
     * 签名算法: RSA2
     */
    @Value("${tools.ali.pay.signType:RSA2}")
    private String signType;
    /**
     * 超时时间
     */
    @Value("${tools.ali.pay.timeoutExpress:2000}")
    private Long timeoutExpress;
    /**
     * 产片销售编码: 仅支持默认值
     */
    @Value("${tools.ali.pay.productCode:FAST_INSTANT_TRADE_PAY}")
    private String productCode;

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getRsaPrivateKey() {
        return rsaPrivateKey;
    }

    public void setRsaPrivateKey(String rsaPrivateKey) {
        this.rsaPrivateKey = rsaPrivateKey;
    }

    public String getRsaPublicKey() {
        return rsaPublicKey;
    }

    public void setRsaPublicKey(String rsaPublicKey) {
        this.rsaPublicKey = rsaPublicKey;
    }

    public String getAliPayPublicKey() {
        return aliPayPublicKey;
    }

    public void setAliPayPublicKey(String aliPayPublicKey) {
        this.aliPayPublicKey = aliPayPublicKey;
    }

    public String getNotifyUrl() {
        return notifyUrl;
    }

    public void setNotifyUrl(String notifyUrl) {
        this.notifyUrl = notifyUrl;
    }

    public String getReturnUrl() {
        return returnUrl;
    }

    public void setReturnUrl(String returnUrl) {
        this.returnUrl = returnUrl;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getSignType() {
        return signType;
    }

    public void setSignType(String signType) {
        this.signType = signType;
    }

    public Long getTimeoutExpress() {
        return timeoutExpress;
    }

    public void setTimeoutExpress(Long timeoutExpress) {
        this.timeoutExpress = timeoutExpress;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    @Override
    public String toString() {
        return "AliPayConfiguration{" +
                "appId='" + appId + '\'' +
                ", rsaPrivateKey='" + rsaPrivateKey + '\'' +
                ", rsaPublicKey='" + rsaPublicKey + '\'' +
                ", aliPayPublicKey='" + aliPayPublicKey + '\'' +
                ", notifyUrl='" + notifyUrl + '\'' +
                ", returnUrl='" + returnUrl + '\'' +
                ", url='" + url + '\'' +
                ", signType='" + signType + '\'' +
                ", timeoutExpress=" + timeoutExpress +
                ", productCode='" + productCode + '\'' +
                '}';
    }
}
