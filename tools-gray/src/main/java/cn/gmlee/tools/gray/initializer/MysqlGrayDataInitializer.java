package cn.gmlee.tools.gray.initializer;

import cn.gmlee.tools.base.util.JdbcUtil;
import cn.gmlee.tools.gray.assist.SqlAssist;
import cn.gmlee.tools.gray.conf.GrayProperties;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Mysql数据初始化.
 */
public class MysqlGrayDataInitializer implements GrayDataInitializer{

    private final String database = "mysql";

    @Override
    public boolean support(String database) {
        return this.database.equalsIgnoreCase(database);
    }

    @Override
    public Map<String, Map<String, Object>> getColumn(Connection conn, GrayProperties properties) {
        List<Map<String, Object>> exec = JdbcUtil.exec(conn, SqlAssist.SELECT_COLUMNS_MYSQL, false);
        return exec.stream().collect(Collectors.groupingBy(this::getTable, Collectors.toMap(this::getField, this::getType)));
    }

    @Override
    public String getColumnSymbol() {
        return "`";
    }

    @Override
    public void addColumn(GrayProperties properties, Connection conn, String table, Map<String, Object> map) throws SQLException {
        JdbcUtil.exec(conn, String.format(SqlAssist.ADD_COLUMNS_MYSQL, conn.getSchema(), table), false);
    }
}
