package cn.gmlee.tools.cloud.feign;

import cn.gmlee.tools.base.util.*;
import feign.Logger;
import feign.Request;
import feign.Response;
import feign.Util;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import static feign.Util.decodeOrDefault;
import static feign.form.util.CharsetUtil.UTF_8;

/**
 * Feign日志
 *
 */
@Slf4j
public class FeignLogger extends Logger {
    private final FeignLoggerProperties properties;

    public FeignLogger(FeignLoggerProperties properties) {
        this.properties = properties;
    }

    @Override
    protected void logRequest(String configKey, Level logLevel, Request request) {
    }

    @Override
    protected Response logAndRebufferResponse(String configKey,
                                              Level logLevel,
                                              Response response,
                                              long elapsedTime) {
        int status = response.status();

        String responseBody = "";
        if (response.body() != null && !(status == 204 || status == 205)) {
            byte[] bodyData;
            try {
                bodyData = Util.toByteArray(response.body().asInputStream());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            if (bodyData.length > 0) {
                responseBody = decodeOrDefault(bodyData, UTF_8, "Binary data");
            }
            response = response.toBuilder().body(bodyData).build();
        }

        String requestBody = new String(NullUtil.get(response.request().body(), new byte[0]));

        boolean print = UrlUtil.matchOne(properties.getFeignLog().excludeUrls, response.request().url());

        // 匹配上则排除: 不打印日志
        if(!print) {

            log.info("[内网日志]\n{}\n=== 请求接口 ===: {}\n=== 请求地址 ===: {}\n=== 请求头部 ===: {}\n=== 请求内容 ===: {}\n=== 响应代码 ===: {}\n=== 响应耗时 ===: {} (ms)\n=== 响应头部 ===: {}\n=== 响应内容 ===: {}\n{}\n",
                    "-----------------------------------------------",
                    configKey,
                    String.format("%s %s", response.request().httpMethod(), response.request().url()),
                    BoolUtil.isTrue(properties.getFeignLog().printHead) ? response.request().headers() : "默认关闭",
                    BoolUtil.isEmpty(requestBody) ? "无" : CharUtil.digest(requestBody, properties.getFeignLog().maxlength),
                    status,
                    elapsedTime,
                    BoolUtil.isTrue(properties.getFeignLog().printHead) ? response.headers() : "默认关闭",
                    BoolUtil.isEmpty(responseBody) ? "无" : CharUtil.digest(responseBody, properties.getFeignLog().maxlength),
                    "-----------------------------------------------"
            );

        }

        return response;
    }

    @Override
    protected void log(String configKey, String format, Object... args) {
    }

    private static String CombineHeaders(Map<String, Collection<String>> headers) {
        StringBuilder sb = new StringBuilder();
        if (headers != null && !headers.isEmpty()) {
            sb.append("Headers:\r\n");
            for (Map.Entry<String, Collection<String>> ob : headers.entrySet()) {
                for (String val : ob.getValue()) {
                    sb.append("  ").append(ob.getKey()).append(": ").append(val).append("\r\n");
                }
            }
        }
        return sb.toString();
    }
}
