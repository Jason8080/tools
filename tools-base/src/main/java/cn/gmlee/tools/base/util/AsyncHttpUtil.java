package cn.gmlee.tools.base.util;

import cn.gmlee.tools.base.enums.Function;
import cn.gmlee.tools.base.enums.XCode;
import cn.gmlee.tools.base.ex.SkillException;
import cn.gmlee.tools.base.mod.HttpResult;
import cn.gmlee.tools.base.mod.Kv;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.nio.conn.NoopIOSessionStrategy;
import org.apache.http.nio.conn.SchemeIOSessionStrategy;
import org.apache.http.nio.conn.ssl.SSLIOSessionStrategy;
import org.apache.http.nio.reactor.IOReactorException;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.ssl.TrustStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.servlet.http.Cookie;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

/**
 * 异步Http请求工具
 *
 * @author Jas °
 * @date 2021 /3/25 (周四)
 */
public class AsyncHttpUtil {
    /**
     * The constant logger.
     */
    protected static final Logger logger = LoggerFactory.getLogger(AsyncHttpUtil.class);

    /**
     * 调用异步GET请求.
     *
     * @param url     the url
     * @param run     the run
     * @param headers the headers
     */
    public static void get(String url, Function.P2v<HttpResult> run, Kv<String, String>... headers) {
        // 创建GET请求
        HttpGet get = new HttpGet(url);
        // 加载配置
        get.setConfig(HttpUtil.CONFIG);
        // 添加请求头
        HttpUtil.addHeader(get, headers);
        // 发起异步请求
        execute(get, getFutureCallback(get, run));
    }

    /**
     * 调用Post请求.
     *
     * @param url     the url
     * @param run     the run
     * @param headers the headers
     */
    public static void post(String url, Function.P2v<HttpResult> run, Kv<String, String>... headers) {
        // 创建POST请求
        HttpPost post = new HttpPost(url);
        // 加载配置
        post.setConfig(HttpUtil.CONFIG);
        // 设置请求头
        HttpUtil.addHeader(post, headers);
        // 发起异步请求
        execute(post, getFutureCallback(post, run));
    }

    /**
     * 调用Post请求 (有请求体).
     *
     * @param url     the url
     * @param params  the params
     * @param run     the run
     * @param headers the headers
     */
    public static void post(String url, Object params, Function.P2v<HttpResult> run, Kv<String, String>... headers) {
        // 创建POST请求
        HttpPost post = new HttpPost(url);
        // 加载配置
        post.setConfig(HttpUtil.CONFIG);
        // 设置请求头
        HttpUtil.addHeader(post, headers);
        // 设置请求内容
        HttpUtil.setEntity(post, params);
        // 发起异步请求
        execute(post, getFutureCallback(post, run));
    }

    /**
     * 发起异步请求
     *
     * @param request
     */
    private static void execute(HttpRequestBase request, FutureCallback<HttpResponse> callback, Cookie... cookies) {
        //创建请求工具
        CloseableHttpAsyncClient client = getAsyncClient(request, cookies);
        client.start();
        client.execute(request, callback);
    }

    /**
     * Gets async client.
     *
     * @param req     the req
     * @param cookies the cookies
     * @return the async client
     */
    public static CloseableHttpAsyncClient getAsyncClient(HttpRequestBase req, Cookie... cookies) {
        HttpAsyncClientBuilder custom = HttpAsyncClients.custom();
        String url = req.getURI().toString();
        if (!BoolUtil.isNull(cookies)) {
            custom.setDefaultCookieStore(HttpUtil.getStore(req.getURI().getHost(), cookies));
        }
        return custom
                .setConnectionManager(getPoolingNHttpClientConnectionManager())
                .setKeepAliveStrategy(HttpUtil.getKeepAliveStrategy())
                .setDefaultRequestConfig(HttpUtil.CONFIG)
                .build();
    }

    private static PoolingNHttpClientConnectionManager getPoolingNHttpClientConnectionManager() {
        try {
            final PoolingNHttpClientConnectionManager connectionManager =
                    new PoolingNHttpClientConnectionManager(new DefaultConnectingIOReactor
                            (IOReactorConfig.DEFAULT), getSSLRegistryAsync());
            return connectionManager;
        } catch (IOReactorException e) {
            return null;
        }
    }

    private static Registry<SchemeIOSessionStrategy> getSSLRegistryAsync() {
        return RegistryBuilder.<SchemeIOSessionStrategy>create()
                .register("http", NoopIOSessionStrategy.INSTANCE)
                .register("https", new SSLIOSessionStrategy(getSSLContext(), NoopHostnameVerifier.INSTANCE))
                .build();
    }

    private static SSLContext getSSLContext() {
        final TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType)
                -> true;
        try {
            final SSLContext sslContext = SSLContexts.custom()
                    .loadTrustMaterial(null, acceptingTrustStrategy)
                    .build();
            sslContext.getServerSessionContext().setSessionCacheSize(1000);
            return sslContext;
        } catch (NoSuchAlgorithmException | KeyStoreException | KeyManagementException e) {
            logger.debug("SSL路由信任失败!", e);
            return null;
        }
    }

    private static FutureCallback<HttpResponse> getFutureCallback(HttpRequestBase request, Function.P2v<HttpResult> run) {
        return new FutureCallback<HttpResponse>() {
            @Override
            public void completed(HttpResponse result) {
                try {
                    run.run(HttpResult.getInstance(request, result));
                } catch (Throwable throwable) {
                    logger.error(String.format("异步响应处理失败"), throwable);
                }
            }

            @Override
            public void failed(Exception ex) {
                try {
                    run.run(HttpResult.getInstance(request, ex));
                } catch (Throwable throwable) {
                    logger.error(String.format("异步响应处理失败"), throwable);
                }
            }

            @Override
            public void cancelled() {
                try {
                    run.run(HttpResult.getInstance(request, new SkillException(XCode.REQUEST_CANCELLED.code, "异步请求被取消")));
                } catch (Throwable throwable) {
                    logger.error(String.format("异步响应处理失败"), throwable);
                }
            }
        };
    }
}
