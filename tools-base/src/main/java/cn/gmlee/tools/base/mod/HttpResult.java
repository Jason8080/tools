package cn.gmlee.tools.base.mod;

import cn.gmlee.tools.base.enums.XCode;
import cn.gmlee.tools.base.ex.SkillException;
import cn.gmlee.tools.base.util.AssertUtil;
import cn.gmlee.tools.base.util.ExceptionUtil;
import cn.gmlee.tools.base.util.JsonUtil;
import cn.gmlee.tools.base.util.QuickUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import lombok.Data;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

/**
 * The type Http result.
 *
 * @author Jas °
 */
@Data
public class HttpResult implements Serializable {

    private static final Logger logger = LoggerFactory.getLogger(HttpResult.class);

    /**
     * The constant CHARSET.
     */
    public static final String CHARSET = "charset";

    private String url;
    private String method;
    private Map<String, String> reqHeaders;
    private byte[] body;
    private ContentType contentType;
    private Header[] resHeaders;
    private Integer status;
    private byte[] result = {};
    private String err;
    private long innerMillis; // 内部耗时
    private long outerMillis; // 外部耗时
    private long millis; // 总耗时

    /**
     * 获取请求头
     *
     * @param name the name
     * @return header header
     */
    public String getHeader(String name) {
        Header header = header(name);
        if (Objects.nonNull(header)) {
            return header.getValue();
        }
        return null;
    }

    /**
     * 判断请求是否得到正确响应.
     *
     * @return the boolean
     */
    public boolean isOk() {
        return XCode.HTTP_OK.code == status;
    }

    /**
     * 判断是否是资源丢失异常
     *
     * @return boolean boolean
     */
    public boolean isMissingEx() {
        return XCode.HTTP_MISSING.code == status;
    }

    /**
     * 判断是否是系统离线异常
     * <p>
     * 系统升级或已下线
     * </p>
     *
     * @return boolean boolean
     */
    public boolean isOfflineEx() {
        return XCode.HTTP_OFFLINE.code == status;
    }

    /**
     * 是否json响应
     *
     * @return boolean boolean
     */
    public boolean isJson() {
        String requestContentType;
        if (contentType != null) {
            requestContentType = contentType.getMimeType();
        } else {
            requestContentType = getHeader(HTTP.CONTENT_TYPE);
        }
        return requestContentType.contains("json");
    }

    /**
     * 获取
     *
     * @return
     */
    private Header header(String name) {
        if (Objects.nonNull(name)) {
            for (Header header : resHeaders) {
                if (name.equalsIgnoreCase(header.getName())) {
                    return header;
                }
            }
        }
        return null;
    }


    /**
     * Gets instance.
     *
     * @param request  the request
     * @param response the response
     * @return the http result
     */
    public static HttpResult getInstance(HttpRequestBase request, HttpResponse response) throws IOException {
        HttpResult result = new HttpResult();
        // 请求内容
        result.setUrl(request.getURI().toString());
        result.setMethod(result.getMethod());
        result.setContentType(result.getContentType());
        result.setReqHeaders(result.getReqHeaders());
        // 响应内容
        result.setStatus(response.getStatusLine().getStatusCode());
        result.setResHeaders(response.getAllHeaders());
        HttpEntity entity = response.getEntity();
        result.setContentType(ContentType.get(entity));
        try {
            QuickUtil.notNull(entity, x -> result.setResult(EntityUtils.toByteArray(x)));
        } catch (Exception e) {
            logger.error("响应体设置异常", e);
        } finally {
            EntityUtils.consume(entity);
        }
        return result;
    }

    /**
     * Gets instance.
     *
     * @param request   the request
     * @param throwable the throwable
     * @return the http result
     */
    public static HttpResult getInstance(HttpRequestBase request, Throwable throwable) {
        HttpResult result = new HttpResult();
        // 请求内容
        result.setUrl(request.getURI().toString());
        result.setMethod(result.getMethod());
        result.setContentType(result.getContentType());
        result.setReqHeaders(result.getReqHeaders());
        // 响应内容
        result.setErr(throwable.getMessage());
        result.setResHeaders(new Header[0]);
        result.setContentType(ContentType.APPLICATION_JSON);
        if (throwable instanceof SkillException) {
            result.setStatus(((SkillException) throwable).getCode());
        } else {
            result.setStatus(500);
        }
        result.setResult(ExceptionUtil.getOriginMsg(throwable).getBytes());
        return result;
    }

    /**
     * 获取相应内容 (字符串).
     * <p>
     * 请使用: {@link #byteResponseBody2String()}方法
     * </p>
     *
     * @return result string
     */
    @Deprecated
    public String getResponseBody() {
        return new String(this.getResult());
    }

    /**
     * 获取相应内容 (字符串).
     *
     * @param length the length
     * @return the string
     */
    public String byteResponseBody2String(int length) {
        byte[] result = this.getResult();
        int len = result.length < length ? result.length : length;
        return new String(Arrays.copyOf(result, len));
    }

    /**
     * 获取相应内容 (字符串).
     *
     * @return the string
     */
    public String byteResponseBody2String() {
        return new String(this.getResult());
    }

    /**
     * Byte response body 2 string string.
     *
     * @param charset the charset
     * @return the string
     */
    public String byteResponseBody2String(String charset) {
        try {
            return new String(this.getResult(), charset);
        } catch (UnsupportedEncodingException e) {
            return ExceptionUtil.cast(e);
        }
    }

    /**
     * Byte response body 2 string string.
     *
     * @param length  the length
     * @param charset the charset
     * @return the string
     */
    public String byteResponseBody2String(int length, String charset) {
        byte[] result = this.getResult();
        int len = result.length < length ? result.length : length;
        try {
            return new String(Arrays.copyOf(result, len), charset);
        } catch (UnsupportedEncodingException e) {
            return ExceptionUtil.cast(e);
        }
    }

    /**
     * 将响应体JSON数据转换成对象
     *
     * @param <T>   the type parameter
     * @param clazz the clazz
     * @return t t
     */
    public <T> T jsonResponseBody2bean(Class<T> clazz) {
        Integer status = this.getStatus();
        String responseBody = this.byteResponseBody2String();
        AssertUtil.eq(XCode.HTTP_OK.code, status, String.format("%s: %s: ", url, status, responseBody));
        return JsonUtil.toBean(responseBody, clazz);
    }

    /**
     * Json response body 2 bean t.
     *
     * @param <T>      the type parameter
     * @param javaType the java type
     * @return the t
     */
    public <T> T jsonResponseBody2bean(JavaType javaType) {
        Integer status = this.getStatus();
        String responseBody = this.byteResponseBody2String();
        AssertUtil.eq(XCode.HTTP_OK.code, status, String.format("%s: %s: ", url, status, responseBody));
        return JsonUtil.toBean(responseBody, javaType);
    }

    /**
     * Json response body 2 bean t.
     *
     * @param <T>       the type parameter
     * @param reference the reference
     * @return the t
     */
    public <T> T jsonResponseBody2bean(TypeReference<T> reference) {
        Integer status = this.getStatus();
        String responseBody = this.byteResponseBody2String();
        AssertUtil.eq(XCode.HTTP_OK.code, status, String.format("%s: %s: %s", url, status, responseBody));
        return JsonUtil.toBean(responseBody, reference);
    }
}
