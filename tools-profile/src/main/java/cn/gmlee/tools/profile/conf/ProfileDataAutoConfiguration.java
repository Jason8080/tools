package cn.gmlee.tools.profile.conf;

import cn.gmlee.tools.profile.initializer.GrayDataInitializer;
import cn.gmlee.tools.profile.initializer.GrayDataTemplate;
import cn.gmlee.tools.profile.initializer.MysqlGrayDataInitializer;
import cn.gmlee.tools.profile.initializer.OracleGrayDataInitializer;
import cn.gmlee.tools.profile.interceptor.ProfileInsertMarkInterceptor;
import cn.gmlee.tools.profile.interceptor.ProfileSelectFilterInterceptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;

/**
 * 数据隔离自动装配类.
 */
@EnableConfigurationProperties(ProfileProperties.class)
public class ProfileDataAutoConfiguration {

    private final DataSource dataSource;

    /**
     * Instantiates a new Gray data auto configuration.
     *
     * @param dataSource the data source
     */
    public ProfileDataAutoConfiguration(DataSource dataSource) {
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
    public GrayDataTemplate grayDataInitializerTemplate(List<GrayDataInitializer> initializers, ProfileProperties properties) throws SQLException {
        GrayDataTemplate template = new GrayDataTemplate(dataSource, properties);
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
    @ConditionalOnMissingBean(ProfileInsertMarkInterceptor.class)
    public ProfileInsertMarkInterceptor grayDataInterceptor(ProfileProperties properties) {
        return new ProfileInsertMarkInterceptor(properties);
    }

    /**
     * Gray data interceptor gray select filter interceptor.
     *
     * @param properties the properties
     * @return the gray select filter interceptor
     * @throws SQLException the sql exception
     */
    @Bean
    @ConditionalOnMissingBean(ProfileSelectFilterInterceptor.class)
    public ProfileSelectFilterInterceptor graySelectFilterInterceptor(ProfileProperties properties) {
        return new ProfileSelectFilterInterceptor(properties);
    }
}
