package cn.gmlee.tools.mate.config;

import cn.gmlee.tools.mate.aop.DataAuthAspect;
import cn.gmlee.tools.mate.interceptor.CodecServer;
import cn.gmlee.tools.mate.interceptor.DataAuthInterceptor;
import cn.gmlee.tools.mybatis.config.mybatis.MyBatisConfiguration;
import cn.gmlee.tools.mybatis.config.plus.MyBatisPlusConfiguration;
import cn.gmlee.tools.mybatis.dao.IBatisDao;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

/**
 * The type Datalog auto configuration.
 *
 * @author Jas °
 * @date 2021 /4/2 (周五)
 */
@Import({DataAuthAspect.class, DataAuthInterceptor.class,})
@AutoConfigureAfter({MyBatisPlusConfiguration.class, MyBatisConfiguration.class})
@ConditionalOnClass({SqlSessionFactory.class, Interceptor.class, IBatisDao.class})
public class DataAuthAutoConfiguration {

    public DataAuthAutoConfiguration() {
    }

    @Bean
    @ConditionalOnMissingBean(CodecServer.class)
    public CodecServer codecServer(){
        return new CodecServer() {};
    }
}
