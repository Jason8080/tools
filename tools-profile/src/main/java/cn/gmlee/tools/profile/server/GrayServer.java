package cn.gmlee.tools.profile.server;

import cn.gmlee.tools.base.util.UrlUtil;
import cn.gmlee.tools.base.util.WebUtil;
import cn.gmlee.tools.profile.conf.GrayProperties;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

/**
 * The type Gray server.
 */
public class GrayServer {

    private final GrayProperties properties;

    /**
     * Instantiates a new Gray server.
     *
     * @param properties the properties
     */
    public GrayServer(GrayProperties properties) {
        this.properties = properties;
    }

    /**
     * Check boolean.
     *
     * @param request the request
     * @return the boolean
     */
    public Boolean check(ServletRequest request) {
        if (!(request instanceof HttpServletRequest)) {
            return true;
        }
        String url = WebUtil.getUrl((HttpServletRequest) request);
        return UrlUtil.matchOne(properties.getExcludes(), url);
    }
}
