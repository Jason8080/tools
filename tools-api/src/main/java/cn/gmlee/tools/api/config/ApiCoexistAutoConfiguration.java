package cn.gmlee.tools.api.config;

import cn.gmlee.tools.api.coexist.ApiCoexistRequestMappingHandlerMapping;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcRegistrations;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/**
 * 多版本共存配置
 * 注意: 该配置与{@link org.springframework.web.servlet.config.annotation.EnableWebMvc}冲突
 *
 * @author Jas°
 * @date 2020/8/28 (周五)
 */
public class ApiCoexistAutoConfiguration implements WebMvcRegistrations {


    @Override
    public RequestMappingHandlerMapping getRequestMappingHandlerMapping() {
        return new ApiCoexistRequestMappingHandlerMapping();
    }

}
