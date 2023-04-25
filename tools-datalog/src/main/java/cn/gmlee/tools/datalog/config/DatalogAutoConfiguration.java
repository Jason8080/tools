package cn.gmlee.tools.datalog.config;

import cn.gmlee.tools.base.util.ExceptionUtil;
import cn.gmlee.tools.datalog.aop.DatalogAspect;
import cn.gmlee.tools.datalog.interceptor.DatalogInterceptorHandler;
import cn.gmlee.tools.datalog.interceptor.AbstractDatalogInterceptorHandler;
import cn.gmlee.tools.datalog.interceptor.DatalogInterceptor;
import cn.gmlee.tools.datalog.interceptor.DefaultDatalogInterceptorHandler;
import cn.gmlee.tools.mybatis.config.mybatis.MyBatisConfiguration;
import cn.gmlee.tools.mybatis.config.plus.MyBatisPlusConfiguration;
import cn.gmlee.tools.mybatis.dao.IBatisDao;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import javax.sql.DataSource;
import java.util.Map;

/**
 * The type Datalog auto configuration.
 *
 * @author Jas °
 * @date 2021 /4/2 (周五)
 */
@Import({
        DatalogAspect.class,
        DatalogInterceptor.class,
})
@AutoConfigureAfter({MyBatisPlusConfiguration.class, MyBatisConfiguration.class})
@ConditionalOnClass({SqlSessionFactory.class, Interceptor.class, IBatisDao.class})
public class DatalogAutoConfiguration {
    /**
     * Instantiates a new Datalog auto configuration.
     *
     * @param sqlSessionFactory  the sql session factory
     * @param datalogInterceptor the update datalog interceptor
     */
    public DatalogAutoConfiguration(
            SqlSessionFactory sqlSessionFactory,
            DatalogInterceptor datalogInterceptor
    ) {
        IBatisDao<Map> iBatisDao = new IBatisDao(sqlSessionFactory);
        datalogInterceptor.setIBatisDao(iBatisDao);
    }

    @Bean("customAbstractDatalogInterceptorHandler")
    @ConditionalOnBean(DatalogInterceptorHandler.class)
    public DatalogInterceptorHandler datalogInterceptorHandler(
            DatalogInterceptor datalogInterceptor,
            DatalogInterceptorHandler datalogInterceptorHandler
    ) {
        if (datalogInterceptorHandler instanceof AbstractDatalogInterceptorHandler) {
            ((AbstractDatalogInterceptorHandler) datalogInterceptorHandler).setIBatisDao(datalogInterceptor.getIBatisDao());
            datalogInterceptor.setAbstractDatalogInterceptorHandler((AbstractDatalogInterceptorHandler) datalogInterceptorHandler);
            return datalogInterceptorHandler;
        }
        return ExceptionUtil.cast(String.format("请继承抽象类: %s", AbstractDatalogInterceptorHandler.class.getName()));
    }

    @Bean
    @ConditionalOnMissingBean(DatalogInterceptorHandler.class)
    public DefaultDatalogInterceptorHandler defaultDatalogInterceptorHandler(
            DatalogInterceptor datalogInterceptor, DataSource dataSource
    ) {
        DefaultDatalogInterceptorHandler defaultDatalogInterceptorHandler = new DefaultDatalogInterceptorHandler(dataSource);
        defaultDatalogInterceptorHandler.setIBatisDao(datalogInterceptor.getIBatisDao());
        datalogInterceptor.setAbstractDatalogInterceptorHandler(defaultDatalogInterceptorHandler);
        return defaultDatalogInterceptorHandler;
    }
}
