package cn.gmlee.tools.base.util;

import cn.gmlee.tools.base.entity.Coords;
import cn.gmlee.tools.base.enums.Regex;
import cn.gmlee.tools.base.enums.XState;
import cn.gmlee.tools.base.mod.HttpResult;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.protocol.HTTP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 通用全局请求响应处理工具
 *
 * @author Jas °
 * @date 2020 /9/14 (周一)
 */
public class WebUtil {

    private static final Logger log = LoggerFactory.getLogger(WebUtil.class);
    // 网速快
    private static final String PACIFIC_API = "https://whois.pconline.com.cn/ipJson.jsp?ip=%s&json=true";
    // 精度高
    private static final String IP_API = "http://ip-api.com/json/%s?lang=zh-CN";

    /**
     * GET url 传参标识符
     */
    public static final String PARAM_SPLIT_CODE = "\\?";

    /**
     * The constant PARAM_SPLIT_CODE.
     */
    public static final String PROTOCOL_SPLIT_CODE = "://";

    /**
     * The constant PORT_SPLIT_CODE.
     */
    public static final String PORT_SPLIT_CODE = ":";
    /**
     * The constant PATH_SPLIT_CODE.
     */
    public static final String PATH_SPLIT_CODE = "/";
    /**
     * The constant DOMAIN_SPLIT_CODE.
     */
    public static final String DOMAIN_SPLIT_CODE = "\\.";

    /**
     * The constant PARAM_START_CODE.
     */
    public static final String PARAM_START_CODE = "?";

    /**
     * URL参数连接符.
     */
    public static final String PARAM_SPLICE_CODE = "&";

    /**
     * URL参数赋值符.
     */
    public static final String PARAM_WITH_CODE = "=";


    /**
     * 全局获取请求对象 (注意: service另起线程获取不到).
     *
     * @return the http servlet request
     */
    public static HttpServletRequest getRequest() {
        ServletRequestAttributes sra = (ServletRequestAttributes)
                RequestContextHolder.getRequestAttributes();
        return sra != null ? sra.getRequest() : null;
    }

    /**
     * 全局获取响应对象 (注意: service另起线程获取不到).
     *
     * @return the http servlet response
     */
    public static HttpServletResponse getResponse() {
        ServletRequestAttributes sra = (ServletRequestAttributes)
                RequestContextHolder.getRequestAttributes();
        return sra != null ? sra.getResponse() : null;
    }


    private static final String unknown = "unknown";

    private static final String[] IP_HEAD_NAMES = {"x-forwarded-for", "HTTP_X_FORWARDED_FOR", "Proxy-Client-IP", "WL-Proxy-Client-IP", "HTTP_CLIENT_IP", "X-Real-IP"};

    /**
     * 获取用户真实IP地址，不使用request.getRemoteAddr();的原因是有可能用户使用了代理软件方式避免真实IP地址,
     * <p>
     * 可是，如果通过了多级反向代理的话，X-Forwarded-For的值并不止一个，而是一串IP值，究竟哪个才是真正的用户端的真实IP呢？
     * 答案是取X-Forwarded-For中第一个非unknown的有效IP字符串。
     * <p>
     * 如：X-Forwarded-For：192.168.1.110, 192.168.1.120, 192.168.1.130, 192.168.1.100
     * <p>
     * 用户真实IP为： 192.168.1.110
     *
     * @param req the req
     * @return ip ip
     */
    public static String getIp(HttpServletRequest req) {
        String ip = null;
        Enumeration<String> names = req.getHeaderNames();
        star:
        while (names.hasMoreElements()) {
            String key = names.nextElement();
            for (String head : IP_HEAD_NAMES) {
                if (!head.equalsIgnoreCase(key)) {
                    continue;
                }
                ip = req.getHeader(key);
                if (BoolUtil.isEmpty(ip) || unknown.equalsIgnoreCase(ip)) {
                    break;
                }
                break star;
            }
        }
        // 如果多层代理返回第一个
        if (ip != null && ip.indexOf(",") > 0) {
            log.debug("发现多级代理: {}", ip);
            ip = ip.substring(0, ip.indexOf(","));
        }
        return ip;
    }

