package cn.gmlee.tools.api.gray;

import cn.gmlee.tools.api.anno.ApiCoexist;
import cn.gmlee.tools.api.gray.model.Gray;
import cn.gmlee.tools.api.gray.model.GrayEnums;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 基于多版本接口共存方案实现
 *
 * @param <Request>  the type parameter
 * @param <Response> the type parameter
 * @author Jas°
 * @date 2020 /12/3 (周四)
 */
public abstract class AbstractGrayFilter<Request, Response> implements GrayFilter<Request, Response> {
    /**
     * 日志输出工具.
     */
    private Logger logger = LoggerFactory.getLogger(AbstractGrayFilter.class);
    /**
     * 蚂蚁url规则匹配工具.
     */
    protected AntPathMatcher urlMatcher = new AntPathMatcher();
    /**
     * 存储灰度权重规则所有url的计数器
     * <p>
     * 当规则匹配失败时清理url计数器
     * </p>
     */
    protected ConcurrentHashMap<String, Integer> visitsMap = new ConcurrentHashMap();

    @Override
    public final void doGray(Request request, Response response) {
        // 无适用规则即不开启灰度
        Gray gray = chooseGray(request);
        try {
            // 请求是否需要灰度
            if (enableGray(request, response, gray)) {
                deploy(request, response, gray);
            }
        } catch (Exception e) {
            logger.error(String.format("启用灰度过滤器出错: %s", "过滤器运行异常"), e);
        } finally {
            clearData(request, response, gray);
        }
    }


    /**
     * Enable gray boolean.
     *
     * @param request  the request
     * @param response the response
     * @param gray     the gray
     * @return the boolean
     */
    public final boolean enableGray(Request request, Response response, Gray gray) {
        // 已经自定义灰度: 不开启
        if (existCustom(getVersion(request))) {
            return false;
        }
        // 已经固用了灰度: 开启
        if (existFixed(getUrl(request), getFixedUrls(request))) {
            return true;
        }
        // 规则不存在: 不开启
        if (Objects.isNull(gray) || !GrayEnums.RULES.contains(gray.getRule())) {
            return false;
        }
        return enable(request, gray);
    }


    /**
     * Deploy boolean.
     *
     * @param request  the request
     * @param response the response
     * @param gray     the gray
     * @return the boolean
     */
    public boolean deploy(Request request, Response response, Gray gray) {
        addMark(request, response, ApiCoexist.VERSION_NAME, gray.getVersion());
        Set<String> fixedUrls = getFixedUrls(request);
        boolean add = fixedUrls.add(getUrl(request));
        if (add) {
            restFixedUrls(request, response, GrayEnums.GRAY_COOKIE, fixedUrls);
        }
        return true;
    }

    // -----------------------------------------------------------------------------------------------------------------

    private Gray chooseGray(Request request) {
        String url = getUrl(request);
        List<Gray> grays = getGrays();
        if (!CollectionUtils.isEmpty(grays)) {
            Optional<Gray> optional = grays.stream().filter(gray -> urlMatcher.match(gray.getUrlPatterns(), url)).findFirst();
            boolean present = optional.isPresent();
            return present ? optional.get() : null;
        }
        return null;
    }

    // -----------------------------------------------------------------------------------------------------------------

    private boolean existCustom(String version) {
        if (!StringUtils.isEmpty(version)) {
            return true;
        }
        return false;
    }

    private boolean existFixed(String url, Set<String> fixedUrls) {
        if (fixedUrls.contains(url)) {
            return true;
        }
        return false;
    }

    private boolean calcWeight(String url, Gray gray) {
        Integer visits = visitsMap.get(url);
        try {
            // 大于100重置计数器
            if (visits == null || visits > Gray.MAX_WEIGHT) {
                visitsMap.put(url, visits = 0);
            }
            visitsMap.put(url, ++visits);
            Integer weight = gray.getWeight();
            // 权重小于等于0或则大于100不启用灰度
            if (weight <= 0 || weight > Gray.MAX_WEIGHT) {
                return false;
            }
            return visits % ((int) Math.ceil(Gray.MAX_WEIGHT / weight)) == 0;
        } catch (Exception e) {
            logger.debug(String.format("启用灰度过滤器%s规则计算出错: %s", GrayEnums.WEIGHT, url), e);
        }
        return false;
    }

    /**
     * 规则总开关总开关.
     *
     * @param request
     * @param gray    the gray
     * @return the boolean
     */
    private boolean enable(Request request, Gray gray) {
        boolean ips = matchIps(getIp(request), gray);
        boolean tokens = matchTokens(getToken(request), gray);
        boolean weight = matchWeight(getUrl(request), gray);
        return ips || tokens || weight;
    }

    private boolean matchWeight(String url, Gray gray) {
        String rule = gray.getRule();
        if (!StringUtils.isEmpty(rule) && rule.contains(GrayEnums.WEIGHT)) {
            return calcWeight(url, gray);
        }
        return false;
    }

    private boolean matchIps(String ip, Gray gray) {
        String rule = gray.getRule();
        if (!StringUtils.isEmpty(rule) && rule.contains(GrayEnums.IPS)) {
            return gray.getIps().contains(ip);
        }
        return false;
    }

    private void clearData(Request request, Response response, Gray gray) {
        if (Objects.isNull(gray)) {
            visitsMap.remove(getUrl(request));
            Set<String> fixedUrls = getFixedUrls(request);
            boolean remove = fixedUrls.remove(getUrl(request));
            if (remove) {
                restFixedUrls(request, response, GrayEnums.GRAY_COOKIE, fixedUrls);
            }
        }
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Match tokens boolean.
     *
     * @param token the token
     * @param gray  the gray
     * @return the boolean
     */
    protected abstract boolean matchTokens(String token, Gray gray);

    /**
     * Add mark.
     *
     * @param request  the request
     * @param response the response
     * @param name     the name
     * @param value    the value
     * @return the request
     */
    protected abstract void addMark(Request request, Response response, String name, String value);

    /**
     * Rest fixed urls.
     *
     * @param request   the request
     * @param response  the response
     * @param name      the name
     * @param fixedUrls the fixed urls
     */
    protected abstract void restFixedUrls(Request request, Response response, String name, Set<String> fixedUrls);
}
