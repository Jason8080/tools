package cn.gmlee.tools.api.gray;

import cn.gmlee.tools.api.assist.GrayMarkAssist;
import cn.gmlee.tools.api.config.ApiGrayRegistrationCenterProperties;
import cn.gmlee.tools.api.anno.ApiCoexist;
import cn.gmlee.tools.api.gray.model.Gray;
import cn.gmlee.tools.api.gray.model.GrayEnums;
import cn.gmlee.tools.base.util.WebUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * Api灰度过滤器
 * <p>
 * 可以使用@Primary覆盖此过滤器
 * </p>
 *
 * @author Jas°
 */
public class MicroServiceApiCoexistGrayFilter extends AbstractGrayFilter<HttpServletRequest, HttpServletResponse> implements Filter {

    private Logger logger = LoggerFactory.getLogger(MicroServiceApiCoexistGrayFilter.class);

    private final ApiGrayRegistrationCenterProperties apiGrayRegistrationCenterProperties;

    public MicroServiceApiCoexistGrayFilter(ApiGrayRegistrationCenterProperties apiGrayRegistrationCenterProperties) {
        this.apiGrayRegistrationCenterProperties = apiGrayRegistrationCenterProperties;
    }

    @Override
    public List<Gray> getGrays() {
        return apiGrayRegistrationCenterProperties.getGrays();
    }

    @Override
    public final void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        try {
            doGray(req, res);
        } catch (Exception e) {
            logger.error("启用灰度过滤器出错", e);
        } finally {
            chain.doFilter(req, res);
        }
    }

    @Override
    protected boolean matchTokens(String token, Gray gray) {
        logger.warn(String.format("灰度过滤器%s策略未启用: %s", GrayEnums.TOKENS, token));
        return false;
    }

    @Override
    protected void addMark(HttpServletRequest request, HttpServletResponse response, String name, String value) {
        GrayMarkAssist.addRequestHeader(request, name, value);
    }

    @Override
    protected void restFixedUrls(HttpServletRequest request, HttpServletResponse response, String name, Set<String> fixedUrls) {
        GrayMarkAssist.resetResponseCookie(response, name, fixedUrls);
    }

    @Override
    public String getIp(HttpServletRequest request) {
        return WebUtil.getIp(request);
    }

    @Override
    public String getVersion(HttpServletRequest request) {
        return request.getHeader(ApiCoexist.VERSION_NAME);
    }

    @Override
    public String getUrl(HttpServletRequest request) {
        return WebUtil.getUrl(request);
    }

    @Override
    public String getToken(HttpServletRequest request) {
        return request.getHeader("token");
    }

    @Override
    public Set<String> getFixedUrls(HttpServletRequest request) {
        return GrayMarkAssist.getCookieUrls(request);
    }
}