    /**
     * 从请求头中获取真实请求地址
     *
     * @param headers the headers
     * @return ip ip
     */
    public static String getIp(Map<String, String> headers) {
        String ip = null;
        Iterator<Map.Entry<String, String>> it = headers.entrySet().iterator();
        star:
        while (it.hasNext()) {
            Map.Entry<String, String> next = it.next();
            String key = next.getKey();
            String value = next.getValue();
            for (String head : IP_HEAD_NAMES) {
                if (!head.equalsIgnoreCase(key)) {
                    continue;
                }
                ip = value;
                if (BoolUtil.isEmpty(ip) || unknown.equalsIgnoreCase(ip)) {
                    break;
                }
                break star;
            }
        }
        // 如果多层代理返回第一个
        if (ip != null && ip.indexOf(",") > 0) {
            log.debug("发现多级代理: {}", ip);
            ip = ip.substring(0, ip.indexOf(","));
        }
        return ip;
    }


    /**
     * 获取用户IP属地
     *
     * @param req the req
     * @return the city
     */
    public static String getCity(HttpServletRequest req) {
        String ip = getIp(req);
        return getCity(ip);
    }

    /**
     * Gets city.
     *
     * @param ip the ip
     * @return the city
     */
    public static String getCity(String ip) {
        String url = String.format(IP_API, ip);
        HttpResult httpResult = HttpUtil.get(url);
        String body = httpResult.byteResponseBody2String();
        if (!httpResult.isOk()) {
            log.error("查询IP属地异常: {}", body);
            return unknown.toUpperCase();
        }
        Map map = JsonUtil.toBean(body, Map.class);
        String status = (String) map.get("status");
        if (!"success".equalsIgnoreCase(status)) {
            String message = (String) map.get("message");
            log.error("查询IP属地异常: {}", message);
            return BoolUtil.contain(message, "private range") ? "局域网" : unknown.toUpperCase();
        }
        String country = (String) map.get("country");
        String pro = (String) map.get("regionName");
        String city = (String) map.get("city");
        if (BoolUtil.isEmpty(country + pro + city)) {
            return unknown.toUpperCase();
        }
        return String.format("%s %s %s", country, pro, city);
    }

    /**
     * Gets current ip.
     *
     * @return the current ip
     */
    public static String getCurrentIp() {
        HttpServletRequest req = WebUtil.getRequest();
        if (req != null) {
            return getIp(req);
        }
        return null;
    }

    /**
     * Gets current city.
     *
     * @return the current city
     */
    public static String getCurrentCity() {
        HttpServletRequest req = WebUtil.getRequest();
        if (req != null) {
            return getCity(req);
        }
        return null;
    }

    /**
     * 获取坐标
     *
     * @return current coords
     */
    public static Coords getCurrentCoords() {
        HttpServletRequest req = WebUtil.getRequest();
        if (req != null) {
            return getCoords(req);
        }
        return new Coords();
    }

    /**
     * 获取坐标
     *
     * @param req the req
     * @return coords coords
     */
    public static Coords getCoords(HttpServletRequest req) {
        String lon = getParameter("lon", req);
        String lat = getParameter("lat", req);
        if (BoolUtil.allNotEmpty(lon, lat)) {
            return new Coords(lon, lat);
        }
        String url = String.format(IP_API, getIp(req));
        HttpResult httpResult = HttpUtil.get(url);
        String body = httpResult.byteResponseBody2String();
        if (!httpResult.isOk()) {
            log.error("查询IP坐标异常: {}", body);
            return new Coords();
        }
        Map map = JsonUtil.toBean(body, Map.class);
        String status = (String) map.get("status");
        if (!"success".equalsIgnoreCase(status)) {
            String message = (String) map.get("message");
            log.error("查询IP坐标异常: {}", message);
        }
        Object lonQuery = map.get("lon");
        Object latQuery = map.get("lat");
        if (BoolUtil.allNotEmpty(lon, lat)) {
            return new Coords(lonQuery.toString(), latQuery.toString());
        }
        return new Coords();
    }

