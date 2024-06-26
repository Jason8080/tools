package cn.gmlee.tools.profile.initializer;

import cn.gmlee.tools.base.util.JdbcUtil;
import cn.gmlee.tools.profile.assist.SqlAssist;
import cn.gmlee.tools.profile.conf.ProfileProperties;
import lombok.Getter;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Mysql数据初始化.
 */
public class MysqlProfileDataInitializer implements ProfileDataInitializer {

    private final String database = "mysql";

    @Getter
    private final List<String> tables;

    public MysqlProfileDataInitializer(String... tables) {
        this.tables = Arrays.asList(tables);
    }

    @Override
    public boolean support(String database) {
        return this.database.equalsIgnoreCase(database);
    }

    @Override
    public Map<String, Map<String, Object>> getColumn(Connection conn, ProfileProperties properties) {
        List<Map<String, Object>> exec = JdbcUtil.exec(conn, SqlAssist.SELECT_COLUMNS_MYSQL, false);
        return exec.stream().collect(Collectors.groupingBy(this::getTable, Collectors.toMap(this::getField, this::getType)));
    }

    @Override
    public void addColumn(ProfileProperties properties, Connection conn, String table, Map<String, Object> map) throws SQLException {
        JdbcUtil.exec(conn, String.format(SqlAssist.ADD_COLUMNS_MYSQL, conn.getSchema(), table), false);
    }
}
