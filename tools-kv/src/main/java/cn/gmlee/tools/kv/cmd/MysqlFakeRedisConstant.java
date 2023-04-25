package cn.gmlee.tools.kv.cmd;

/**
 * 常量.
 *
 * @author Jas °
 * @date 2021 /7/20 (周二)
 */
public class MysqlFakeRedisConstant {
    /**
     * The constant column_key.
     */
    public static final String COLUMN_KEY = "key";
    /**
     * The constant column_val.
     */
    public static final String COLUMN_VAL = "val";
    /**
     * The constant column_expire.
     */
    public static final String COLUMN_EXPIRE = "expire";


    /**
     * 创建伪KV数据库的SQL语句.
     */
    public static final String CREATE_TABLE_SQL =
            "CREATE TABLE `tools_kv` ( " +
                    "  `key` varchar(1024) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT '键', " +
                    "  `val` longtext CHARACTER SET utf8 COLLATE utf8_bin COMMENT '值: Text 或 Base64', " +
                    "  `expire` timestamp NULL DEFAULT NULL COMMENT '到期时间', " +
                    "  PRIMARY KEY (`key`) " +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='伪kv数据库'";

    /**
     * 查询伪KV是否存在.
     */
    public static final String EXIST_TABLE_SQL = "show tables like 'tools_kv'";

    /**
     * 回收过期时间的数据.
     */
    public static final String RECYCLE_KV_SQL = "delete from `tools_kv` where `expire` < current_timestamp(3)";

    /**
     * 获取1个key值.
     */
    public static final String get = "SELECT `key`, `val`, `expire` FROM tools_kv where `key`=?";

    /**
     * 获取1个key值.
     */
    public static final String getEx = "SELECT `key`, `val`, `expire` FROM tools_kv where `key`=? and `expire` > current_timestamp(3)";

    /**
     * 获取1个key值 (并锁住key).
     */
    public static final String getForUpdate = "SELECT `key`, `val`, `expire` FROM tools_kv where `key`=? FOR UPDATE";

    /**
     * 覆盖1个key值 (不论是否存在).
     */
    public static final String setEx = "insert into `tools_kv`(`key`, `val`, `expire`) values(?, ?, ?) ON DUPLICATE KEY UPDATE `val`=?, `expire`=?";
    /**
     * 覆盖1个key值 (不论是否存在).
     */
    public static final String set = "insert into `tools_kv`(`key`, `val`) values(?, ?) ON DUPLICATE KEY UPDATE `val`=?";
    /**
     * 仅当key不存在时插入.
     */
    public static final String setNx = "insert ignore into `tools_kv`(`key`, `val`, `expire`) values(?, ?, ?)";
    /**
     * 删除键.
     */
    public static final String delete = "delete from `tools_kv` where `key` = ?";
    /**
     * 更新键.
     */
    public static final String updateEx = "update `tools_kv` set `val`=?, `expire`=? where `key`=? and `expire` > current_timestamp(3)";
}