    /**
     * 获取域名.
     *
     * @return the string
     */
    public static String getCurrentDomain() {
        HttpServletRequest req = WebUtil.getRequest();
        if (req != null) {
            return getDomain(req);
        }
        return null;
    }

    /**
     * 获取域名.
     *
     * @param req the req
     * @return the string
     */
    public static String getDomain(HttpServletRequest req) {
        return getDomain(req.getRequestURL().toString());
    }

    /**
     * 获取域名.
     *
     * @param url the url
     * @return the domain
     */
    public static String getDomain(String url) {
        String delProtocol = url.substring(url.indexOf(PROTOCOL_SPLIT_CODE) + PROTOCOL_SPLIT_CODE.length());
        int pathIndex = delProtocol.indexOf(PATH_SPLIT_CODE);
        String baseUrl = delProtocol;
        if (pathIndex >= 0) {
            baseUrl = delProtocol.substring(0, pathIndex);
        }
        int portIndex = baseUrl.indexOf(PORT_SPLIT_CODE);
        String domain = baseUrl;
        if (portIndex >= 0) {
            domain = delProtocol.substring(0, portIndex);
        }
        return domain;
    }


    /**
     * 获取当前顶级域名.
     *
     * @return the current domain top
     */
    public static String getCurrentDomainTop() {
        HttpServletRequest req = WebUtil.getRequest();
        if (req != null) {
            return getDomainTop(req);
        }
        return null;
    }

    /**
     * 获取顶级域名.
     *
     * @param req the req
     * @return the domain top
     */
    public static String getDomainTop(HttpServletRequest req) {
        return getDomainTop(getDomain(req));
    }

    /**
     * 获取顶级域名.
     * <p>
     * 正确的传参: order.gmlee.cn; gmlee.cn
     * </p>
     *
     * @param domain 该参数必须是域名,而非请求地址: http://gmlee.cn/xx?6=1
     * @return the domain top
     */
    public static String getDomainTop(String domain) {
        if (domain != null) {
            String[] array = domain.split(DOMAIN_SPLIT_CODE);
            if (array.length > 1) {
                return array[array.length - 2] + "." + array[array.length - 1];
            }
        }
        return domain;
    }

    /**
     * 获取请求者端口.
     *
     * @param req the req
     * @return the port
     */
    public static Integer port(HttpServletRequest req) {
        //这个是请求者的端口号并非请求端口号
        return req.getRemotePort();
    }

    /**
     * Current port integer.
     *
     * @return the integer
     */
    public static Integer currentPort() {
        HttpServletRequest req = WebUtil.getRequest();
        if (req != null) {
            //这个是请求者的端口号并非请求端口号
            return port(req);
        }
        return null;
    }


    /**
     * 请求端口.
     *
     * @param req the req
     * @return the req port
     */
    public static Integer getPort(HttpServletRequest req) {
        return req.getServerPort();
    }

    /**
     * Gets current port.
     *
     * @return the current port
     */
    public static Integer getCurrentPort() {
        HttpServletRequest req = WebUtil.getRequest();
        if (req != null) {
            return getPort(req);
        }
        return null;
    }

    /**
     * 请求的全路径.
     *
     * @param req the req
     * @return the path
     */
    public static String getUrl(HttpServletRequest req) {
        return req.getRequestURL().toString();
    }

    /**
     * Gets current url.
     *
     * @return the current url
     */
    public static String getCurrentUrl() {
        HttpServletRequest req = WebUtil.getRequest();
        if (req != null) {
            return getUrl(req);
        }
        return null;
    }

    /**
     * 请求的全路径?带参数.
     *
     * @param req the req
     * @return the path
     */
    public static String getPath(HttpServletRequest req) {
        StringBuffer url = req.getRequestURL();
        if (!StringUtils.isEmpty(req.getQueryString())) {
            url.append("?").append(req.getQueryString());
        }
        return url.toString();
    }

