package cn.gmlee.tools.seata.config;

import cn.gmlee.tools.base.enums.XCode;
import cn.gmlee.tools.base.ex.SkillException;
import cn.gmlee.tools.ds.assist.DatasourceAssist;
import cn.gmlee.tools.ds.config.dynamic.DynamicDataSourceProperties;
import cn.gmlee.tools.ds.config.dynamic.DynamicDatasourceAutoConfiguration;
import cn.gmlee.tools.ds.dynamic.DynamicDataSource;
import cn.gmlee.tools.ds.dynamic.DynamicDataSourceHolder;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import io.seata.rm.datasource.DataSourceProxy;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertyName;
import org.springframework.boot.context.properties.source.ConfigurationPropertySource;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * 重写动态数据源
 *
 * @author Jas°
 * @date 2020/10/21 (周三)
 */
@Configuration
@Import(DynamicDataSourceProperties.class)
@ConditionalOnClass({DynamicDatasourceAutoConfiguration.OverrideDataSourceConfiguration.class, MybatisSqlSessionFactoryBean.class, DataSourceProxy.class})
public class SeataDynamicDataSourceConfiguration implements DynamicDatasourceAutoConfiguration.OverrideDataSourceConfiguration {

    @Bean("dynamicDataSource")
    public DataSource dataSource(DynamicDataSourceProperties dynamicDataSourceProperties) {
        Map<String, Map<String, String>> dsMap = dynamicDataSourceProperties.getDatasource();
        if (!dsMap.isEmpty()) {
            // 加载数据源
            Map<Object, Object> targetDataSource = new HashMap(dsMap.size());
            dsMap.forEach((String name, Map<String, String> map) -> {
                ConfigurationPropertySource source = new MapConfigurationPropertySource(map);
                Binder binder = new Binder(source);
                // 通过类型绑定参数并获得实例对象
                DataSource targetDs = binder.bind(ConfigurationPropertyName.EMPTY, Bindable.of(DatasourceAssist.getDatasourceType(map))).get();
                targetDataSource.put(name, new DataSourceProxy(targetDs));
                DynamicDataSourceHolder.addDatasourceKeys(name);
            });
            DynamicDataSource dynamicDataSource = new DynamicDataSource();
            dynamicDataSource.setTargetDataSources(targetDataSource);
            return dynamicDataSource;
        }
        throw new SkillException(XCode.THIRD_PARTY3000.code, "至少需要 1 个数据源");
    }
}
