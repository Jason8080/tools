package cn.gmlee.tools.api.once;

import cn.gmlee.tools.base.ex.agreed.UserExperienceException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 自定义Once处理器.
 * <p>
 * 在确认为重复请求时如何处理?
 * </p>
 *
 * @author Jas
 */
public interface OnceHandler {
    /**
     * 处理.
     *
     * @param request  请求对象
     * @param response 响应对象
     * @param args     接收参数
     * @return 处理结果 object
     */
    default Object handler(HttpServletRequest request, HttpServletResponse response, Object... args) {
        throw new UserExperienceException("请求频繁");
    }
}