    /**
     * Gets current path.
     *
     * @return the current path
     */
    public static String getCurrentPath() {
        HttpServletRequest req = WebUtil.getRequest();
        if (req != null) {
            return getPath(req);
        }
        return null;
    }


    /**
     * 相对路径.
     *
     * @param req the req
     * @return the rel path
     */
    public static String getRelPath(HttpServletRequest req) {
        return req.getServletPath();
    }

    /**
     * Gets current rel path.
     *
     * @return the current rel path
     */
    public static String getCurrentRelPath() {
        HttpServletRequest req = WebUtil.getRequest();
        if (req != null) {
            return getRelPath(req);
        }
        return null;
    }

    /**
     * 获取服务器地址.
     * <p>
     * https://localhost
     * https://localhost:8080
     * https://127.0.0.1:443
     * </p>
     *
     * @param req the req
     * @return the server addr
     */
    public static String getServerAddress(HttpServletRequest req) {
        String url = WebUtil.getUrl(req);
        // 编译正则表达式
        Pattern pattern = Regex.HTTP_SERVER_ADDR.patternIgnoreCase;
        // 忽略大小写的写法
        Matcher matcher = pattern.matcher(url);
        if (!matcher.matches()) {
            String[] split2 = url.split(Regex.HTTP_SERVER_ADDR.regex);
            if (split2.length > 1) {
                return url.substring(0, url.length() - split2[1].length());
            } else {
                return split2[0];
            }
        }
        return url;
    }

    /**
     * Gets current server address.
     *
     * @return the current server address
     */
    public static String getCurrentServerAddress() {
        return getServerAddress(getRequest());
    }


    /**
     * 获取请求主机.
     *
     * @param req the req
     * @return the host
     */
    public static String getHost(HttpServletRequest req) {
        return req.getRemoteHost();
    }

    /**
     * Gets current host.
     *
     * @return the current host
     */
    public static String getCurrentHost() {
        HttpServletRequest req = WebUtil.getRequest();
        if (req != null) {
            return getHost(req);
        }
        return null;
    }

    /**
     * 获取项目根路径(/项目名).
     *
     * @param req the req
     * @return the host
     */
    public static String getRoot(HttpServletRequest req) {
        return req.getContextPath();
    }

    /**
     * Gets current root.
     *
     * @return the current root
     */
    public static String getCurrentRoot() {
        HttpServletRequest req = WebUtil.getRequest();
        if (req != null) {
            return getRoot(req);
        }
        return null;
    }

    /**
     * 获取接口基础地址.
     * <p>
     * 例:
     * http://localhost:8080/project/
     * https://xxx.com:443/project/
     * https://xxx.com:443/
     * </p>
     *
     * @param req the req
     * @return the base url
     */
    public static String getBaseUrl(HttpServletRequest req) {
        // 获取请求url(全路径)
        String url = getServerAddress(req);
        // 获取项目根目录(一般默认/)
        String baseUrl = url + "/" + getRoot(req);
        return baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
    }

    /**
     * Gets current base url.
     *
     * @return the current base url
     */
    public static String getCurrentBaseUrl() {
        return getBaseUrl(getRequest());
    }

    /**
     * 给当前响应追加Cookies.
     *
     * @param name  the name
     * @param value the value
     * @return the cookie
     */
    public static Cookie addCurrentCookie(String name, String value) {
        HttpServletRequest req = WebUtil.getRequest();
        if (req != null) {
            return addCurrentCookie(WebUtil.getDomainTop(req), name, value);
        }
        return null;
    }

    /**
     * Add current cookie cookie.
     *
     * @param domain the domain
     * @param name   the name
     * @param value  the value
     * @return the cookie
     */
    public static Cookie addCurrentCookie(String domain, String name, String value) {
        HttpServletResponse res = WebUtil.getResponse();
        if (BoolUtil.allNotNull(res)) {
            return addCookie(res, name, value, 1, 86400 * 30, domain, "/", false, false, "TOOLS-COOKIES");
        }
        return null;
    }

