package cn.gmlee.tools.api.assist;

import cn.gmlee.tools.api.anno.Once;
import cn.gmlee.tools.base.util.ExceptionUtil;
import cn.gmlee.tools.base.util.JsonUtil;
import cn.gmlee.tools.base.util.Md5Util;
import cn.gmlee.tools.base.util.WebUtil;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 防重放机制辅助工具
 *
 * @author Jas°
 * @date 2020/11/3 (周二)
 */
public class OnceAssist {
    /**
     * 防重放机制
     */
    protected static final ConcurrentHashMap<String, Map<String, Long>> onceMap = new ConcurrentHashMap();
    // ================================== <url?queryString, Map<token, deadline>> ======================================

    /**
     * 检测方法中的参数符合重放机制要求
     * <p>
     * 根据请求url?QueryString和token检测是否重复提交
     * </p>
     *
     * @param once
     * @return true: 允许请求, false:不允许请求
     */
    public static boolean requestOk(Once once, Object... args) {
        long timestamp = once.value();
        HttpServletRequest request = WebUtil.getRequest();
        String url = WebUtil.getPath(request);
        Map<String, String> headerMap = WebUtil.getHeaderMap(request);
        String headerJson = ExceptionUtil.sandbox(() -> JsonUtil.toJson(headerMap));
        String bodyJson = ExceptionUtil.sandbox(() -> JsonUtil.toJson(args));
        String token = Md5Util.encode(headerJson + "_" + bodyJson);
        // 访问一次清理一次: 增加单次请求的负荷, 减少系统资源的浪费
        OnceClearAssist.autoClear();
        return isOK(url, token, timestamp);
    }

    /**
     * 判断是否允许访问
     *
     * @param url   接口路径
     * @param token 请求令牌
     * @return true: 允许访问, false:不允许访问
     */
    private static boolean isOK(String url, String token, long timestamp) {
        Map<String, Long> tokenMap = onceMap.get(url);
        // 首次创建api限制
        if (tokenMap == null) {
            tokenMap = new HashMap(1);
            tokenMap.put(token, System.currentTimeMillis() + timestamp);
            onceMap.put(url, tokenMap);
        } else {
            Long offTime = tokenMap.get(token);
            // 首次创建用户token限制
            if (offTime != null && System.currentTimeMillis() < offTime) {
                return false;
            }
            offTime = System.currentTimeMillis() + timestamp;
            tokenMap.put(token, offTime);
        }
        return true;
    }

}
