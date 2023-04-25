package cn.gmlee.tools.sharding.config;

import cn.gmlee.tools.base.util.AssertUtil;
import cn.gmlee.tools.base.util.BoolUtil;
import cn.gmlee.tools.base.util.ExceptionUtil;
import cn.gmlee.tools.sharding.assist.MysqlShardingAssist;
import cn.gmlee.tools.sharding.assist.ShardingConfigAssist;
import cn.gmlee.tools.sharding.ds.ShardingDynamicDataSource;
import cn.gmlee.tools.sharding.entity.ShardingConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.scheduling.TriggerContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@EnableScheduling
@AutoConfigureAfter(ShardingDataSourceAutoConfiguration.class)
public class ShardingTimingOverloadAutoConfiguration implements SchedulingConfigurer {

    private static final Logger logger = LoggerFactory.getLogger(ShardingTimingOverloadAutoConfiguration.class);

    @Resource
    private ShardingDataSourceAutoConfiguration shardingDataSourceAutoConfiguration;

    @Resource
    private ShardingDynamicDataSource dataSource;

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        ShardingConfigProperties configProperties = shardingDataSourceAutoConfiguration.getConfigProperties();
        // 观察数据量
        taskRegistrar.addTriggerTask(() -> ExceptionUtil.sandbox(() -> dataSizeObserver()),
                (TriggerContext triggerContext) -> {
                    CronTrigger cronTrigger = new CronTrigger(configProperties.getCron().getObserveDataSize());
                    Date date = cronTrigger.nextExecutionTime(triggerContext);
                    return date;
                }
        );
        // 切换数据源
        taskRegistrar.addTriggerTask(() -> ExceptionUtil.sandbox(() -> cutShardingDataSource()),
                (TriggerContext triggerContext) -> {
                    CronTrigger cronTrigger = new CronTrigger(configProperties.getCron().getCutDataSource());
                    Date date = cronTrigger.nextExecutionTime(triggerContext);
                    return date;
                }
        );
    }

    private void cutShardingDataSource() throws SQLException {
        ShardingConfigProperties configProperties = shardingDataSourceAutoConfiguration.getConfigProperties();
        DataSourceProperties dataSourceProperties = shardingDataSourceAutoConfiguration.getDataSourceProperties();
        logger.info("系统{}正在切换数据源...", configProperties.getSysId());
        List<ShardingConfig> shardingConfigs = ShardingConfigAssist.getShardingConfigs(
                shardingDataSourceAutoConfiguration.getDataSource(), configProperties.getSysId()
        );
        if (BoolUtil.isEmpty(shardingConfigs)) {
            return;
        }
        // 检查是否存在刷新的规则
        Optional<ShardingConfig> any = shardingConfigs.stream()
                .filter(ShardingConfigAssist::checkCurrentTableRuleUpdateAt).findAny();
        if (!any.isPresent()) {
            return;
        }
        // 创建新数据源并进行切换
        DataSource dataSource = ShardingConfigAssist.createShardingDataSource(
                configProperties, dataSourceProperties, shardingConfigs, this.dataSource.getDataSourceName()
        );
        this.dataSource.cutDataSource(dataSource);
        logger.info("系统{}完成数据源切换...", configProperties.getSysId());
    }

    private void dataSizeObserver() {
        ShardingConfigProperties configProperties = shardingDataSourceAutoConfiguration.getConfigProperties();
        logger.info("系统{}正在检查数据量...", configProperties.getSysId());
        List<ShardingConfig> shardingConfigs = ShardingConfigAssist.getShardingConfigs(
                shardingDataSourceAutoConfiguration.getDataSource(), configProperties.getSysId()
        );
        if (BoolUtil.isEmpty(shardingConfigs)) {
            return;
        }
        for (ShardingConfig conf : shardingConfigs) {
            try {
                observerSingleTable(shardingDataSourceAutoConfiguration.getDataSource(), configProperties, conf);
            } catch (Exception e) {
                e.printStackTrace();
                // 不影响其他表的观察
                continue;
            }
        }
    }

    private void observerSingleTable(DataSource dataSource, ShardingConfigProperties configProperties, ShardingConfig conf) {
        String actualDataNodes = conf.getActualDataNodes();
        AssertUtil.notEmpty(actualDataNodes, String.format("数据节点是空"));
        String[] split = actualDataNodes.split(",");
        AssertUtil.notEmpty(split, String.format("数据节点是空: %s", actualDataNodes));
        String last = split[split.length - 1];
        AssertUtil.notEmpty(last, String.format("最新节点异常: %s", actualDataNodes));
        String[] databaseTable = last.split("\\.");
        AssertUtil.eq(databaseTable.length, 2, String.format("最新节点异常: %s", last));
        String database = databaseTable[0];
        String table = databaseTable[1];
        MysqlShardingAssist.observerDataSize(dataSource, configProperties, database, table, conf);
    }

}
