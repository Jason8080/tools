package cn.gmlee.tools.webapp.filter;

import cn.gmlee.tools.base.enums.XCode;
import cn.gmlee.tools.base.ex.SkillException;
import cn.gmlee.tools.base.mod.R;
import cn.gmlee.tools.base.util.BoolUtil;
import cn.gmlee.tools.base.util.JsonUtil;
import cn.gmlee.tools.base.util.LoginUtil;
import cn.gmlee.tools.webapp.service.LoginServer;
import org.springframework.http.MediaType;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * 复用流
 *
 * @author Jas °
 * @date 2020 /8/28 (周五)
 */
public class AuthFilter extends OncePerRequestFilter {

    private static AntPathMatcher matcher = new AntPathMatcher();

    private LoginServer loginServer;

    private String[] urlExcludes;

    /**
     * 登陆服务过滤器.
     *
     * @param loginServer 登录服务
     * @param urlExcludes 排除的接口
     */
    public AuthFilter(LoginServer loginServer, String... urlExcludes) {
        this.loginServer = loginServer;
        this.urlExcludes = urlExcludes;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            if (!excludes(request, urlExcludes)) {
                loginServer.add(request);
            }
        } catch (SkillException e) {
            response.setCharacterEncoding(Charset.defaultCharset().name());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            R result = new R(XCode.LOGIN_TIMEOUT.code, e.getMessage());
            response.getWriter().println(JsonUtil.toJson(result));
            return;
        }
        try {
            filterChain.doFilter(request, response);
        } finally {
            LoginUtil.remove();
        }
    }

    private boolean excludes(HttpServletRequest request, String... urlExcludes) {
        if (BoolUtil.notEmpty(urlExcludes)) {
            String servletPath = request.getServletPath();
            for (String pattern : urlExcludes) {
                boolean match = matcher.match(pattern, servletPath);
                if (match) {
                    return true;
                }
            }
        }
        return false;
    }
}
