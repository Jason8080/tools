package cn.gmlee.tools.base.mod;

import cn.gmlee.tools.base.util.CharUtil;
import cn.gmlee.tools.base.util.JsonUtil;
import cn.gmlee.tools.base.util.QuickUtil;

import java.io.Serializable;

/**
 * 通用日志打印实体
 *
 * @author Jas °
 * @date 2020 /9/14 (周一)
 */
public class JsonLog implements Serializable {
    public JsonLog() {
        System.out.println("xxxxxxxxxxxxxxxx");
    }

    /**
     * Log json log.
     *
     * @return the json log
     */
    public static JsonLog log() {
        return new JsonLog();
    }

    /**
     * 请求地址
     */
    public String url;
    /**
     * 请求方法
     */
    public String method;
    /**
     * 请求说明 [ApiPrint: value(version)]
     */
    public String print;
    /**
     * 功能类型
     */
    public Integer type;
    /**
     * 响应代码
     */
    public Integer code;
    /**
     * 请求地址
     */
    public String requestIp;
    /**
     * 请求参数
     */
    public Object requestHeaders;
    /**
     * 请求参数
     */
    public Object requestParams;
    /**
     * 请求时间
     */
    public String requestTime;
    /**
     * 响应参数
     */
    public Object responseParams;
    /**
     * 响应参数
     */
    public Object responseHeaders;
    /**
     * 响应时间
     */
    public String responseTime;
    /**
     * 消耗时间
     */
    public Long elapsedTime;
    /**
     * 异常信息
     */
    public String ex;
    /**
     * 输出位置.
     */
    public String site;

    /**
     * Sets url.
     *
     * @param url the url
     * @return the url
     */
    public JsonLog setUrl(String url) {
        this.url = url;
        return this;
    }

    /**
     * Sets method.
     *
     * @param method the method
     * @return the method
     */
    public JsonLog setMethod(String method) {
        this.method = method;
        return this;
    }

    /**
     * Sets print.
     *
     * @param print the print
     * @return the print
     */
    public JsonLog setPrint(String print) {
        this.print = print;
        return this;
    }

    /**
     * Sets type.
     *
     * @param type the type
     * @return the type
     */
    public JsonLog setType(Integer type) {
        this.type = type;
        return this;
    }

    /**
     * Sets code.
     *
     * @param code the code
     * @return the code
     */
    public JsonLog setCode(Integer code) {
        this.code = code;
        return this;
    }

    /**
     * Sets request params.
     *
     * @param requestIp the request ip
     * @return the request params
     */
    public JsonLog setRequestIp(String requestIp) {
        this.requestIp = requestIp;
        return this;
    }

    /**
     * Sets request headers.
     *
     * @param requestHeaders the request headers
     * @return the request headers
     */
    public JsonLog setRequestHeaders(Object requestHeaders) {
        this.requestHeaders = requestHeaders;
        return this;
    }

    /**
     * Sets request params.
     *
     * @param requestParams the request params
     * @return the request params
     */
    public JsonLog setRequestParams(Object requestParams) {
        this.requestParams = requestParams;
        return this;
    }

    /**
     * Sets request time.
     *
     * @param requestTime the request time
     * @return the request time
     */
    public JsonLog setRequestTime(String requestTime) {
        this.requestTime = requestTime;
        return this;
    }

    /**
     * Sets response params.
     *
     * @param responseParams the response params
     * @return the response params
     */
    public JsonLog setResponseParams(Object responseParams) {
        this.responseParams = responseParams;
        return this;
    }

    /**
     * Sets response headers.
     *
     * @param responseHeaders the response headers
     * @return the response headers
     */
    public JsonLog setResponseHeaders(Object responseHeaders) {
        this.responseHeaders = responseHeaders;
        return this;
    }

    /**
     * Sets response time.
     *
     * @param responseTime the response time
     * @return the response time
     */
    public JsonLog setResponseTime(String responseTime) {
        this.responseTime = responseTime;
        return this;
    }

    /**
     * Sets elapsed time.
     *
     * @param elapsedTime the elapsed time
     * @return the response time
     */
    public JsonLog setElapsedTime(Long elapsedTime) {
        this.elapsedTime = elapsedTime;
        return this;
    }

    /**
     * Sets ex.
     *
     * @param ex the ex
     * @return the ex
     */
    public JsonLog setEx(String ex) {
        this.ex = ex;
        return this;
    }

    /**
     * Sets site.
     *
     * @param site the site
     * @return site site
     */
    public JsonLog setSite(String site) {
        this.site = site;
        return this;
    }

    /**
     * Builder string.
     *
     * @param length 打印的最大长度
     * @return the string
     */
    public String builder(int length) {
        return builder(true, false, length);
    }

    /**
     * Builder string.
     *
     * @param isSimple 是否简单化打印
     * @param isFormat 是否格式化打印
     * @param length   打印的最大长度
     * @return the string
     */
    public String builder(boolean isSimple, boolean isFormat, int length) {
        cleanSimple(isSimple, this);
        String json = JsonUtil.toJson(this, false, isSimple);
        String format = isFormat ? JsonUtil.format(json) : json;
        return CharUtil.digest(format, length);
    }

    private static void cleanSimple(boolean isSimple, JsonLog log) {
        QuickUtil.isTrue(isSimple, () -> log
                .setType(null)
                .setPrint(null)
                .setRequestIp(null)
                .setRequestHeaders(null)
                .setRequestTime(null)
                .setResponseHeaders(null)
                .setResponseTime(null)
                .setSite(null)
        );
    }

    @Override
    public String toString() {
        return "JsonLog{" +
                "url='" + url + '\'' +
                ", method='" + method + '\'' +
                ", print='" + print + '\'' +
                ", code='" + code + '\'' +
                ", requestIp='" + requestIp + '\'' +
                ", requestHeaders=" + requestHeaders +
                ", requestParams=" + requestParams +
                ", requestTime='" + requestTime + '\'' +
                ", responseParams=" + responseParams +
                ", responseHeaders=" + responseHeaders +
                ", responseTime='" + responseTime + '\'' +
                ", elapsedTime=" + elapsedTime +
                ", ex='" + ex + '\'' +
                ", site='" + site + '\'' +
                '}';
    }
}
