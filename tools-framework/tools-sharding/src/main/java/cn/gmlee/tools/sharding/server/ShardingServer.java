package cn.gmlee.tools.sharding.server;

import cn.gmlee.tools.base.util.AssertUtil;
import cn.gmlee.tools.base.util.BoolUtil;
import cn.gmlee.tools.base.util.JdbcUtil;
import cn.gmlee.tools.sharding.assist.MysqlShardingAssist;
import cn.gmlee.tools.sharding.assist.ShardingConfigAssist;
import cn.gmlee.tools.sharding.config.ShardingConfigProperties;
import cn.gmlee.tools.sharding.entity.ShardingConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 自动扩缩容分片服务.
 * <p>
 * 提供给使用者可操作的功能.
 * </p>
 */
@Valid
public class ShardingServer {

    private static final Logger log = LoggerFactory.getLogger(ShardingServer.class);

    private DataSource dataSource;
    private ShardingConfigProperties configProperties;

    /**
     * Instantiates a new Sharding server.
     *
     * @param dataSource       the data source
     * @param configProperties the config properties
     */
    public ShardingServer(DataSource dataSource, ShardingConfigProperties configProperties) {
        this.dataSource = dataSource;
        this.configProperties = configProperties;
    }

    /**
     * 初始化分片配置.
     * <p>
     * 往tools_sharding_conf插入配置
     * </p>
     *
     * @param sysId    the sys id
     * @param database the database
     * @param table    the table
     */
    public boolean initializeTableShardingRule(@NotNull Long sysId, @NotEmpty String database, @NotEmpty String table) {
        String actualDataNodes = String.format("%s.%s", database, table);
        // sysId+database+table重复则忽略
        String sql = String.format(MysqlShardingAssist.INSERT_TOOLS_SHARDING_CONFIG_SQL, sysId, database, table, actualDataNodes);
        List<Map<String, Integer>> maps = JdbcUtil.exec(dataSource, sql, Integer.class);
        if (maps.isEmpty()) {
            return false;
        }
        Map<String, Integer> ret = maps.get(0);
        Integer count = ret.get("count");
        if (count != null && count > 0) {
            log.info("保存分片配置 {} 成功", String.format("[%s] %s.%s", sysId, database, table));
            return true;
        }
        log.warn("保存分片配置 {} 失败", String.format("[%s] %s.%s", sysId, database, table));
        return false;
    }

    /**
     * 释放物理节点.
     * <p>
     * 该操作将给tools_sharding_conf上X锁
     * 禁止其他线程读写操作(一般禁止该行为).
     * </p>
     *
     * @param id       不搞sysId替换(因为释放的节点解析逻辑名比较麻烦)
     * @param database the database
     * @param table    the table
     */
    public boolean releaseActualDataNodes(@NotNull Long id, @NotEmpty String database, @NotEmpty String table) {
        // 获取连接
        Connection con = JdbcUtil.get(dataSource, false);
        // 是否跳过
        int flag;
        try {
            flag = executeSkipCheck(con, id, database, table);
            if (flag < 0) {
                return false;
            }
        } catch (Exception e) {
            log.error("释放节点异常", e);
            // 回滚事务
            JdbcUtil.rollback(con);
            return false;
        }
        // 提交事务
        JdbcUtil.commit(con);
        return flag > 0;
    }

    /**
     * 检查是否存在跳过情况.
     *
     * @param con
     * @param id
     * @param database
     * @param table
     * @return
     */
    private int executeSkipCheck(Connection con, Long id, String database, String table) {
        String sql = String.format(MysqlShardingAssist.SELECT_FOR_UPDATE_TOOLS_SHARDING_CONFIG_SQL, id);
        List<Map<String, Object>> maps = JdbcUtil.exec(con, sql, false);
        AssertUtil.notEmpty(maps, String.format("配置不存在: [%s]", id));
        List<ShardingConfig> shardingConfigs = ShardingConfigAssist.convertShardingConfig(maps);
        AssertUtil.notEmpty(shardingConfigs, String.format("配置转换失败: %s", maps));
        ShardingConfig config = shardingConfigs.get(0);
        // 拼接释放节点名称
        String quote = String.format("`%s`.`%s`", database, table);
        String noneQuote = String.format("%s.%s", database, table);
        // 禁止释放源库源表
        if(database.equalsIgnoreCase(config.getLogicDb()) && table.equalsIgnoreCase(config.getLogicTab())){
            log.warn("禁止释放源库源表: [{}] {}", id, quote);
            return -1;
        }
        // 检查是否包含
        String actualDataNodes = config.getActualDataNodes();
        if (BoolUtil.isEmpty(actualDataNodes)) {
            log.warn("逻辑表是空: [{}]", id);
            return -1;
        }
        String[] split = actualDataNodes.split(",");
        // 如果只有1个节点不能缩容了
        if (split.length <= 1) {
            log.warn("只剩1个节点了: [{}] {}", id, actualDataNodes);
            return -1;
        }
        // 新节点(维持顺序)
        List<String> newActualDataNodes = new ArrayList(split.length - 1);
        for (String node : split) {
            if (quote.equalsIgnoreCase(node) || noneQuote.equalsIgnoreCase(node)) {
                continue;
            }
            newActualDataNodes.add(node);
        }
        // 确保完成缩容
        if (newActualDataNodes.size() >= split.length) {
            log.warn("节点 {}.{} 不存在: [{}] {}", database, table, id, actualDataNodes);
            return -1;
        }
        String nodes = String.join(",", newActualDataNodes);
        String updateSql = String.format(MysqlShardingAssist.RESET_TOOLS_SHARDING_CONFIG_ACTUAL_DATA_NODES_SQL, nodes, id);
        List<Map<String, Integer>> ret = JdbcUtil.exec(con, updateSql, Integer.class, false);
        if (ret.isEmpty() || ret.get(0) == null || ret.get(0).get("count") < 1) {
            log.warn("缩容出错: {}", updateSql);
            return 0;
        }
        log.info("缩容完成: {}-{}", id, noneQuote);
        return 1;
    }
}
