package cn.gmlee.tools.mybatis.config.plus;

import cn.gmlee.tools.base.util.NullUtil;
import cn.gmlee.tools.mybatis.assist.ConfigurationAssist;
import cn.gmlee.tools.mybatis.assist.LocalResourcesAssist;
import com.baomidou.mybatisplus.autoconfigure.MybatisPlusProperties;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;
import java.util.List;

/**
 * MybatisPlus数据源配置.
 * <p>
 * 有MybatisSqlSessionFactoryBean相关jar包并且没有被覆盖则生效
 * 将覆盖SpringBoot自动装配 {@link com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration}
 * </p>
 *
 * @author Jas°
 * @Date 2020 /8/20 16:12
 */
@MapperScan({"**.dao.mapper.**"})
@EnableConfigurationProperties(MybatisPlusProperties.class)
@ConditionalOnClass({MybatisSqlSessionFactoryBean.class, MybatisPlusProperties.class})
public class MyBatisPlusConfiguration {

    @Value("${tools.mysql.dynamic.mapperPackageName:mapper}")
    private String[] mapperPackageName;

    @javax.annotation.Resource
    private MybatisPlusProperties mybatisPlusProperties;

    @Autowired(required = false)
    private List<Interceptor> interceptors;

    @Bean(name = "sqlSessionFactory")
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
        ConfigurationAssist.log(this.getClass());
        MybatisSqlSessionFactoryBean sqlSessionFactory = new MybatisSqlSessionFactoryBean();
        sqlSessionFactory.setDataSource(dataSource);
        // 映射器
        Resource[] resources = LocalResourcesAssist.getPackagesResource(mapperPackageName);
        sqlSessionFactory.setMapperLocations(LocalResourcesAssist.getFileSystemResource(resources));
        MybatisConfiguration configuration = getMybatisConfigurationBoot(sqlSessionFactory);
        // 注入mybatis拦截器(保证dataAuthInterceptor插件在前面执行: 所以下面的顺序不能错)
        ConfigurationAssist.addPagination(configuration, interceptors);
        ConfigurationAssist.addInterceptor(configuration, interceptors);
        return sqlSessionFactory.getObject();
    }

    private MybatisConfiguration getMybatisConfigurationBoot(MybatisSqlSessionFactoryBean sqlSessionFactoryBean) {
        GlobalConfig globalConfig = mybatisPlusProperties.getGlobalConfig();
        MybatisConfiguration configuration = NullUtil.get(mybatisPlusProperties.getConfiguration(), MybatisConfiguration.class);
        ConfigurationAssist.addSetting(configuration);
        sqlSessionFactoryBean.setGlobalConfig(globalConfig);
        sqlSessionFactoryBean.setConfiguration(configuration);
        return configuration;
    }
}
