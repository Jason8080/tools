package cn.gmlee.tools.log.http;

import cn.gmlee.tools.base.mod.JsonLog;
import cn.gmlee.tools.base.util.ExceptionUtil;
import cn.gmlee.tools.base.util.TimeUtil;
import cn.gmlee.tools.base.util.WebUtil;
import cn.gmlee.tools.log.config.ApiPrintTrigger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import okio.Buffer;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class OkHttpInterceptor implements Interceptor {

    @Autowired
    private ApiPrintTrigger apiPrintTrigger;

    @Override
    public Response intercept(Chain chain) throws IOException {
        JsonLog log = ExceptionUtil.sandbox(() -> before(chain.request()));
        long start = System.currentTimeMillis();
        try {
            Response response = chain.proceed(chain.request());
            ExceptionUtil.sandbox(() -> after(response, log, start));
            ExceptionUtil.sandbox(() -> apiPrintTrigger.log(log, null, null));
            return response;
        } catch (Throwable throwable) {
            ExceptionUtil.sandbox(() -> ex(log, throwable, start));
            ExceptionUtil.sandbox(() -> apiPrintTrigger.log(log, null, throwable));
            throw throwable;
        }
    }

    private JsonLog before(Request request) {
        HttpUrl url = request.url();
        RequestBody body = request.body();
        Buffer bodySb = new Buffer();
        ExceptionUtil.sandbox(() -> body.writeTo(bodySb));
        return JsonLog.log()
                .setUrl(url.url().toString())
                .setMethod(request.method())
                .setPrint(url.uri().toString())
                .setType(-1)
                .setRequestIp(WebUtil.getCurrentIp())
                .setRequestHeaders(request.headers().toMultimap())
                .setRequestParams(bodySb.toString())
                .setRequestTime(TimeUtil.getCurrentDatetime())
                .setSite(this.getClass().getName());
    }

    private void after(Response response, JsonLog log, long start) throws IOException {
        log.setResponseParams(response.body().string())
                .setResponseHeaders(response.headers().toMultimap())
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
