package cn.gmlee.tools.webapp.filter;

import cn.gmlee.tools.base.enums.XCode;
import cn.gmlee.tools.base.ex.SkillException;
import cn.gmlee.tools.base.mod.R;
import cn.gmlee.tools.base.util.BoolUtil;
import cn.gmlee.tools.base.util.JsonUtil;
import cn.gmlee.tools.base.util.VcUtil;
import cn.gmlee.tools.base.util.WebUtil;
import cn.gmlee.tools.webapp.config.vc.VcProperties;
import cn.gmlee.tools.webapp.service.VcService;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;

/**
 * 验证码过滤器.
 *
 * @author Jas
 */
public class VcFilter extends OncePerRequestFilter {

    private VcService vcService;
    private VcProperties vcProperties;

    public VcFilter(VcService vcService) {
        this.vcService = vcService;
        this.vcProperties = vcService.getVcProperties();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            Map<String, String> headerMap = WebUtil.getHeaderMap(request);
            String vcId = headerMap.get(vcProperties.getIdHeader());
            String str = headerMap.get(vcProperties.getVcHeader());
            if (BoolUtil.allNotEmpty(vcId, str)) {
                // 真实的验证码内容
                if (str.equalsIgnoreCase(vcService.getVc(vcId))) {
                    // 验证通过
                    VcUtil.setCheckResult(true);
                } else {
                    // 验证不通过
                    VcUtil.setCheckResult(false);
                }
            }
        } catch (SkillException e) {
            response.setCharacterEncoding(Charset.defaultCharset().name());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            R result = new R(XCode.FAIL.code, e.getMessage());
            response.getWriter().println(JsonUtil.toJson(result));
            return;
        }
        try {
            filterChain.doFilter(request, response);
            // catch 无效: 被ControllerAdvice提前捕捉了, so, 别写了
        } finally {
            VcUtil.remove();
        }
    }
}
