package cn.gmlee.tools.spring.mvc;

import javax.servlet.http.HttpServletRequest;

/**
 * 动态控制器定义.
 *
 * @param <R> the type parameter
 */
@FunctionalInterface
public interface DynamicController< R> {

    /**
     * 动态接口定义.
     *
     * @return 动态相应定义
     */
    R handle(HttpServletRequest request);

}
