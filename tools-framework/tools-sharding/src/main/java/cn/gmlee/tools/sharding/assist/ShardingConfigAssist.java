package cn.gmlee.tools.sharding.assist;

import cn.gmlee.tools.base.util.*;
import cn.gmlee.tools.ds.assist.DatasourceAssist;
import cn.gmlee.tools.sharding.config.DataSourceProperties;
import cn.gmlee.tools.sharding.config.ShardingConfigProperties;
import cn.gmlee.tools.sharding.entity.ShardingConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.shardingsphere.driver.api.ShardingSphereDataSourceFactory;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.audit.ShardingAuditStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.keygen.KeyGenerateStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * The type Sharding config assist.
 */
public class ShardingConfigAssist {
    private static final Map<String, Date> currentTableRuleMap = new ConcurrentHashMap();

    /**
     * 获取数据库最新分片配置.
     *
     * @param dataSource the dataSource
     * @param sysId      the sys id
     * @return the scs
     */
    public static List<ShardingConfig> getShardingConfigs(DataSource dataSource, Long sysId) {
        List<Map<String, Object>> configMap = JdbcUtil.exec(dataSource, String.format(MysqlShardingAssist.SELECT_TOOLS_SHARDING_CONFIG_SQL, sysId));
        return convertShardingConfig(configMap);
    }

    /**
     * Convert sharding config list.
     *
     * @param configMap the config map
     * @return the list
     */
    public static List<ShardingConfig> convertShardingConfig(List<Map<String, Object>> configMap) {
        List<ShardingConfig> shardingConfigs = new ArrayList();
        for (Map<String, Object> config : configMap) {
            shardingConfigs.add(JsonUtil.convert(config, ShardingConfig.class, false, true));
        }
        return shardingConfigs;
    }

    /**
     * 创建分片数据源.
     *
     * @param configProperties     the config properties
     * @param dataSourceProperties the data source properties
     * @param shardingConfigs      the sharding configs
     * @param dataSourceName       the data source name
     * @return the data source
     * @throws SQLException the sql exception
     */
    public static DataSource createShardingDataSource(ShardingConfigProperties configProperties, DataSourceProperties dataSourceProperties, List<ShardingConfig> shardingConfigs, String dataSourceName) throws SQLException {
        ModeConfiguration modeConfig = ShardingConfigAssist.createModeConfiguration(configProperties);
        Map<String, DataSource> dataSourceMap = ShardingConfigAssist.createDataSources(dataSourceProperties, shardingConfigs);
        Collection<RuleConfiguration> ruleConfigs = ShardingConfigAssist.createShardingRuleConfiguration(shardingConfigs);
        Properties props = configProperties.getProps();
        QuickUtil.isFalse(props.containsKey("sql-show") || props.containsKey("sqlShow"), () -> props.put("sql-show", true));
        DataSource dataSource = ShardingSphereDataSourceFactory
                .createDataSource(dataSourceName, modeConfig, dataSourceMap, ruleConfigs, props);
        return dataSource;
    }

    /**
     * 创建指定模式配置.
     *
     * @param shardingConfigProperties the sharding config properties
     * @return the mode configuration
     */
    public static ModeConfiguration createModeConfiguration(ShardingConfigProperties shardingConfigProperties) {
        return new ModeConfiguration("Standalone", null);
    }

    /**
     * 创建所需数据源.
     *
     * @param dataSourceProperties the data source properties
     * @param shardingConfigs      the sharding configs
     * @return the map
     */
    public static Map<String, DataSource> createDataSources(DataSourceProperties dataSourceProperties, List<ShardingConfig> shardingConfigs) {
        Map<String, DataSource> dataSourceMap = new LinkedHashMap();
        DataSource def = DatasourceAssist.createDataSource(dataSourceProperties.getDatasource().getDef());
        dataSourceMap.put("def", def);
        // 解析配置的数据源
        if (BoolUtil.isEmpty(shardingConfigs)) {
            return dataSourceMap;
        }
        Set<String> databaseNames = new HashSet();
        for (ShardingConfig conf : shardingConfigs) {
            String actualDataNodes = conf.getActualDataNodes();
            String[] split = actualDataNodes.split(",");
            AssertUtil.notEmpty(split, String.format("分片配置数据节点是空: %s", conf));
            for (String name : split) {
                String[] node = name.split("\\.");
                AssertUtil.notEmpty(node, String.format("分片配置节点格式异常: %s", name));
                if (node[0].startsWith("`") && node[0].endsWith("`")) {
                    String db = node[0].substring(1, node[0].length() - 1);
                    databaseNames.add(db);
                    continue;
                }
                databaseNames.add(node[0]);
            }
        }
        // 初始化配置数据源(实例也可以改成动态化 (未来按需扩展))
        AssertUtil.notEmpty(databaseNames, String.format("分片配置数据源是空: %s", shardingConfigs));
        for (String databaseName : databaseNames) {
            HikariDataSource db = new HikariDataSource();
            // 实例也可以改成动态化 (未来按需扩展)
            db.setDriverClassName("com.mysql.cj.jdbc.Driver");
            db.setJdbcUrl(String.format("jdbc:mysql://localhost:3306/%s?serverTimezone=Asia/Shanghai&useSSL=false&verifyServerCertificate=false", databaseName));
            db.setUsername("root");
            db.setPassword("root");
            dataSourceMap.put(databaseName, db);
        }
        return dataSourceMap;
    }

