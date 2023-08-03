package cn.gmlee.tools.base.util;

import cn.gmlee.tools.base.enums.XCode;
import cn.gmlee.tools.base.mod.HttpResult;
import cn.gmlee.tools.base.mod.Kv;
import lombok.Setter;
import org.apache.http.*;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.methods.*;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.Args;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.Cookie;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.*;

/**
 * 模拟Http请求工具类
 *
 * @author Jas °
 */
public class HttpUtil {
    /**
     * The constant logger.
     */
    protected static final Logger logger = LoggerFactory.getLogger(HttpUtil.class);

    /**
     * The constant HTTPS.
     */
    public static final String HTTPS = "https";
    /**
     * The constant HTTP_.
     */
    public static final String HTTP_ = "http";


    /**
     * The constant CONTENT_TYPE.
     */
    public static final String CONTENT_TYPE = "Content-Type";
    /**
     * The constant JSON_HEADER.
     */
    public static final String JSON_HEADER = "application/json";
    /**
     * The constant DATA_HEADER.
     */
    public static final String DATA_HEADER = "multipart/form-data";
    /**
     * The constant FORM_HEADER.
     */
    public static final String FORM_HEADER = "application/x-www-form-urlencoded";

    /**
     * 协议版本.
     */
    private static HttpVersion version;
    /**
     * 客户端自定义.
     */
    public static CloseableHttpClient closeableHttpClient;
    /**
     * 请求拦截器.
     */
    public static final List<HttpRequestInterceptor> reqInterceptors = new ArrayList();
    /**
     * 响应拦截器.
     */
    public static final List<HttpResponseInterceptor> resInterceptors = new ArrayList();

    /**
     * 重置Http协议版本.
     *
     * @param version the version
     */
    public static void setVersion(String version) {
        if (BoolUtil.notEmpty(version)) {
            String[] split = version.split("\\.");
            AssertUtil.eq(split.length, 2, String.format("Http util protocol version configure error"));
            int major = Integer.parseInt(split[0]);
            int minor = Integer.parseInt(split[1]);
            HttpUtil.version = new HttpVersion(major, minor);
        }
    }

    /**
     * 最大连接数.
     */
    public static int maxTotal = 1000;
    /**
     * 单主机(host)最大连接数
     */
    public static int maxPerRoute = 100;
    /**
     * 最大空闲时间(超时后重新连接)
     */
    public static int validateAfterInactivity = 10000;

    /**
     * 允许业务自定义配置.
     */
    public static RequestConfig CONFIG = RequestConfig.custom()
            // 连接的超时时间
            .setConnectTimeout(5000)
            // 从连接池获取连接的超时时间
            .setConnectionRequestTimeout(5000)
            // 请求数据的超时时间 (一般是业务处理等待时间)
            .setSocketTimeout(5000)
            // 设置Cookies策略
            .setCookieSpec(CookieSpecs.STANDARD_STRICT)
            .build();

    /**
     * The constant SSL_CONTEXT.
     */
    protected static final SSLConnectionSocketFactory SSL_CONTEXT = ExceptionUtil.suppress(() -> SSLConnectionSocketFactory.getSocketFactory());


    /**
     * Http预检工具.
     *
     * @param url     the url
     * @param methods the methods
     * @return t boolean
     */
    public static boolean options(String url, String... methods) {
        try {
            HttpRequestBase options = new HttpOptions(url);
            CloseableHttpClient client = getClient(options);
            CloseableHttpResponse response = client.execute(options);
            int status = response.getStatusLine().getStatusCode();
            if (status == XCode.HTTP_OK.code) {
                Header[] allHeaders = response.getAllHeaders();
                if (methods.length > 0) {
                    for (String method : methods) {
                        for (int i = 0; i < allHeaders.length; i++) {
                            if ("Allow".equalsIgnoreCase(allHeaders[i].getName())) {
                                String values = allHeaders[i].getValue();
                                boolean contains = values.contains(method);
                                if (contains) {
                                    return true;
                                }
                            }
                        }
                    }
                    return false;
                }
                return true;
            }
        } catch (IOException e) {
            logger.debug("请求预检不支持: {}:{}", Arrays.asList(methods), url);
        }
        return false;
    }

