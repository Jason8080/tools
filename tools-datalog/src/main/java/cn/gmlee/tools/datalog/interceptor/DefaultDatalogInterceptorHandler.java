package cn.gmlee.tools.datalog.interceptor;

import cn.gmlee.tools.base.mod.Login;
import cn.gmlee.tools.datalog.model.Datalog;

import javax.sql.DataSource;

/**
 * 默认输出到控制台的实现
 *
 * @author Jas°
 * @date 2021/4/6 (周二)
 */
public class DefaultDatalogInterceptorHandler extends AbstractDatalogInterceptorHandler<Datalog> {

    /**
     * 请注意: 该数据源与业务数据源一致有风险
     */
    private DataSource dataSource;

    public DefaultDatalogInterceptorHandler(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    protected DataSource getDataSource() {
        return dataSource;
    }

    @Override
    protected Login getLogin() {
        return null;
    }
}