    /**
     * Add current cookie cookie.
     *
     * @param name     the name
     * @param value    the value
     * @param httpOnly the http only
     * @return the cookie
     */
    public static Cookie addCurrentCookie(String name, String value, boolean httpOnly) {
        HttpServletRequest req = WebUtil.getRequest();
        HttpServletResponse res = WebUtil.getResponse();
        if (BoolUtil.allNotNull(req, res)) {
            String domain = WebUtil.getDomainTop(req);
            return addCookie(res, name, value, 1, 86400 * 30, domain, "/", httpOnly, false, "TOOLS-COOKIES");
        }
        return null;
    }


    /**
     * 追加指定域的Cookies.
     *
     * @param request  the request
     * @param response the response
     * @param name     the name
     * @param value    the value
     * @return the cookie
     */
    public static Cookie addCookie(HttpServletRequest request, HttpServletResponse response, String name, String value) {
        return addCookie(response, name, value, 1, 86400 * 30, WebUtil.getDomainTop(request), "/", false, false, "TOOLS-COOKIES");
    }

    /**
     * Add cookie cookie.
     *
     * @param request  the request
     * @param response the response
     * @param name     the name
     * @param value    the value
     * @param httpOnly the http only
     * @return the cookie
     */
    public static Cookie addCookie(HttpServletRequest request, HttpServletResponse response, String name, String value, boolean httpOnly) {
        return addCookie(response, name, value, 1, 86400 * 30, WebUtil.getDomainTop(request), "/", httpOnly, false, "TOOLS-COOKIES");
    }

    /**
     * Add cookie cookie.
     *
     * @param response the response
     * @param domain   the domain
     * @param name     the name
     * @param value    the value
     * @return the cookie
     */
    public static Cookie addCookie(HttpServletResponse response, String domain, String name, String value) {
        return addCookie(response, name, value, 1, 86400 * 30, domain, "/", false, false, "TOOLS-COOKIES");
    }

    /**
     * 添加自定义Cookie.
     *
     * @param response the response
     * @param name     the name
     * @param value    the value
     * @param version  the version
     * @param expiry   the expiry
     * @param domain   the domain
     * @param path     the path
     * @param httpOnly the http only
     * @param secure   the secure
     * @param comment  the comment
     * @return the cookie
     */
    public static Cookie addCookie(HttpServletResponse response,
                                   String name, String value, int version, int expiry, String domain,
                                   String path, boolean httpOnly, boolean secure, String comment
    ) {
        Cookie cookie = new Cookie(name, value);
        // 设置版本号
        cookie.setVersion(version);
        // 设置有效期
        cookie.setMaxAge(expiry);
        // 设置域名
        cookie.setDomain(domain);
        cookie.setPath(path);
        // 禁止JS读取
        cookie.setHttpOnly(httpOnly);
        // 允许http传输
        cookie.setSecure(secure);
        cookie.setComment(comment);
        addCookie(response, cookie);
        return cookie;
    }

    /**
     * 添加Cookies.
     *
     * @param response the response
     * @param cookies  the cookies
     */
    public static void addCookie(HttpServletResponse response, Cookie... cookies) {
        if (BoolUtil.notEmpty(cookies)) {
            for (Cookie cookie : cookies) {
                response.addCookie(cookie);
            }
        }
    }

    /**
     * 删除默认Cookies.
     * <p>
     * 只删除顶级域、根路径下的指定Cookies
     * </p>
     *
     * @param request  the request
     * @param response the response
     * @param names    the names
     */
    public static void delCookie(HttpServletRequest request, HttpServletResponse response, String... names) {
        if (BoolUtil.notEmpty(names)) {
            for (String name : names) {
                Cookie cookie = WebUtil.getCookieObj(name, request);
                if (BoolUtil.notNull(cookie)) {
                    String domain = WebUtil.getDomainTop(request);
                    cookie.setDomain(domain);
                    cookie.setPath("/");
                    // 0: 立刻删除, -1: 关闭会话后删除
                    cookie.setMaxAge(XState.NO.code);
                    response.addCookie(cookie);
                }
            }
        }
    }

