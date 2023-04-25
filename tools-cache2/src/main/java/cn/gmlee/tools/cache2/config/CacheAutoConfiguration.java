package cn.gmlee.tools.cache2.config;


import cn.gmlee.tools.cache2.aspect.CacheAspect;
import cn.gmlee.tools.cache2.server.ds.DsServer;
import cn.gmlee.tools.cache2.server.ds.dao.mapper.MysqlMapper;
import cn.gmlee.tools.cache2.server.ds.mysql.MysqlService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@ConditionalOnBean(CacheAspect.class)
public class CacheAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(MysqlMapper.class)
    public DsServer dsServer(MysqlMapper mysqlMapper) {
        MysqlService dsServer = new MysqlService();
        dsServer.setMysqlMapper(mysqlMapper);
        return dsServer;
    }
}
