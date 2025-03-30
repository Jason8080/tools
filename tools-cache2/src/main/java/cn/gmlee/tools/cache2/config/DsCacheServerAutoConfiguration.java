package cn.gmlee.tools.cache2.config;


import cn.gmlee.tools.base.util.HttpUtil;
import cn.gmlee.tools.cache2.server.ds.DsServer;
import cn.gmlee.tools.cache2.server.ds.clazz.EnumServer;
import cn.gmlee.tools.cache2.server.ds.dao.mapper.SqlMapper;
import cn.gmlee.tools.cache2.server.ds.http.ApiServer;
import cn.gmlee.tools.cache2.server.ds.http.HttpServer;
import cn.gmlee.tools.cache2.server.ds.http.RestServer;
import cn.gmlee.tools.cache2.server.ds.mysql.DbServer;
import cn.gmlee.tools.cache2.server.ds.mysql.SqlServer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

public class DsCacheServerAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(DbServer.class)
    @ConditionalOnBean(SqlMapper.class)
    public DsServer dsSqlServer() {
        return new SqlServer();
    }

    @Bean
    @ConditionalOnMissingBean(HttpServer.class)
    @ConditionalOnBean(RestTemplate.class)
    public DsServer dsRestServer() {
        return new RestServer();
    }

    @Bean
    @ConditionalOnMissingBean(HttpServer.class)
    @ConditionalOnClass(HttpUtil.class)
    public DsServer dsApiServer() {
        return new ApiServer();
    }

    @Bean
    @ConditionalOnMissingBean(EnumServer.class)
    public EnumServer dsEnumServer() {
        return new EnumServer();
    }

}
