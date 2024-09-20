package cn.gmlee.tools.spring.mvc;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 动态控制器定义.
 *
 * @param <P> the type parameter
 * @param <R> the type parameter
 */
@FunctionalInterface
public interface DynamicJsonController<P, R> {

    /**
     * 动态接口定义.
     *
     * @param p 动态参数定义
     * @return 动态相应定义
     */
    @ResponseBody
    R handle(@RequestBody P p);

}
