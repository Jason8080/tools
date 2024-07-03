package cn.gmlee.tools.profile.conf;

import cn.gmlee.tools.base.util.BoolUtil;
import cn.gmlee.tools.profile.initializer.MysqlProfileDataInitializer;
import cn.gmlee.tools.profile.initializer.OracleProfileDataInitializer;
import cn.gmlee.tools.profile.initializer.ProfileDataInitializer;
import cn.gmlee.tools.profile.initializer.ProfileDataTemplate;
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
    @ConditionalOnMissingBean(MysqlProfileDataInitializer.class)
    public MysqlProfileDataInitializer mysqlProfileDataInitializer(){
        return new MysqlProfileDataInitializer();
    }

    /**
     * Oracle gray data initializer oracle gray data initializer.
     *
     * @return the oracle gray data initializer
     */
    @Bean
    @ConditionalOnMissingBean(OracleProfileDataInitializer.class)
    public OracleProfileDataInitializer oracleProfileDataInitializer(){
        return new OracleProfileDataInitializer();
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
    @ConditionalOnBean(ProfileDataInitializer.class)
    public ProfileDataTemplate profileDataTemplate(List<ProfileDataInitializer> initializers, ProfileProperties... properties) throws SQLException {
        ProfileProperties prop = BoolUtil.isEmpty(properties) ? new ProfileProperties() : properties[0];
        ProfileDataTemplate template = new ProfileDataTemplate(dataSource, prop);
        template.init(initializers.toArray(new ProfileDataInitializer[0]));
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
    public ProfileInsertMarkInterceptor profileInsertMarkInterceptor(ProfileProperties... properties) {
        return new ProfileInsertMarkInterceptor();
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
    public ProfileSelectFilterInterceptor profileSelectFilterInterceptor(ProfileProperties... properties) {
        return new ProfileSelectFilterInterceptor();
    }
}
