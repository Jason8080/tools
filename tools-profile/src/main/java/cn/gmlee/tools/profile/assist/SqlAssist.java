package cn.gmlee.tools.profile.assist;

import cn.gmlee.tools.base.util.BoolUtil;
import cn.gmlee.tools.base.util.ExceptionUtil;
import cn.gmlee.tools.base.util.QuickUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.mapping.BoundSql;

import java.lang.reflect.Field;

/**
 * The type Sql assist.
 */
@Slf4j
public class SqlAssist {
    /**
     * The constant SELECT_COLUMNS_ORACLE.
     */
    public static final String SELECT_COLUMNS_ORACLE = "SELECT table_name, column_name, data_type, data_length FROM USER_TAB_COLUMNS WHERE TABLE_NAME LIKE '%'";
    /**
     * The constant SELECT_COLUMNS_MYSQL.
     */
    public static final String SELECT_COLUMNS_MYSQL = "SELECT table_name, column_name, data_type, character_maximum_length FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME LIKE '%'";
    /**
     * The constant ADD_COLUMNS_ORACLE.
     */
    public static final String ADD_COLUMNS_ORACLE = "ALTER TABLE \"%s\".\"%s\" ADD (\"env\" NUMBER(2,0) DEFAULT 1)";
    /**
     * The constant ADD_COLUMNS_COMMENT_ORACLE.
     */
    public static final String ADD_COLUMNS_COMMENT_ORACLE = "COMMENT ON COLUMN \"%s\".\"%s\".\"env\" IS '环境: 0测试1生产; 默认1'";
    /**
     * The constant ADD_COLUMNS_MYSQL.
     */
    public static final String ADD_COLUMNS_MYSQL = "ALTER TABLE `%s`.`%s` ADD COLUMN `env` tinyint(2) NULL DEFAULT 1 COMMENT '环境: 0测试1生产; 默认1'";

    /**
     * Reset.
     *
     * @param boundSql the bound sql
     * @param newSql   the new sql
     * @throws NoSuchFieldException the no such field exception
     */
    public static void reset(BoundSql boundSql, String newSql) throws NoSuchFieldException {
        if(BoolUtil.isEmpty(newSql)){
            return;
        }
        log.info("数据灰度处理前: {}", boundSql.getSql());
        Field field = boundSql.getClass().getDeclaredField("sql");
        boolean ok = field.isAccessible();
        QuickUtil.isFalse(ok, () -> field.setAccessible(true));
        ExceptionUtil.suppress(() -> field.set(boundSql, newSql));
        QuickUtil.isFalse(ok, () -> field.setAccessible(false));
        log.info("数据灰度处理后: {}", boundSql.getSql());
    }
}
