package cn.gmlee.tools.gray.filter;


import cn.gmlee.tools.gray.helper.GrayHelper;
import cn.gmlee.tools.gray.server.GrayServer;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 加解密过滤器.
 *
 * @author Jas
 */
public class GrayFilter implements Filter {

    private final GrayServer grayServer;

    public GrayFilter(GrayServer grayServer) {
        this.grayServer = grayServer;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        // 是否开启灰度
        try {
            GrayHelper.enable(grayServer.check(request));
        } finally {
            try {
                // 失败仍然发起请求: 非灰度
                chain.doFilter(request, response);
            } finally {
                GrayHelper.clear();
            }
        }
    }

    @Override
    public void destroy() {

    }
}
