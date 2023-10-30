package cn.gmlee.tools.cache2.config;


import cn.gmlee.tools.base.util.HttpUtil;
import cn.gmlee.tools.cache2.server.ds.DsServer;
import cn.gmlee.tools.cache2.server.ds.dao.mapper.SqlMapper;
import cn.gmlee.tools.cache2.server.ds.mysql.ApiServer;
import cn.gmlee.tools.cache2.server.ds.mysql.SqlServer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;

public class DsCacheServerAutoConfiguration {

    @Bean
    @ConditionalOnClass(SqlMapper.class)
    public DsServer dsSqlServer() {
        return new SqlServer();
    }

    @Bean
    @ConditionalOnClass(HttpUtil.class)
    public DsServer dsApiServer() {
        return new ApiServer();
    }

}
