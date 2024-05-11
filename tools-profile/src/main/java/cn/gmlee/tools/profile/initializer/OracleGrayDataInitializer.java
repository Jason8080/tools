package cn.gmlee.tools.profile.initializer;

import cn.gmlee.tools.base.util.JdbcUtil;
import cn.gmlee.tools.profile.assist.SqlAssist;
import cn.gmlee.tools.profile.conf.ProfileProperties;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Mysql数据初始化.
 */
public class OracleGrayDataInitializer implements GrayDataInitializer {

    private final String database = "oracle";

    @Override
    public boolean support(String database) {
        return this.database.equalsIgnoreCase(database);
    }

    @Override
    public Map<String, Map<String, Object>> getColumn(Connection conn, ProfileProperties properties) {
        List<Map<String, Object>> exec = JdbcUtil.exec(conn, SqlAssist.SELECT_COLUMNS_ORACLE, false);
        return exec.stream().collect(Collectors.groupingBy(this::getTable, Collectors.toMap(this::getField, this::getType)));
    }

    @Override
    public void addColumn(ProfileProperties properties, Connection conn, String table, Map<String, Object> map) throws SQLException {
        JdbcUtil.exec(conn, String.format(SqlAssist.ADD_COLUMNS_ORACLE, conn.getSchema(), table), false);
        JdbcUtil.exec(conn, String.format(SqlAssist.ADD_COLUMNS_COMMENT_ORACLE, conn.getSchema(), table), false);
    }
}
