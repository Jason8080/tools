package cn.gmlee.tools.datalog.interceptor;

import cn.gmlee.tools.datalog.model.Datalog;

import java.util.List;

/**
 * 数据变更日志处理器
 *
 * @param <LOG> the type parameter
 * @author Jas °
 * @date 2021 /4/6 (周二)
 */
public interface DatalogInterceptorHandler<LOG extends Datalog> {
    /**
     * Commit boolean.
     *
     * @param logs the logs
     * @return the boolean
     */
    default boolean commit(List<LOG> logs){
        return true;
    }

    /**
     * Rollback boolean.
     *
     * @param logs the logs
     * @return the boolean
     */
    default boolean rollback(List<LOG> logs){
        return true;
    }
}