    /**
     * 调用GET请求(application/json).
     *
     * @param url     the url
     * @param headers the headers
     * @return t t
     */
    public static HttpResult get(String url, Kv<String, String>... headers) {
        // 创建GET请求
        HttpGet request = new HttpGet(url);
        QuickUtil.notNull(version, x -> request.setProtocolVersion(version));
        // 加载配置
        request.setConfig(CONFIG);
        // 设置请求头
        addHeader(request, headers);
        // 封装结果
        return execute(request);
    }

    private static class HttpGetWithEntity extends HttpEntityEnclosingRequestBase {
        /**
         * Instantiates a new Http get with entity.
         *
         * @param url the url
         */
        public HttpGetWithEntity(String url) {
            super.setURI(URI.create(url));
        }

        @Override
        public String getMethod() {
            return "GET";
        }
    }

    /**
     * Get http result.
     *
     * @param url     the url
     * @param params  the params
     * @param headers the headers
     * @return the http result
     */
    public static HttpResult get(String url, Object params, Kv<String, String>... headers) {
        // 创建GET请求
        HttpGetWithEntity request = new HttpGetWithEntity(url);
        QuickUtil.notNull(version, x -> request.setProtocolVersion(version));
        // 加载配置
        request.setConfig(CONFIG);
        // 设置请求头
        addHeader(request, headers);
        // 设置请求内容
        setEntity(request, params);
        // 封装结果
        return execute(request);
    }

