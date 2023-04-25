package cn.gmlee.tools.api.config;

import cn.gmlee.tools.api.anno.ApiCoexist;
import cn.gmlee.tools.base.util.NullUtil;
import cn.gmlee.tools.base.util.WebUtil;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import feign.Target;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.util.StringUtils;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

/**
 * 多版本接口共存全线灰度自动装配
 *
 * @author Jas°
 * @date 2020/12/25 (周五)
 */
@ConditionalOnClass({RequestInterceptor.class, RequestTemplate.class, ServletRequest.class})
public class ApiCoexistFeignLineGrayAutoConfiguration implements RequestInterceptor {
    private final Logger logger = LoggerFactory.getLogger(ApiCoexistFeignLineGrayAutoConfiguration.class);

    @Override
    public void apply(RequestTemplate requestTemplate) {
        HttpServletRequest request = WebUtil.getRequest();
        String version = request != null ? request.getHeader(ApiCoexist.VERSION_NAME) : null;
        if (!StringUtils.isEmpty(version)) {
            requestTemplate.header(ApiCoexist.VERSION_NAME, version);
            Target<?> target = requestTemplate.feignTarget();
            String url = NullUtil.get(target.url()) + requestTemplate.url();
            logger.debug("已启用全链路灰度过滤器: {}", url);
        }
    }
}
