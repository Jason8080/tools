package cn.gmlee.tools.api.assist;

import cn.gmlee.tools.api.gray.model.GrayEnums;
import cn.gmlee.tools.base.util.JsonUtil;
import cn.gmlee.tools.base.util.UrlUtil;
import cn.gmlee.tools.base.util.WebUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.tomcat.util.http.MimeHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 通用灰度请求头辅助工具
 *
 * @author Jas°
 * @date 2020 /11/30 (周一)
 */
public class GrayMarkAssist {

    private static Logger logger = LoggerFactory.getLogger(GrayMarkAssist.class);


    public static Set<String> getCookieUrls(HttpServletRequest req) {
        Set<String> urls = new HashSet();
        String cookie = WebUtil.getCookie(GrayEnums.GRAY_COOKIE, req);
        try {
            if (!StringUtils.isEmpty(cookie)) {
                String decode = UrlUtil.decode(cookie);
                List<String> cookies = JsonUtil.toBean(decode, new TypeReference<List<String>>() {});
                urls.addAll(cookies);
            }
        } catch (Exception e) {
            logger.debug(String.format("获取灰度固用数据出错: %s", cookie), e);
        }
        return urls;
    }

    public static void addRequestHeader(HttpServletRequest request, String name, String value) {
        Class<? extends HttpServletRequest> clazz = request.getClass();
        try {
            MimeHeaders mimeHeaders = getMimeHeaders(request, clazz);
            mimeHeaders.removeHeader(name);
            mimeHeaders.addValue(name).setString(value);
        } catch (Exception e) {
            logger.error("启用灰度过滤器出错: 无法注入灰度请求头%s-%s", name, value);
        }
    }

    public static void resetResponseCookie(HttpServletResponse response, String name, Set<String> fixedUrls) {
        try {
            String value = JsonUtil.toJson(fixedUrls);
            String encode = UrlUtil.encode(value);
            Cookie cookie = new Cookie(name, encode);
            cookie.setPath("/");
            cookie.setMaxAge(Integer.MAX_VALUE);
            response.addCookie(cookie);
        } catch (Exception e) {
            logger.error(String.format("重置灰度Cookie固定策略出错: %s", fixedUrls), e);
        }
    }


    private static MimeHeaders getMimeHeaders(HttpServletRequest request, Class<? extends HttpServletRequest> clazz) throws NoSuchFieldException, IllegalAccessException {
        Field requestField = clazz.getDeclaredField("request");
        requestField.setAccessible(true);

        Object o = requestField.get(request);
        Field coyoteRequest = o.getClass().getDeclaredField("coyoteRequest");
        coyoteRequest.setAccessible(true);

        Object o2 = coyoteRequest.get(o);
        Field headers = o2.getClass().getDeclaredField("headers");
        headers.setAccessible(true);
        return (MimeHeaders) headers.get(o2);
    }
}