    /**
     * Get http result.
     *
     * @param url     the url
     * @param out     the out
     * @param params  the params
     * @param headers the headers
     * @return the http result
     */
    public static void get(String url, OutputStream out, Object params, Kv<String, String>... headers) {
        // 创建GET请求
        HttpGetWithEntity request = new HttpGetWithEntity(url);
        QuickUtil.notNull(version, x -> request.setProtocolVersion(version));
        // 加载配置
        request.setConfig(CONFIG);
        // 设置请求头
        addHeader(request, headers);
        // 设置请求内容
        setEntity(request, params);
        // 写出到指定位置
        HttpResult httpResult = execute(request);
        try {
            out.write(httpResult.getResult());
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get.
     *
     * @param url     the url
     * @param out     the out
     * @param headers the headers
     */
    public static void get(String url, OutputStream out, Kv<String, String>... headers) {
        // 创建GET请求
        HttpGet request = new HttpGet(url);
        QuickUtil.notNull(version, x -> request.setProtocolVersion(version));
        // 加载配置
        request.setConfig(CONFIG);
        // 设置请求头
        addHeader(request, headers);
        // 写出到指定位置
        HttpResult httpResult = execute(request);
        try {
            out.write(httpResult.getResult());
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 调用POST请求 (无参).
     *
     * @param url     the url
     * @param headers the headers
     * @return the http result
     */
    public static HttpResult post(String url, Kv<String, String>... headers) {
        // 创建POST请求
        HttpPost request = new HttpPost(url);
        QuickUtil.notNull(version, x -> request.setProtocolVersion(version));
        // 加载配置
        request.setConfig(CONFIG);
        // 设置请求头
        addHeader(request, headers);
        // 封装结果
        return execute(request);
    }

    /**
     * 调用POST请求(application/json).
     *
     * @param url     the url
     * @param params  the params
     * @param headers the headers
     * @return t t
     */
    public static HttpResult post(String url, Object params, Kv<String, String>... headers) {
        // 创建POST请求
        HttpPost request = new HttpPost(url);
        QuickUtil.notNull(version, x -> request.setProtocolVersion(version));
        // 加载配置
        request.setConfig(CONFIG);
        // 设置请求头
        addHeader(request, headers);
        // 设置请求内容
        setEntity(request, params);
        // 封装结果
        return execute(request);
    }

    /**
     * 调用POST请求(application/json) -> 无参.
     *
     * @param url     the url
     * @param out     the out
     * @param headers the headers
     */
    public static void post(String url, OutputStream out, Kv<String, String>... headers) {
        // 创建POST请求
        HttpPost request = new HttpPost(url);
        QuickUtil.notNull(version, x -> request.setProtocolVersion(version));
        // 加载配置
        request.setConfig(CONFIG);
        // 设置请求头
        addHeader(request, headers);
        // 写出到指定位置
        HttpResult httpResult = execute(request);
        try {
            out.write(httpResult.getResult());
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 调用POST请求(application/json).
     *
     * @param url     the url
     * @param params  the params
     * @param out     the out
     * @param headers the headers
     */
    public static void post(String url, Object params, OutputStream out, Kv<String, String>... headers) {
        // 创建POST请求
        HttpPost request = new HttpPost(url);
        QuickUtil.notNull(version, x -> request.setProtocolVersion(version));
        // 加载配置
        request.setConfig(CONFIG);
        // 设置请求头
        addHeader(request, headers);
        // 设置请求内容
        setEntity(request, params);
        // 写出到指定位置
        HttpResult httpResult = execute(request);
        try {
            out.write(httpResult.getResult());
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 添加请求头
     *
     * @param http the http
     * @param kvs  the kvs
     */
    public static void addHeader(HttpRequestBase http, Kv<String, String>... kvs) {
        // 默认json请求
        if ("POST".equalsIgnoreCase(http.getMethod())) {
            http.setHeader(CONTENT_TYPE, JSON_HEADER);
        }
        if (kvs != null && kvs.length > 0) {
            for (Kv<String, String> kv : kvs) {
                String key = kv.getKey();
                if (key == null || HTTP.CONTENT_LEN.equalsIgnoreCase(key)) {
                    continue;
                }
                http.setHeader(key, kv.getVal());
            }
        }
    }

    /**
     * 执行并封装响应实体(application/json).
     *
     * @param request the request
     * @param cookies the cookies
     * @return http result
     */
    public static HttpResult execute(HttpRequestBase request, Cookie... cookies) {
        //创建请求工具
        CloseableHttpClient client = getClient(request, cookies);
        long start = System.currentTimeMillis();
        CloseableHttpResponse response = null;
        try {
            response = client.execute(request);
            HttpResult httpResult = HttpResult.getInstance(request, response);
            // 统计耗时
            httpResult.setInnerMillis(System.currentTimeMillis() - start);
            return httpResult;
        } catch (Exception e) {
            HttpResult httpResult = HttpResult.getInstance(request, e);
            // 统计耗时
            httpResult.setInnerMillis(System.currentTimeMillis() - start);
            return httpResult;
        } finally {
            //关闭请求通道
            CloseableHttpResponse finalResponse = response;
            ExceptionUtil.sandbox(() -> finalResponse.close(), false);
            ExceptionUtil.sandbox(() -> client.close());
        }
    }

    /**
     * Sets entity.
     *
     * @param post   the post
     * @param params the bytes
     */
    protected static void setEntity(HttpEntityEnclosingRequestBase post, Object params) {
        //获取请求体流
        HttpEntity requestEntity = getEntity(params, post);
        //设置请求内容
        post.setEntity(requestEntity);
    }

    private static HttpEntity getEntity(Object params, HttpRequestBase post) {
        EntityBuilder build = EntityBuilder.create();
        Header header = post.getLastHeader(CONTENT_TYPE);
        // 必须保留(调用方有自己序列化的情况)
        if (params instanceof byte[]) {
            build.setBinary((byte[]) params);
            return build.build();
        }
        if (params instanceof String) {
            build.setBinary(((String) params).getBytes());
            return build.build();
        }
        if (header == null) {
            return build.build();
        }
        if (BoolUtil.contain(header.getValue(), JSON_HEADER)) {
            byte[] bytes = JsonUtil.toBytes(params);
            build.setBinary(bytes);
            return build.build();
        }
        if (BoolUtil.contain(header.getValue(), FORM_HEADER)) {
            setBody(build, params);
            return build.build();
        }
        if (BoolUtil.contain(header.getValue(), DATA_HEADER)) {
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            setBody(builder, params);
            HttpEntity httpEntity = builder.build();
            post.setHeader(httpEntity.getContentType());
            return httpEntity;
        }
        return build.build();
    }

    private static void setBody(EntityBuilder builder, Object params) {
        Map map;
        if (params instanceof Map) {
            map = (Map) params;
        } else {
            map = ClassUtil.generateMap(params);
        }
        List<NameValuePair> pairs = new ArrayList(map.size());
        map.forEach((k, v) -> {
            pairs.add(getNameValuePair(k, v));
        });
        builder.setParameters(pairs);
    }


    private static NameValuePair getNameValuePair(Object k, Object v) {
        return new NameValuePair() {
            @Override
            public String getName() {
                return ExceptionUtil.sandbox(() -> k.toString());
            }

            @Override
            public String getValue() {
                return ExceptionUtil.sandbox(() -> v.toString());
            }
        };
    }

    private static void setBody(MultipartEntityBuilder builder, Object params) {
        Map map;
        if (params instanceof Map) {
            map = (Map) params;
        } else {
            map = ClassUtil.generateMap(params);
        }
        map.forEach((k, v) -> {
            // 没有名称的内容无法发送
            if (BoolUtil.notNull(k)) {
                // 设置字节数组参数
                if (v instanceof byte[]) {
                    builder.addBinaryBody(k.toString(), (byte[]) v);
                }
                // 设置基本数据类型的参数
                if (BoolUtil.isBaseClass(v, String.class)) {
                    QuickUtil.isNull(v, () -> builder.addTextBody(k.toString(), null));
                    QuickUtil.notNull(v, x -> builder.addTextBody(k.toString(), v.toString()));
                }
                // 设置文件类型参数
                if (v instanceof File) {
                    builder.addBinaryBody((String) k, (File) v);
                }
            }
        });
    }

    /**
     * Gets client.
     *
     * @param req     the req
     * @param cookies the cookies
     * @return the client
     */
    protected static CloseableHttpClient getClient(HttpRequestBase req, Cookie... cookies) {
        if (HttpUtil.closeableHttpClient != null) {
            return HttpUtil.closeableHttpClient;
        }
        HttpClientBuilder custom = HttpClients.custom();
        reqInterceptors.forEach(x -> custom.addInterceptorFirst(x));
        resInterceptors.forEach(x -> custom.addInterceptorLast(x));
        String url = req.getURI().toString();
        if (!Objects.isNull(url) && url.startsWith(HTTPS)) {
            custom.setSSLSocketFactory(SSL_CONTEXT);
        }
        if (!Objects.isNull(cookies)) {
            custom.setDefaultCookieStore(getStore(req, cookies));
        }
        return HttpUtil.closeableHttpClient = custom
                .setConnectionManagerShared(true)
                .setConnectionManager(getConnectionManager())
                .setKeepAliveStrategy(getKeepAliveStrategy())
                .setDefaultRequestConfig(CONFIG)
                .evictExpiredConnections()
                .build();
    }

    /**
     * Gets connection manager.
     *
     * @return the connection manager
     */
    protected static HttpClientConnectionManager getConnectionManager() {
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        // 设置最大连接数不超过
        cm.setMaxTotal(maxTotal);
        // 每个主机最大的连接数
        cm.setDefaultMaxPerRoute(maxPerRoute);
        // 可用空闲连接过期时间, 重用空闲连接时会先检查是否空闲时间超过这个时间，如果超过，释放socket重新建立
        cm.setValidateAfterInactivity(validateAfterInactivity);
        return cm;
    }

    /**
     * Gets keep alive strategy.
     *
     * @return the keep alive strategy
     */
    protected static ConnectionKeepAliveStrategy getKeepAliveStrategy() {
        // ConnectionKeepAliveStrategy myStrategy = new ConnectionKeepAliveStrategy()
        return (HttpResponse response, HttpContext context) -> {
            Args.notNull(response, "HTTP response");
            final HeaderElementIterator it = new BasicHeaderElementIterator(
                    response.headerIterator(HTTP.CONN_KEEP_ALIVE));
            while (it.hasNext()) {
                final HeaderElement he = it.nextElement();
                final String param = he.getName();
                final String value = he.getValue();
                if (value != null && param.equalsIgnoreCase("timeout")) {
                    try {
                        return Long.parseLong(value) * 1000;
                    } catch (final NumberFormatException ignore) {
                    }
                }
            }
            return 1;
        };
    }

    /**
     * Gets store.
     *
     * @param req     the req
     * @param cookies the cookies
     * @return the store
     */
    protected static CookieStore getStore(HttpRequestBase req, Cookie[] cookies) {
        BasicCookieStore store = new BasicCookieStore();
        if (BoolUtil.notNull(cookies)) {
            for (Cookie c : cookies) {
                if (BoolUtil.allNotNull(c.getName(), c.getValue())) {
                    BasicClientCookie cookie = new BasicClientCookie(c.getName(), c.getValue());
                    cookie.setVersion(0);
                    cookie.setDomain(req.getURI().getHost());
                    cookie.setPath("/");
                    store.addCookie(cookie);
                }
            }
        }
        return store;
    }
}
