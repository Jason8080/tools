package cn.gmlee.tools.sharding.assist;

import cn.gmlee.tools.base.util.AssertUtil;
import cn.gmlee.tools.base.util.BoolUtil;
import cn.gmlee.tools.base.util.IdUtil;
import cn.gmlee.tools.base.util.JdbcUtil;
import cn.gmlee.tools.sharding.config.ShardingConfigProperties;
import cn.gmlee.tools.sharding.entity.ShardingConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Mysql分片辅助工具
 *
 * @author Jas °
 * @date 2021 /6/11 (周五)
 */
public class MysqlShardingAssist {
    private static final Logger logger = LoggerFactory.getLogger(MysqlShardingAssist.class);
    /**
     * 查询建表语句返回字段.
     */
    private static final String CREATE_TABLE = "Create Table";
    /**
     * 查询建库语句返回字段.
     */
    private static final String CREATE_DATABASE = "Create Database";
    /**
     * 显示建库语句.
     */
    public static final String SHOW_CREATE_DATABASE_SQL = "show create database `%s`";
    /**
     * 显示建表语句.
     */
    public static final String SHOW_CREATE_TABLE_SQL = "show create table `%s`.`%s`";
    /**
     * 删库.
     */
    public static final String DROP_DATABASE_SQL = "drop database if exists `%s`";
    /**
     * 删表.
     */
    public static final String DROP_TABLE_SQL = "drop table if exists `%s`.`%s`";
    /**
     * 查询所有库名.
     * <p>
     * 备选SQL: show databases
     * </p>
     */
    public static final String SELECT_DATABASES_SQL = "SELECT SCHEMA_NAME FROM `INFORMATION_SCHEMA`.`SCHEMATA` WHERE SCHEMA_NAME LIKE '%s%%' ORDER BY SCHEMA_NAME ASC";
    /**
     * 查询所有表名 (可同时查多库).
     * <p>
     * 备选SQL: show tables, 前提是使用该数据库的数据源
     * </p>
     */
    public static final String SELECT_TABLES_SQL = "SELECT TABLE_SCHEMA, TABLE_NAME FROM `INFORMATION_SCHEMA`.`TABLES` WHERE TABLE_SCHEMA LIKE '%s%%' AND TABLE_NAME LIKE '%s%%' ORDER BY TABLE_SCHEMA,TABLE_NAME ASC";
    /**
     * 插入分片规则.
     */
    public static final String INSERT_TOOLS_SHARDING_CONFIG_SQL = "insert ignore into `tools_sharding`.`tools_sharding_config` (`sys_id`, `logic_db`, `logic_tab`, `actual_data_nodes`) values ('%s', '%s', '%s', '%s') ";
    /**
     * 获取库表匹配规则.
     */
    public static final String SELECT_TOOLS_SHARDING_CONFIG_SQL = "select * from `tools_sharding`.`tools_sharding_config` where `sys_id`=%s and `status`=1 and `del` is false ";
    /**
     * 读取某个配置并加锁.
     */
    public static final String SELECT_FOR_UPDATE_TOOLS_SHARDING_CONFIG_SQL = "select * from `tools_sharding`.`tools_sharding_config` where `id`=%s for update ";
    /**
     * 追加数据节点.
     */
    public static final String APPEND_TOOLS_SHARDING_CONFIG_ACTUAL_DATA_NODES_SQL = "update `tools_sharding`.`tools_sharding_config` set actual_data_nodes=CONCAT(actual_data_nodes,',%s') where id=%s ";
    /**
     * 重置数据节点.
     */
    public static final String RESET_TOOLS_SHARDING_CONFIG_ACTUAL_DATA_NODES_SQL = "update `tools_sharding`.`tools_sharding_config` set actual_data_nodes='%s' where id=%s ";
    /**
     * 查询表数据量.
     */
    public static final String COUNT_TABLE_SQL = "select count(*) from `%s`.`%s`";

    /**
     * Observer data size.
     *
     * @param dataSource       the data source
     * @param configProperties the config properties
     * @param database         the database
     * @param table            the table
     * @param conf             the conf
     */
    public static void observerDataSize(DataSource dataSource, ShardingConfigProperties configProperties, String database, String table, ShardingConfig conf) {
        // 数据量检查
        List<Map<String, Integer>> countMaps = JdbcUtil.exec(dataSource, String.format(MysqlShardingAssist.COUNT_TABLE_SQL, database, table), Integer.class);
        AssertUtil.notEmpty(countMaps, String.format("查询表数据量失败: %s", countMaps));
        Integer currentRowQty = countMaps.get(0).get("count(*)");
        Integer rowUpper = configProperties.getVolume().getRowUpper();
        // 数据量达到上限创建新表
        if (currentRowQty >= rowUpper) {
            observerTable(dataSource, configProperties, conf, database, table);
        }
    }

