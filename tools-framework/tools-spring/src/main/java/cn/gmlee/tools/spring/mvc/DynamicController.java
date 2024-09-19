package cn.gmlee.tools.spring.mvc;

/**
 * 动态控制器定义.
 *
 * @param <P> the type parameter
 * @param <R> the type parameter
 */
public interface DynamicController<P, R> {

    /**
     * 动态接口定义.
     *
     * @param p 动态参数定义
     * @return 动态相应定义
     */
    R handle(P p);

}
