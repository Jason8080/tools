package cn.gmlee.tools.mate.config;

import cn.gmlee.tools.mate.aop.DataAuthAspect;
import cn.gmlee.tools.mate.interceptor.CodecServer;
import cn.gmlee.tools.mate.interceptor.DataAuthInterceptor;
import cn.gmlee.tools.mate.interceptor.DataAuthServer;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

/**
 * 数据权限装配.
 *
 * @author Jas °
 * @date 2021 /4/2 (周五)
 */
@Import({DataAuthAspect.class, DataAuthInterceptor.class,})
@ConditionalOnClass({SqlSessionFactory.class, Interceptor.class})
public class DataAuthAutoConfiguration {

    public DataAuthAutoConfiguration() {
    }

    @Bean
    @ConditionalOnMissingBean(DataAuthServer.class)
    public DataAuthServer dataAuthServer(){
        return new DataAuthServer() {};
    }

    @Bean
    @ConditionalOnMissingBean(CodecServer.class)
    public CodecServer codecServer(){
        return new CodecServer() {};
    }
}
