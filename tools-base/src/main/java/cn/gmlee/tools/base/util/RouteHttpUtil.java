package cn.gmlee.tools.base.util;

import cn.gmlee.tools.base.mod.HttpResult;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.*;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.protocol.HTTP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

/**
 * 通用Http路由工具
 *
 * @author Jas °
 * @date 2021 /3/25 (周四)
 */
public class RouteHttpUtil {
    protected static final Logger logger = LoggerFactory.getLogger(RouteHttpUtil.class);

    /**
     * Route.
     *
     * @param req the req
     * @param res the res
     * @param url the url
     * @return the http result
     * @throws IOException the io exception
     */
    public static HttpResult route(HttpServletRequest req, HttpServletResponse res, String url)
            throws IOException {
        long start = System.currentTimeMillis();
        // 1. 获取方法
        String method = req.getMethod();
        // 2. 封装请求头
        Map<String, String> reqHeaders = WebUtil.getHeaderMap(req);
        // 3. 获取请求参数
        Map<String, Object> urlParameterMap = WebUtil.getUrlParameterMap(req);
        ByteArrayOutputStream stream = StreamUtil.toStream(req.getInputStream());
        url = WebUtil.addParam(url, urlParameterMap);
        // 4. 调用服务接口
        long outerStart = System.currentTimeMillis();
        HttpResult httpResult = way(url, method, reqHeaders, stream.toByteArray(), req.getCookies());
        long outerEnd = System.currentTimeMillis();
        httpResult.setUrl(url);
        httpResult.setMethod(method);
        httpResult.setReqHeaders(reqHeaders);
        httpResult.setBody(stream.toByteArray());
        // 5. 封装响应头
        Header[] resHeaders = httpResult.getResHeaders();
        for (Header header : resHeaders) {
            res.setHeader(header.getName(), header.getValue());
        }
        res.setStatus(httpResult.getStatus());
        // 6. 写入相应内容
        ServletOutputStream out = res.getOutputStream();
        out.write(httpResult.getResult());
        out.flush();
        out.close();
        // 7. 记录时间消耗
        long end = System.currentTimeMillis();
        httpResult.setMillis(end - start);
        httpResult.setOuterMillis(outerEnd - outerStart);
        httpResult.setInnerMillis(httpResult.getMillis() - httpResult.getOuterMillis());
        return httpResult;
    }

    /**
     * 构建基础请求
     *
     * @param method the method
     */
    public static HttpRequestBase build(String url, String method) {
        switch (method) {
            case "GET":
                return new HttpUtil.HttpGetWithEntity(url);
            case "POST":
                return new HttpPost(url);
            case "PUT":
                return new HttpPut(url);
            case "HEAD":
                return new HttpHead(url);
            case "PATCH":
                return new HttpPatch(url);
            case "TRACE":
                return new HttpTrace(url);
            case "DELETE":
                return new HttpDelete(url);
            case "OPTIONS":
                return new HttpOptions(url);
            default:
                logger.warn("不支持方法{}; 已默认构建Get请求", method);
                return new HttpGet(url);
        }
    }

    public static HttpResult way(String url, String method, Map<String, String> reqHeaders, byte[] bytes, Cookie... cookies) {
        return unity(RouteHttpUtil.build(url, method), reqHeaders, bytes, cookies);
    }


    /**
     * 调用GET请求.
     *
     * @param url     the url
     * @param cookies the cookies
     * @return the t
     */
    public static HttpResult get(String url, Cookie... cookies) {
        // 创建GET请求
        HttpGet get = new HttpGet(url);
        // 加载配置
        get.setConfig(HttpUtil.clearCurrentRequestConfig());
        return HttpUtil.execute(get, cookies);
    }


    /**
     * 调用Get请求.
     *
     * @param url     the url
     * @param header  the header
     * @param cookies the cookies
     * @return the http result
     */
    public static HttpResult get(String url, Map<String, String> header, Cookie... cookies) {
        // 创建GET请求
        HttpGet get = new HttpGet(url);
        // 加载配置
        get.setConfig(HttpUtil.clearCurrentRequestConfig());
        // 设置请求头
        addHeader(header, get);
        return HttpUtil.execute(get, cookies);
    }

    /**
     * 调用POST请求
     *
     * @param url     the url
     * @param cookies the cookies
     * @return http result
     */
    public static HttpResult post(String url, Cookie... cookies) {
        // 创建POST请求
        HttpPost post = new HttpPost(url);
        // 加载配置
        post.setConfig(HttpUtil.clearCurrentRequestConfig());
        // 调用请求
        return HttpUtil.execute(post, cookies);
    }


    /**
     * 调用POST请求
     *
     * @param url     the url
     * @param header  the header
     * @param cookies the cookies
     * @return the http result
     */
    public static HttpResult post(String url, Map<String, String> header, Cookie... cookies) {
        //创建POST请求
        HttpPost post = new HttpPost(url);
        //加载配置
        post.setConfig(HttpUtil.clearCurrentRequestConfig());
        //添加请求头
        addHeader(header, post);
        //调用请求
        return HttpUtil.execute(post, cookies);
    }