    /**
     * 获取指定cookie
     *
     * @param name    名称
     * @param request the request
     * @return cookie值 cookie
     */
    public static String getCookie(String name, HttpServletRequest request) {
        Cookie cookie = getCookieObj(name, request);
        return cookie != null ? cookie.getValue() : null;
    }

    /**
     * 获取指定cookie
     *
     * @param name    名称
     * @param request the request
     * @return cookie值 cookie
     */
    public static Cookie getCookieObj(String name, HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (name.equals(cookie.getName())) {
                return cookie;
            }
        }
        return null;
    }

    /**
     * 获取指定参数(优先取用请求头)
     *
     * @param param   参数名
     * @param request 请求
     * @return 参数值 param
     */
    public static String getParam(String param, HttpServletRequest request) {
        String value = request.getHeader(param);
        if (StringUtils.isEmpty(value)) {
            value = request.getParameter(param);
        }
        return value;
    }


    /**
     * 获取指定参数(包括cookie)
     *
     * @param param   参数名
     * @param request 请求
     * @return 参数值 parameter
     */
    public static String getParameter(String param, HttpServletRequest request) {
        String value = getParam(param, request);
        if (StringUtils.isEmpty(value)) {
            value = getCookie(param, request);
        }
        return value;
    }

    /**
     * Gets url parameter map.
     *
     * @param req the req
     * @return url parameter map
     */
    public static Map<String, Object> getUrlParameterMap(HttpServletRequest req) {
        return getQueryString(req.getQueryString());
    }

    /**
     * Gets current header.
     *
     * @param name the name
     * @return current header
     */
    public static String getCurrentHeader(String name) {
        HttpServletRequest req = WebUtil.getRequest();
        if (req != null) {
            return req.getHeader(name);
        }
        return null;
    }

    /**
     * 是否JSON请求.
     *
     * @return the boolean
     */
    public static boolean asJsonRequest() {
        HttpServletRequest req = WebUtil.getRequest();
        if (req != null) {
            String contentType = req.getContentType();
            return contentType.startsWith(HttpUtil.JSON_HEADER);
        }
        return false;
    }

    /**
     * 是否JSON请求.
     *
     * @param req the req
     * @return the boolean
     */
    public static boolean asJson(HttpServletRequest req) {
        String contentType = req.getContentType();
        return contentType.startsWith(HttpUtil.JSON_HEADER);
    }


    /**
     * 获取请求头.
     *
     * @param req the req
     * @return the header map
     */
    public static Map<String, String> getHeaderMap(HttpServletRequest req) {
        Map<String, String> reqHeaders = new HashMap(0);
        Enumeration<String> names = req.getHeaderNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            reqHeaders.put(name, req.getHeader(name));
        }
        return reqHeaders;
    }


    /**
     * Gets current header map.
     *
     * @return the current header map
     */
    public static Map<String, String> getCurrentHeaderMap() {
        HttpServletRequest req = WebUtil.getRequest();
        if (req != null) {
            return getHeaderMap(req);
        }
        return new HashMap(0);
    }

    /**
     * Gets header map.
     *
     * @param headers the headers
     * @return the header map
     */
    public static Map<String, String> getHeaderMap(Header... headers) {
        Map<String, String> resHeaders = new HashMap(0);
        for (Header header : headers) {
            resHeaders.put(header.getName(), header.getValue());
        }
        return resHeaders;
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

    /**
     * To parameter map map.
     *
     * @param map the map
     * @return the map
     */
    public static Map<String, String[]> toParameterMap(Map<String, Object> map) {
        HashMap<String, String[]> parameters = new HashMap<>();
        putAll(parameters, map);
        return parameters;
    }


    private static void putAll(Map<String, String[]> parameters, Map<String, Object> map) {
        if (BoolUtil.isEmpty(map)) {
            return;
        }
        Iterator<Map.Entry<String, Object>> it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Object> next = it.next();
            String key = next.getKey();
            Object value = next.getValue();
            put(parameters, key, value);
        }
    }

    private static void put(Map<String, String[]> parameters, String key, Object value) {
        if (value instanceof Object[]) {
            Object[] os = (Object[]) value;
            String[] array = new String[os.length];
            for (int i = 0; i < os.length; i++) {
                array[i] = os[i] != null ? os[i].toString() : null;
            }
            parameters.put(key, array);
        } else if (value instanceof Collections) {
            Collection<Object> os = (Collection<Object>) value;
            Iterator<Object> iterator = os.iterator();
            String[] array = new String[os.size()];
            for (int i = 0; i < os.size() && iterator.hasNext(); i++) {
                Object o = iterator.next();
                array[i] = o != null ? o.toString() : null;
            }
            parameters.put(key, array);
        } else {
            String[] array = new String[1];
            array[0] = value != null ? value.toString() : null;
            parameters.put(key, array);
        }
    }

    /**
     * 获取请求字符集.
     *
     * @param req the req
     * @return the charset
     */
    public static String getCharset(HttpServletRequest req) {
        return req.getCharacterEncoding();
    }

    /**
     * 获取请求字符集.
     *
     * @param req the req
     * @return the charset
     */
    public static String getCharset(HttpRequestBase req) {
        Header header = req.getFirstHeader(HTTP.CONTENT_TYPE);
        if (Objects.nonNull(header)) {
            HeaderElement[] elements = header.getElements();
            for (HeaderElement e : elements) {
                NameValuePair[] pairs = e.getParameters();
                for (NameValuePair p : pairs) {
                    if ("charset".equalsIgnoreCase(p.getName())) {
                        return p.getValue();
                    }
                }
            }
        }
        return "UTF-8";
    }


    /**
     * 添加1批参数.
     *
     * @param url    the url
     * @param params the params
     * @return 全路径 string
     */
    public static String addParam(String url, Map<String, Object> params) {
        if (BoolUtil.notEmpty(url) && BoolUtil.notEmpty(params)) {
            StringBuilder sb = new StringBuilder();
            Iterator<Map.Entry<String, Object>> it = params.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, Object> next = it.next();
                String key = next.getKey();
                Object value = next.getValue();
                String str = addParam(sb.toString(), key, value.toString());
                sb.append(str);
            }
            sb.insert(0, url);
            return sb.toString();
        }
        return url;
    }

    /**
     * 添加1个参数.
     *
     * @param url   the url
     * @param key   the key
     * @param value the value
     * @return 参数部分 string
     */
    public static String addParam(String url, String key, String value) {
        url = NullUtil.get(url);
        StringBuilder sb = new StringBuilder(url);
        if (BoolUtil.allNotNull(key, value)) {
            if (url.endsWith(PARAM_START_CODE)) {
                sb.append(UrlUtil.encode(key));
                sb.append(PARAM_WITH_CODE);
            } else if (url.contains(PARAM_START_CODE)) {
                sb.append(PARAM_SPLICE_CODE);
                sb.append(UrlUtil.encode(key));
                sb.append(PARAM_WITH_CODE);
            } else {
                sb.append(PARAM_START_CODE);
                sb.append(UrlUtil.encode(key));
                sb.append(PARAM_WITH_CODE);
            }
            sb.append(UrlUtil.encode(value));
        }
        return sb.toString();
    }


    /**
     * 读取请求体字节流.
     *
     * @param req the req
     * @return the body
     */
    public static ByteArrayOutputStream getBody(HttpServletRequest req) {
        try {
            return StreamUtil.toStream(req.getInputStream());
        } catch (IOException e) {
            return new ByteArrayOutputStream();
        }
    }

    /**
     * 读取请求体字节流(当前请求).
     *
     * @return the current body
     */
    public static ByteArrayOutputStream getCurrentBody() {
        HttpServletRequest req = WebUtil.getRequest();
        if (req != null) {
            return getBody(req);
        }
        return new ByteArrayOutputStream();
    }
}
