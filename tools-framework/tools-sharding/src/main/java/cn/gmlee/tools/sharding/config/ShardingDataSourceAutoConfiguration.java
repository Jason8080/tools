package cn.gmlee.tools.sharding.config;

import cn.gmlee.tools.ds.assist.DatasourceAssist;
import cn.gmlee.tools.ds.config.dynamic.DynamicDatasourceAutoConfiguration;
import cn.gmlee.tools.sharding.assist.ShardingConfigAssist;
import cn.gmlee.tools.sharding.ds.ShardingDynamicDataSource;
import cn.gmlee.tools.sharding.server.ShardingServer;
import cn.gmlee.tools.sharding.entity.ShardingConfig;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 分片配置类.
 *
 * @author Jas °
 * @date 2021 /6/10 (周四)
 */
@AutoConfigureBefore({DynamicDatasourceAutoConfiguration.class})
@EnableConfigurationProperties({ShardingConfigProperties.class, DataSourceProperties.class})
public class ShardingDataSourceAutoConfiguration implements DynamicDatasourceAutoConfiguration.OverrideDataSourceConfiguration {

    private static final Logger log = LoggerFactory.getLogger(ShardingDataSourceAutoConfiguration.class);

    @Resource
    @Getter
    private ShardingConfigProperties configProperties;

    @Resource
    @Getter
    private DataSourceProperties dataSourceProperties;

    /**
     * 基础配置专用数据源.
     * <p>
     * 该数据源
     * </p>
     */
    @Getter
    private final DataSource dataSource;

    public ShardingDataSourceAutoConfiguration(DataSourceProperties dataSourceProperties) {
        this.dataSource = DatasourceAssist.createDataSource(dataSourceProperties.getDatasource().getCof());
    }

    @Bean
    @ConditionalOnProperty(value = "tools.sharding.enable", havingValue = "true", matchIfMissing = true)
    public ShardingServer shardingServer() {
        return new ShardingServer(this.dataSource, configProperties);
    }

    @Bean
    @ConditionalOnProperty(value = "tools.sharding.enable", havingValue = "true", matchIfMissing = true)
    public DataSource toolsShardingSphereDataSource() throws Exception {
        List<ShardingConfig> shardingConfigs = ShardingConfigAssist.getShardingConfigs(dataSource, configProperties.getSysId());
        // 创建动态数据源
        Map<Object, Object> targetDataSource = new HashMap();
        ShardingDynamicDataSource dynamicDataSource = new ShardingDynamicDataSource(targetDataSource);
        dynamicDataSource.setTargetDataSources(targetDataSource);
        // 创建分片数据源
        DataSource dataSource = ShardingConfigAssist.createShardingDataSource(
                configProperties, dataSourceProperties,
                shardingConfigs, dynamicDataSource.getDataSourceName()
        );
        // 保存分片数据源
        dynamicDataSource.cutDataSource(dataSource);
        return dynamicDataSource;
    }
}
