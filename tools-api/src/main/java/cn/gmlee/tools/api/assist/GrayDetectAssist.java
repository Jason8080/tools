package cn.gmlee.tools.api.assist;

import cn.gmlee.tools.api.coexist.ApiCoexistRequestMappingHandlerMapping;
import org.springframework.context.ApplicationContext;
import org.springframework.web.method.HandlerMethod;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

/**
 * 通用灰度Api侦查辅助工具
 *
 * @author Jas°
 * @date 2020 /12/2 (周三)
 */
public class GrayDetectAssist {

    /**
     * 判断请求是否存在.
     * <p>
     * 灰度资源不存在时将旧版资源默认为灰度资源
     * </p>
     *
     * @param applicationContext the application context
     * @param request            the request
     * @return the boolean
     */
    public static boolean exist(ApplicationContext applicationContext, HttpServletRequest request) {
        try {
            if (Objects.nonNull(applicationContext)) {
                ApiCoexistRequestMappingHandlerMapping handlerMapping = applicationContext.getBean(ApiCoexistRequestMappingHandlerMapping.class);
                if (Objects.nonNull(handlerMapping)) {
                    HandlerMethod handlerMethod = handlerMapping.getHandlerInternal(request);
                    return Objects.nonNull(handlerMethod);
                }
            }
        } catch (Exception e) {
        }
        return false;
    }
}