    /**
     * 调用POST请求
     *
     * @param url     the url
     * @param bytes   the bytes
     * @param cookies the cookies
     * @return http result
     */
    public static HttpResult post(String url, byte[] bytes, Cookie... cookies) {
        // 创建POST请求
        HttpPost post = new HttpPost(url);
        // 加载配置
        post.setConfig(HttpUtil.clearCurrentRequestConfig());
        // 设置请求内容
        HttpUtil.setEntity(post, bytes);
        // 调用请求
        return HttpUtil.execute(post, cookies);
    }


    /**
     * 调用POST请求
     *
     * @param url     the url
     * @param header  the header
     * @param bytes   the bytes
     * @param cookies the cookies
     * @return the http result
     */
    public static HttpResult post(String url, Map<String, String> header, byte[] bytes, Cookie... cookies) {
        //创建POST请求
        HttpPost post = new HttpPost(url);
        //加载配置
        post.setConfig(HttpUtil.clearCurrentRequestConfig());
        //添加请求头
        addHeader(header, post);
        //设置请求内容
        HttpUtil.setEntity(post, bytes);
        //调用请求
        return HttpUtil.execute(post, cookies);
    }

    public static HttpRequestBase clone(HttpRequestBase request, String url) {
        HttpRequestBase newRequest = RouteHttpUtil.build(url, request.getMethod());
        // 加载配置
        newRequest.setConfig(request.getConfig());
        // 添加请求头
        addHeader(newRequest, request.getAllHeaders());
        // 设置请求内容
        if (request instanceof HttpEntityEnclosingRequestBase && newRequest instanceof HttpEntityEnclosingRequestBase) {
            ((HttpEntityEnclosingRequestBase) newRequest).setEntity(((HttpEntityEnclosingRequestBase) request).getEntity());
        }
        return newRequest;
    }

    public static void clone(HttpRequestBase request, HttpRequestBase newRequest) {
        // 加载配置
        newRequest.setConfig(request.getConfig());
        // 添加请求头
        addHeader(newRequest, request.getAllHeaders());
        // 设置请求内容
        if (request instanceof HttpEntityEnclosingRequestBase && newRequest instanceof HttpEntityEnclosingRequestBase) {
            ((HttpEntityEnclosingRequestBase) newRequest).setEntity(((HttpEntityEnclosingRequestBase) newRequest).getEntity());
        }
    }


    public static HttpResult unity(HttpRequestBase request, Map<String, String> reqHeaders, byte[] bytes, Cookie... cookies) {
        // 加载配置
        request.setConfig(HttpUtil.clearCurrentRequestConfig());
        // 添加请求头
        addHeader(reqHeaders, request);
        // 设置请求内容
        if (request instanceof HttpEntityEnclosingRequestBase) {
            HttpUtil.setEntity((HttpEntityEnclosingRequestBase) request, bytes);
        }
        // 调用请求
        return HttpUtil.execute(request, cookies);
    }

    public static void unity(HttpRequestBase request, Map<String, String> reqHeaders, byte[] bytes, FutureCallback<HttpResponse> callback, Cookie... cookies) {
        //加载配置
        request.setConfig(HttpUtil.clearCurrentRequestConfig());
        //添加请求头
        addHeader(reqHeaders, request);
        //设置请求内容
        if (request instanceof HttpEntityEnclosingRequestBase) {
            HttpUtil.setEntity((HttpEntityEnclosingRequestBase) request, bytes);
        }
        //发起异步请求
        execute(request, callback, cookies);
    }

    private static void addHeader(HttpRequestBase http, Header... header) {
        if (BoolUtil.notEmpty(header)) {
            for (Header head : header) {
                String key = head.getName();
                if (HTTP.CONTENT_LEN.equalsIgnoreCase(key)) {
                    continue;
                }
                String value = head.getValue();
                http.addHeader(key, value);
            }
        }
    }

    private static void addHeader(Map<String, String> header, HttpRequestBase http) {
        if (BoolUtil.notEmpty(header)) {
            Iterator<Map.Entry<String, String>> it = header.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, String> next = it.next();
                String key = next.getKey();
                if (HTTP.CONTENT_LEN.equalsIgnoreCase(key)) {
                    continue;
                }
                String value = next.getValue();
                http.addHeader(key, value);
            }
        }
    }

    /**
     * 发起异步请求
     *
     * @param request
     */
    private static void execute(HttpRequestBase request, FutureCallback<HttpResponse> callback,
                                Cookie... cookies) {
        //创建请求工具
        CloseableHttpAsyncClient client = AsyncHttpUtil.getAsyncClient(request, cookies);
        try {
            client.start();
            client.execute(request, callback);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {//关闭请求通道
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
