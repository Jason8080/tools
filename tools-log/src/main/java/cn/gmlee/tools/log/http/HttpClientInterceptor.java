package cn.gmlee.tools.log.http;

import cn.gmlee.tools.base.mod.JsonLog;
import cn.gmlee.tools.base.util.ExceptionUtil;
import cn.gmlee.tools.base.util.StreamUtil;
import cn.gmlee.tools.base.util.TimeUtil;
import cn.gmlee.tools.base.util.WebUtil;
import cn.gmlee.tools.log.config.ApiPrintTrigger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class HttpClientInterceptor implements ClientHttpRequestInterceptor {

    @Autowired
    private ApiPrintTrigger apiPrintTrigger;

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        JsonLog log = ExceptionUtil.sandbox(() -> before(request, body));
        long start = System.currentTimeMillis();
        try {
            ClientHttpResponse response = execution.execute(request, body);
            ExceptionUtil.sandbox(() -> after(response, log, start));
            ExceptionUtil.sandbox(() -> apiPrintTrigger.log(log, null, null));
            return response;
        } catch (Throwable throwable) {
            ExceptionUtil.sandbox(() -> ex(log, throwable, start));
            ExceptionUtil.sandbox(() -> apiPrintTrigger.log(log, null, throwable));
            throw throwable;
        }
    }

    private JsonLog before(HttpRequest request, byte... body) {
        return JsonLog.log()
                .setUrl(request.getURI().toString())
                .setMethod(request.getMethod().name())
                .setPrint(request.getURI().getPath())
                .setType(-1)
                .setRequestIp(WebUtil.getCurrentIp())
                .setRequestHeaders(request.getHeaders().toSingleValueMap())
                .setRequestParams(new String(body))
                .setRequestTime(TimeUtil.getCurrentDatetime())
                .setSite(this.getClass().getName());
    }

    private void after(ClientHttpResponse response, JsonLog log, long start) throws IOException {
        log.setResponseParams(StreamUtil.toString(response.getBody()))
                .setResponseHeaders(response.getHeaders().toSingleValueMap())
                .setResponseTime(TimeUtil.getCurrentDatetime())
                .setElapsedTime(System.currentTimeMillis() - start);
    }

    private void ex(JsonLog log, Throwable throwable, long start) {
        log.setResponseParams(null)
                .setResponseHeaders(null)
                .setResponseTime(TimeUtil.getCurrentDatetime())
                .setElapsedTime(System.currentTimeMillis() - start)
                .setEx(ExceptionUtil.getOriginMsg(throwable));
    }
}
