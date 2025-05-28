package cn.gmlee.tools.base.util;

import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.util.*;

/**
 * 通用HTTP连接地址工具
 *
 * @author Jas °
 * @date 2020 /12/14 (周一)
 */
public class UrlUtil {

    private static final AntPathMatcher urlMatcher = new AntPathMatcher();

    /**
     * The constant CHARSET.
     */
    public static final String CHARSET = "utf-8";
    /**
     * The constant PARAM_SPLIT_CODE.
     */
    public static final String PARAM_SPLIT_CODE = "\\?";
    /**
     * The constant PARAM_START_CODE.
     */
    public static final String PARAM_START_CODE = "?";
    /**
     * The constant PARAM_SPLICE_CODE.
     */
    public static final String PARAM_SPLICE_CODE = "&";
    /**
     * The constant PARAM_WITH_CODE.
     */
    public static final String PARAM_WITH_CODE = "=";

    /**
     * URL编码.
     * <p>
     * 防止多次编码
     * </p>
     *
     * @param url the url
     * @return the string
     */
    public static String encode(String url) {
        try {
            // 找到原始url(永远返回一次编码的结果)
            String decode = decode(NullUtil.get(url));
            return URLEncoder.encode(decode, CHARSET);
        } catch (UnsupportedEncodingException e) {
            return ExceptionUtil.cast(String.format("无法对%s进行编码", url), e);
        }
    }

    /**
     * URL编码.
     * <p>
     * 防止多次编码
     * </p>
     *
     * @param url the url
     * @return the string
     */
    public static String encodeOnce(String url) {
        try {
            return URLEncoder.encode(NullUtil.get(url), CHARSET);
        } catch (UnsupportedEncodingException e) {
            return ExceptionUtil.cast(String.format("无法对%s进行编码", url), e);
        }
    }

    /**
     * URL解码.
     * <p>
     * 防止多次编码
     * </p>
     *
     * @param url the url
     * @return the string
     */
    public static String decode(String url) {
        String last = decodeOnce(url);
        while (!last.equals(decodeOnce(last))) {
            return decode(decodeOnce(last));
        }
        return last;
    }

    /**
     * URL解码.
     * <p>
     * 只解一次
     * </p>
     *
     * @param url the url
     * @return the string
     */
    public static String decodeOnce(String url) {
        try {
            if (BoolUtil.contain(url, "+")) {
                // 包含+符合无法解码
                return url;
            }
            return URLDecoder.decode(url, CHARSET);
        } catch (UnsupportedEncodingException e) {
            return ExceptionUtil.cast(String.format("无法对%s进行解码", url), e);
        }
    }


    /**
     * 匹配1个url.
     *
     * @param patterns 所有规则
     * @param urls     所有地址
     * @return 所有 {@param urls} 中有任意1个匹配任意1个{@param patterns}既返回true
     */
    public static boolean matchOne(Collection<String> patterns, String... urls) {
        AssertUtil.notNull(patterns, String.format("匹配规则是空"));
        for (String url : NullUtil.get(urls, new String[0])) {
            Optional<String> optional = patterns.stream().filter(pattern -> urlMatcher.match(pattern, url)).findFirst();
            if (optional.isPresent()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Match first string.
     *
     * @param patterns the patterns
     * @param urls     the urls
     * @return the string
     */
    public static String matchFirst(Collection<String> patterns, String... urls) {
        AssertUtil.notNull(patterns, String.format("匹配规则是空"));
        for (String url : NullUtil.get(urls, new String[0])) {
            Optional<String> optional = patterns.stream().filter(pattern -> urlMatcher.match(pattern, url)).findFirst();
            if (optional.isPresent()) {
                return optional.get();
            }
        }
        return null;
    }


    /**
     * 匹配1个url.
     *
     * @param pattern 规则
     * @param urls    所有地址
     * @return 所有 {@param urls} 中有任意1个匹配任意1个{@param patterns}既返回true
     */
    public static boolean matchOne(String pattern, String... urls) {
        AssertUtil.notEmpty(pattern, String.format("匹配规则是空"));
        for (String url : NullUtil.get(urls, new String[0])) {
            if (urlMatcher.match(pattern, url)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Match first string.
     *
     * @param pattern the pattern
     * @param urls    the urls
     * @return the string
     */
    public static String matchFirst(String pattern, String... urls) {
        AssertUtil.notEmpty(pattern, String.format("匹配规则是空"));
        for (String url : NullUtil.get(urls, new String[0])) {
            if (urlMatcher.match(pattern, url)) {
                return url;
            }
        }
        return null;
    }

    /**
     * URL文件下载.
     *
     * @param url the url
     * @return the byte array output stream
     */
    public static ByteArrayOutputStream download(String url) {
        InputStream in = null;
        try {
            // 将在线图片地址转换为URL对象
            URL u = new URL(url);
            // 打开链接
            URLConnection connection = u.openConnection();
            // 转换为连接
            HttpURLConnection httpURLConnection = (HttpURLConnection) connection;
            // 获取输入流
            in = httpURLConnection.getInputStream();
            // 转换复用流
            return StreamUtil.toStream(in);
        } catch (IOException e) {
            throw new RuntimeException(String.format("文件下载错误: %s", url), e);
        } finally {
            // 关闭输入流
            if (in != null) {
                ExceptionUtil.suppress(in::close);
            }
        }
    }


    /**
     * 获取URL指定参数
     *
     * @param url 请求
     * @return 参数集 params
     */
    public static Map<String, Object> getParams(String url) {
        if (!StringUtils.isEmpty(url)) {
            String[] split = url.split(PARAM_SPLIT_CODE);
            if (split.length > 1) {
                return getQueryString(split[1]);
            } else {
                return getQueryString(split[0]);
            }
        }
        return new HashMap(0);
    }


    private static Map<String, Object> getQueryString(String queryString) {
        Map<String, Object> map = new HashMap(0);
        String[] params = NullUtil.get(queryString).split(PARAM_SPLICE_CODE);
        for (String str : params) {
            String[] kv = str.split(PARAM_WITH_CODE);
            // 提供自动解码
            String val = kv.length > 1 ? UrlUtil.decode(kv[1]) : "";
            if (BoolUtil.allNotEmpty(kv[0], val)) {
                if (map.containsKey(kv[0])) {
                    Object value = map.get(kv[0]);
                    if (value instanceof Collection) {
                        ((Collection) value).add(val);
                    } else if (!Objects.isNull(value)) {
                        List values = new ArrayList();
                        values.add(val);
                        values.add(value);
                        map.put(UrlUtil.decode(kv[0]), values);
                    }
                } else {
                    map.put(UrlUtil.decode(kv[0]), val);
                }
            }
        }
        return map;
    }
}
