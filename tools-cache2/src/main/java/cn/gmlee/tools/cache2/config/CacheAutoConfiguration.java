package cn.gmlee.tools.cache2.config;


import cn.gmlee.tools.cache2.aspect.CacheAspect;
import cn.gmlee.tools.cache2.server.ds.DsServer;
import cn.gmlee.tools.cache2.server.ds.dao.mapper.MysqlMapper;
import cn.gmlee.tools.cache2.server.ds.mysql.ApiService;
import cn.gmlee.tools.cache2.server.ds.mysql.SqlService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@ConditionalOnBean(CacheAspect.class)
public class CacheAutoConfiguration {

    @Bean
    @ConditionalOnBean(MysqlMapper.class)
    public DsServer dsSqlServer(MysqlMapper mysqlMapper) {
        SqlService dsServer = new SqlService();
        dsServer.setMysqlMapper(mysqlMapper);
        return dsServer;
    }

    @Bean
    @ConditionalOnBean(RestTemplate.class)
    public DsServer dsApiServer(RestTemplate restTemplate) {
        ApiService dsServer = new ApiService();
        dsServer.setRestTemplate(restTemplate);
        return dsServer;
    }
}
