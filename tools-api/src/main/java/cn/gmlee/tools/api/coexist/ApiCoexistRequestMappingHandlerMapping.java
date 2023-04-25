package cn.gmlee.tools.api.coexist;

import cn.gmlee.tools.api.anno.ApiCoexist;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.condition.RequestCondition;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

/**
 * 过滤器配置
 *
 * @author Jas°
 * @date 2020/8/28 (周五)
 */
public class ApiCoexistRequestMappingHandlerMapping extends RequestMappingHandlerMapping {
    @Override
    protected RequestCondition<ApiCoexistCondition> getCustomTypeCondition(Class<?> handlerType) {
        ApiCoexist apiCoexist = AnnotationUtils.findAnnotation(handlerType, ApiCoexist.class);
        return createCondition(apiCoexist);
    }

    @Override
    protected RequestCondition<ApiCoexistCondition> getCustomMethodCondition(Method method) {
        ApiCoexist apiCoexist = AnnotationUtils.findAnnotation(method, ApiCoexist.class);
        return createCondition(apiCoexist);
    }

    private RequestCondition<ApiCoexistCondition> createCondition(ApiCoexist apiCoexist) {
        return apiCoexist == null ? null : new ApiCoexistCondition(apiCoexist.value());
    }

    @Override
    public HandlerMethod getHandlerInternal(HttpServletRequest request) throws Exception {
        return super.getHandlerInternal(request);
    }
}