    private static void observerTable(DataSource dataSource, ShardingConfigProperties configProperties, ShardingConfig conf, String database, String table) {
        // 解析库名
        String databasePrefix = conf.getLogicDb();
        String databasePostfix = databasePrefix.length() >= database.length() ? "0" :
                database.substring(conf.getLogicDb().length() + 1);
        // 解析表名
        String tablePrefix = conf.getLogicTab();
        String tablePostfix = tablePrefix.length() >= table.length() ? "0" :
                table.substring(conf.getLogicTab().length() + 1);
        // 表数量检查
        List<Map<String, String>> tableMaps = "0".equalsIgnoreCase(tablePostfix) ?
                Collections.emptyList() : // 首次分库分表需建库建表
                JdbcUtil.exec(dataSource, String.format(MysqlShardingAssist.SELECT_TABLES_SQL, database, tablePrefix), String.class);
        int currentTabQty = tableMaps.size();
        Integer tableUpper = configProperties.getVolume().getTableUpper();
        String db = database;
        // 首次分片检查
        // (为了把分片节点放到业务库之外)
        boolean newSharding = database.equalsIgnoreCase(conf.getLogicDb());
        // 表数量达到上限创建新库
        if (newSharding || currentTabQty <= 0 || currentTabQty >= tableUpper) {
            db = databaseCreate(dataSource, database, databasePrefix, databasePostfix);
        }
        // 创建新表
        AssertUtil.notEmpty(db, String.format("新库节点异常"));
        String tab = tableCreate(dataSource, database, db, table, tablePrefix, tablePostfix);
        AssertUtil.notEmpty(db, String.format("新表节点异常"));
        String node = db + "." + tab;
        // 添加节点
        String actualDataNodes = conf.getActualDataNodes();
        if (!actualDataNodes.contains(node)) {
            appendActualDataNodes(dataSource, conf.getId(), node);
        }
    }

    private static String databaseCreate(DataSource dataSource, String database, String prefix, String postfix) {
        // 获取建库语句
        List<Map<String, String>> maps = JdbcUtil.exec(dataSource, String.format(MysqlShardingAssist.SHOW_CREATE_DATABASE_SQL, database), String.class);
        if (BoolUtil.isEmpty(maps)) {
            return database;
        }
        String sql = maps.get(0).get(CREATE_DATABASE);
        // 解析新库名称
        String newDatabase = analysisRename(database, database, prefix, postfix);
        sql = sql.replace(String.format("`%s`", database), String.format("%s `%s`", "IF NOT EXISTS", newDatabase));
        // 创建新库
        List<Map<String, Integer>> result = JdbcUtil.exec(dataSource, sql, Integer.class);
        logger.info("建库语句已经执行: {}-{}", newDatabase, result);
        if (BoolUtil.isEmpty(result)) {
            return database;
        }
        Integer count = result.get(0).get("count");
        if (count < 1) {
            return database;
        }
        return newDatabase;
    }

    private static String tableCreate(DataSource dataSource, String database, String newDatabase, String table, String prefix, String postfix) {
        // 获取建表语句
        List<Map<String, String>> maps = JdbcUtil.exec(dataSource, String.format(MysqlShardingAssist.SHOW_CREATE_TABLE_SQL, database, table), String.class);
        if (BoolUtil.isEmpty(maps)) {
            return table;
        }
        String sql = maps.get(0).get(CREATE_TABLE);
        // 解析新表名称
        String newTable = analysisRename(database, newDatabase, prefix, postfix);
        sql = sql.replace(String.format("`%s`", table), String.format("%s `%s`.`%s`", "IF NOT EXISTS", newDatabase, newTable));
        // 创建新表
        List<Map<String, Integer>> result = JdbcUtil.exec(dataSource, sql, Integer.class);
        logger.info("建表语句已经执行: {}.{}-{}", newDatabase, newTable, result);
        return newTable;
    }

    private static String analysisRename(String oldDatabase, String newDatabase, String prefix, String postfix) {
        if (BoolUtil.isDigit(postfix)) {
            long index = Long.parseLong(postfix);
            // 非新库
            if (BoolUtil.eq(oldDatabase, newDatabase)) {
                index++;
            } else {
                index = 1;
            }
            return prefix + "_" + index;
        }
        return prefix + "_" + IdUtil.snowflakeId();
    }

    private static void appendActualDataNodes(DataSource dataSource, Long id, String node) {
        List<Map<String, Integer>> maps = JdbcUtil.exec(dataSource, String.format(MysqlShardingAssist.APPEND_TOOLS_SHARDING_CONFIG_ACTUAL_DATA_NODES_SQL, node, id), Integer.class);
        AssertUtil.notEmpty(maps, String.format("追加节点失败"));
        Integer count = maps.get(0).get("count");
        AssertUtil.eq(count, 1, String.format("追加节点错误"));
        logger.info("扩容完成: {}-{}", id, node);
    }
}
