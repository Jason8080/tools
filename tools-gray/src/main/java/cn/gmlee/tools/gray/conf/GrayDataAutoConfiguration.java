package cn.gmlee.tools.gray.conf;

import cn.gmlee.tools.gray.filter.GrayFilter;
import cn.gmlee.tools.gray.initializer.GrayDataInitializer;
import cn.gmlee.tools.gray.initializer.GrayDataInitializerTemplate;
import cn.gmlee.tools.gray.initializer.MysqlGrayDataInitializer;
import cn.gmlee.tools.gray.initializer.OracleGrayDataInitializer;
import cn.gmlee.tools.gray.interceptor.GrayInsertMarkInterceptor;
import cn.gmlee.tools.gray.interceptor.GraySelectFilterInterceptor;
import cn.gmlee.tools.gray.server.GrayServer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;

/**
 * 数据灰度自动装配类.
 */
@EnableConfigurationProperties(GrayProperties.class)
public class GrayDataAutoConfiguration {

    private final DataSource dataSource;

    /**
     * Instantiates a new Gray data auto configuration.
     *
     * @param dataSource the data source
     */
    public GrayDataAutoConfiguration(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Mysql gray data initializer mysql gray data initializer.
     *
     * @return the mysql gray data initializer
     */
    @Bean
    @ConditionalOnMissingBean(MysqlGrayDataInitializer.class)
    public MysqlGrayDataInitializer mysqlGrayDataInitializer(){
        return new MysqlGrayDataInitializer();
    }

    /**
     * Oracle gray data initializer oracle gray data initializer.
     *
     * @return the oracle gray data initializer
     */
    @Bean
    @ConditionalOnMissingBean(OracleGrayDataInitializer.class)
    public OracleGrayDataInitializer oracleGrayDataInitializer(){
        return new OracleGrayDataInitializer();
    }

    /**
     * 灰度初始化模版.
     *
     * @param initializers the initializers
     * @param properties   the properties
     * @return the gray data initializer template
     * @throws SQLException the sql exception
     */
    @Bean
    @ConditionalOnBean(GrayDataInitializer.class)
    public GrayDataInitializerTemplate grayDataInitializerTemplate(List<GrayDataInitializer> initializers, GrayProperties properties) throws SQLException {
        GrayDataInitializerTemplate template = new GrayDataInitializerTemplate(dataSource, properties);
        template.init(initializers.toArray(new GrayDataInitializer[0]));
        return template;
    }

    /**
     * Gray data interceptor gray data interceptor.
     *
     * @param properties the properties
     * @return the gray data interceptor
     * @throws SQLException the sql exception
     */
    @Bean
    @ConditionalOnMissingBean(GrayInsertMarkInterceptor.class)
    public GrayInsertMarkInterceptor grayDataInterceptor(GrayProperties properties) throws SQLException {
        return new GrayInsertMarkInterceptor(properties);
    }

    /**
     * Gray data interceptor gray select filter interceptor.
     *
     * @param properties the properties
     * @return the gray select filter interceptor
     * @throws SQLException the sql exception
     */
    @Bean
    @ConditionalOnMissingBean(GraySelectFilterInterceptor.class)
    public GraySelectFilterInterceptor graySelectFilterInterceptor(GrayProperties properties) throws SQLException {
        return new GraySelectFilterInterceptor(properties);
    }

    /**
     * Gray server gray server.
     *
     * @param properties the properties
     * @return the gray server
     */
    @Bean
    @ConditionalOnMissingBean(GrayServer.class)
    public GrayServer grayServer(GrayProperties properties){
        return new GrayServer(properties);
    }

    /**
     * Gray filter registration bean filter registration bean.
     *
     * @param grayServer the gray server
     * @return the filter registration bean
     */
    @Bean("FilterRegistrationBean-GrayFilter")
    @ConditionalOnMissingBean(name = "FilterRegistrationBean-GrayFilter")
    public FilterRegistrationBean<GrayFilter> grayFilterFilterRegistrationBean(GrayServer grayServer) {
        FilterRegistrationBean<GrayFilter> reg = new FilterRegistrationBean<>();
        reg.setFilter(new GrayFilter(grayServer));
        reg.addUrlPatterns("/*");
        reg.setName("grayFilter");
        return reg;
    }
}
