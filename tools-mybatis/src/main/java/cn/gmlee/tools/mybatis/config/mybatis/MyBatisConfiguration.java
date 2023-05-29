package cn.gmlee.tools.mybatis.config.mybatis;

import cn.gmlee.tools.base.util.NullUtil;
import cn.gmlee.tools.mybatis.assist.ConfigurationAssist;
import cn.gmlee.tools.mybatis.assist.LocalResourcesAssist;
import cn.gmlee.tools.mybatis.config.plus.MyBatisPlusConfiguration;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.mybatis.spring.boot.autoconfigure.MybatisProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;
import java.util.List;

/**
 * Mybatis数据源配置.
 * <p>
 * MyBatisPlusConfig不存在并且没有人覆盖这个数据源配置才生效
 * 将覆盖SpringBoot自动装配 {@link org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration}
 * </p>
 *
 * @author Jas°
 * @Date 2020 /8/20 16:12
 */
@ConditionalOnMissingBean({MyBatisPlusConfiguration.class})
@MapperScan({"**.dao.mapper.**"})
@ConditionalOnClass({MybatisProperties.class})
@EnableConfigurationProperties(MybatisProperties.class)
public class MyBatisConfiguration {

    @Value("${tools.mysql.dynamic.mapperPackageName:mapper}")
    private String mapperPackageName;

    @javax.annotation.Resource
    private MybatisProperties mybatisProperties;

    @SuppressWarnings("all")
    @Autowired(required = false)
    private List<Interceptor> interceptors;

    @Bean
    public SqlSessionFactory sqlSessionFactoryBean(DataSource dataSource) throws Exception {
        ConfigurationAssist.log(this.getClass());
        SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
        sessionFactory.setDataSource(dataSource);
        // 映射器
        Resource[] resources = new PathMatchingResourcePatternResolver().getResources("classpath*:/**/" + mapperPackageName + "/**/*.xml");
        sessionFactory.setMapperLocations(LocalResourcesAssist.getFileSystemResource(resources));
        Configuration configuration = getMybatisConfigurationBoot(sessionFactory);
        // 注入mybatis拦截器(保证dataAuthInterceptor插件在前面执行: 所以需要确保分页插件在后面)
        ConfigurationAssist.addInterceptor(configuration, interceptors);
        return sessionFactory.getObject();
    }

    private Configuration getMybatisConfigurationBoot(SqlSessionFactoryBean sessionFactoryBean) {
        Configuration configuration = NullUtil.get(mybatisProperties.getConfiguration(), Configuration.class);
        ConfigurationAssist.addSetting(configuration);
        sessionFactoryBean.setConfiguration(configuration);
        return configuration;
    }

}