    /**
     * 创建分片规则.
     *
     * @param shardingConfigs the sharding configs
     * @return the collection
     */
    public static Collection<RuleConfiguration> createShardingRuleConfiguration(List<ShardingConfig> shardingConfigs) {
        Collection<RuleConfiguration> ruleConfigs = new ArrayList();
        ShardingRuleConfiguration ruleConfig = new ShardingRuleConfiguration();
        // 创建分表规则
        createTableRuleConfiguration(ruleConfig, shardingConfigs);
        // 内置主键策略
        ruleConfig.getKeyGenerators().put("snowflake", new AlgorithmConfiguration("snowflake", new Properties()));
        // 内置审计策略
        ruleConfig.getAuditors().put("sharding_key_none_auditor", new AlgorithmConfiguration("NONE", new Properties()));
        ruleConfig.getAuditors().put("sharding_key_required_auditor", new AlgorithmConfiguration("DML_SHARDING_CONDITIONS", new Properties()));
        ruleConfigs.add(ruleConfig);
        return ruleConfigs;
    }

    private static void createTableRuleConfiguration(ShardingRuleConfiguration ruleConf, List<ShardingConfig> shardingConfigs) {
        if (BoolUtil.isEmpty(shardingConfigs)) {
            return;
        }
        for (ShardingConfig conf : shardingConfigs) {
            // 创建分表配置
            String nodes = conf.getActualDataNodes();
            ShardingTableRuleConfiguration rule = new ShardingTableRuleConfiguration(conf.getLogicTab(), nodes.replace("`", ""));
            rule.setDatabaseShardingStrategy(new StandardShardingStrategyConfiguration(conf.getDbShardingColumn(), conf.getDbShardingAlgorithmName()));
            rule.setTableShardingStrategy(new StandardShardingStrategyConfiguration(conf.getTabShardingColumn(), conf.getTabShardingAlgorithmName()));
            rule.setKeyGenerateStrategy(new KeyGenerateStrategyConfiguration(conf.getPrimaryKey(), conf.getPrimaryKeyType()));
            String[] auditorNames = conf.getAuditorNames().split(",");
            rule.setAuditStrategy(new ShardingAuditStrategyConfiguration(Arrays.stream(auditorNames).collect(Collectors.toSet()), conf.getAuditorAllowHintDisable()));
            ruleConf.getTables().add(rule);
            // 配置分片算法
            Properties dbKvs = JsonUtil.toBean(conf.getDbShardingAlgorithmKvs(), Properties.class);
            ruleConf.getShardingAlgorithms().put(conf.getDbShardingAlgorithmName(), new AlgorithmConfiguration(conf.getDbShardingAlgorithmType(), dbKvs));
            Properties tabKvs = JsonUtil.toBean(conf.getTabShardingAlgorithmKvs(), Properties.class);
            ruleConf.getShardingAlgorithms().put(conf.getTabShardingAlgorithmName(), new AlgorithmConfiguration(conf.getTabShardingAlgorithmType(), tabKvs));
            // 记录分表规则
            currentTableRuleMap.put(String.format("%s.%s", conf.getLogicDb(), conf.getLogicTab()), NullUtil.get(conf.getUpdatedAt(), new Date()));
        }
    }

    /**
     * 检查是否存在刷新了的规则.
     *
     * @param conf the conf
     * @return 是否刷新 boolean
     */
    public static boolean checkCurrentTableRuleUpdateAt(ShardingConfig conf) {
        Date old = currentTableRuleMap.get(String.format("%s.%s", conf.getLogicDb(), conf.getLogicTab()));
        Date updatedAt = conf.getUpdatedAt();
        if (old != null && updatedAt != null) {
            return updatedAt.after(old);
        }
        return false;
    }
}
