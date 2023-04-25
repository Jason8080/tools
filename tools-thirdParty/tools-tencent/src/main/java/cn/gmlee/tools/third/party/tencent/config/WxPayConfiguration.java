package cn.gmlee.tools.third.party.tencent.config;


import com.github.wxpay.sdk.WXPayConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * The type H 5 pay configuration.
 *
 * @author Jas °
 */
@Configuration
@PropertySource(value = {"classpath:wx-pay.properties"}, ignoreResourceNotFound = true)
public class WxPayConfiguration implements WXPayConfig {

    @Value("${tools.tencent.pay.appId:#{null}}")
    private String appId;
    @Value("${tools.tencent.pay.mchId:#{null}}")
    private String mchId;
    @Value("${tools.tencent.pay.key:#{null}}")
    private String key;
    @Value("${tools.tencent.pay.cert:cert/apiclient_cert.p12}")
    private String cert;
    @Value("${tools.tencent.pay.httpConnectTimeoutMs:2000}")
    private Integer httpConnectTimeoutMs;
    @Value("${tools.tencent.pay.httpReadTimeoutMs:3000}")
    private Integer httpReadTimeoutMs;
    @Value("${tools.tencent.pay.currency:CNY}")
    private String currency;
    @Value("${tools.tencent.pay.returnUrl:}")
    private String returnUrl;
    @Value("${tools.tencent.pay.notifyUrl:}")
    private String notifyUrl;
    @Value("${tools.tencent.pay.useSandbox:true}")
    private Boolean useSandbox;

    @Override
    public InputStream getCertStream() {
        try {
            return new FileInputStream(cert);
        } catch (FileNotFoundException e) {
            return new ByteArrayInputStream(new byte[0]);
        }
    }

    @Override
    public String getAppID() {
        return appId;
    }

    @Override
    public String getMchID() {
        return mchId;
    }

    /**
     * Sets app id.
     *
     * @param appId the app id
     */
    public void setAppId(String appId) {
        this.appId = appId;
    }

    /**
     * Sets mch id.
     *
     * @param mchId the mch id
     */
    public void setMchId(String mchId) {
        this.mchId = mchId;
    }

    @Override
    public String getKey() {
        return key;
    }

    /**
     * Sets key.
     *
     * @param key the key
     */
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * Gets cert.
     *
     * @return the cert
     */
    public String getCert() {
        return cert;
    }

    /**
     * Sets cert.
     *
     * @param cert the cert
     */
    public void setCert(String cert) {
        this.cert = cert;
    }

    @Override
    public int getHttpConnectTimeoutMs() {
        return httpConnectTimeoutMs;
    }

    /**
     * Sets http connect timeout ms.
     *
     * @param httpConnectTimeoutMs the http connect timeout ms
     */
    public void setHttpConnectTimeoutMs(Integer httpConnectTimeoutMs) {
        this.httpConnectTimeoutMs = httpConnectTimeoutMs;
    }

    @Override
    public int getHttpReadTimeoutMs() {
        return httpReadTimeoutMs;
    }

    /**
     * Sets http read timeout ms.
     *
     * @param httpReadTimeoutMs the http read timeout ms
     */
    public void setHttpReadTimeoutMs(Integer httpReadTimeoutMs) {
        this.httpReadTimeoutMs = httpReadTimeoutMs;
    }

    /**
     * Gets currency.
     *
     * @return the currency
     */
    public String getCurrency() {
        return currency;
    }

    /**
     * Sets currency.
     *
     * @param currency the currency
     */
    public void setCurrency(String currency) {
        this.currency = currency;
    }

    /**
     * Gets return url.
     *
     * @return the return url
     */
    public String getReturnUrl() {
        return returnUrl;
    }

    /**
     * Sets return url.
     *
     * @param returnUrl the return url
     */
    public void setReturnUrl(String returnUrl) {
        this.returnUrl = returnUrl;
    }

    /**
     * Gets notify url.
     *
     * @return the notify url
     */
    public String getNotifyUrl() {
        return notifyUrl;
    }

    /**
     * Sets notify url.
     *
     * @param notifyUrl the notify url
     */
    public void setNotifyUrl(String notifyUrl) {
        this.notifyUrl = notifyUrl;
    }

    public Boolean getUseSandbox() {
        return useSandbox;
    }

    public void setUseSandbox(Boolean useSandbox) {
        this.useSandbox = useSandbox;
    }

    @Override
    public String toString() {
        return "WxPayConfiguration{" +
                "appId='" + appId + '\'' +
                ", mchId='" + mchId + '\'' +
                ", key='" + key + '\'' +
                ", cert='" + cert + '\'' +
                ", httpConnectTimeoutMs=" + httpConnectTimeoutMs +
                ", httpReadTimeoutMs=" + httpReadTimeoutMs +
                ", currency='" + currency + '\'' +
                ", returnUrl='" + returnUrl + '\'' +
                ", notifyUrl='" + notifyUrl + '\'' +
                ", useSandbox=" + useSandbox +
                '}';
    }
}
