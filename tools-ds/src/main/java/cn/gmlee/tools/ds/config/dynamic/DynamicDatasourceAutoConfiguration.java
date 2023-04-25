package cn.gmlee.tools.ds.config.dynamic;

import cn.gmlee.tools.ds.assist.DatasourceAssist;
import cn.gmlee.tools.ds.dynamic.DynamicDataSource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionManager;

import javax.sql.DataSource;

/**
 * @author Jas°
 * @date 2020/11/25 (周三)
 */
@EnableConfigurationProperties(DynamicDataSourceProperties.class)
@ConditionalOnMissingBean({DynamicDatasourceAutoConfiguration.OverrideDataSourceConfiguration.class})
public class DynamicDatasourceAutoConfiguration {

    public static final String DEFAULT_TX = "defaultTx";

    public interface OverrideDataSourceConfiguration {
    }

    @Bean(DEFAULT_TX)
    @ConditionalOnMissingBean(TransactionManager.class)
    public DataSourceTransactionManager transaction(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean
    public DynamicDataSource dataSource(DynamicDataSourceProperties dynamicDataSourceProperties) {
        return DatasourceAssist.createDynamicDataSource(dynamicDataSourceProperties.getDatasource(), dynamicDataSourceProperties.getDruid());
    }

}
