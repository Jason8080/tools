package cn.gmlee.tools.profile.initializer;

import cn.gmlee.tools.base.util.AssertUtil;
import cn.gmlee.tools.profile.conf.ProfileProperties;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * 数据隔离初始化器.
 */
public interface ProfileDataInitializer {
    /**
     * Support boolean.
     *
     * @param database the database
     * @return the boolean
     */
    boolean support(String database);

    /**
     * Gets tables.
     *
     * @return the list
     */
    List<String> getTables();

    /**
     * Gets columns.
     *
     * @param conn       the conn
     * @param properties the properties
     * @return the columns
     */
    Map<String, Map<String, Object>> getColumn(Connection conn, ProfileProperties properties);

    /**
     * Add column.
     *
     * @param properties the properties
     * @param conn       the conn
     * @param table      the table
     * @param map        the map
     * @throws SQLException the sql exception
     */
    void addColumn(ProfileProperties properties, Connection conn, String table, Map<String, Object> map) throws SQLException;

    /**
     * Gets table.
     *
     * @param map the map
     * @return the table
     */
    default String getTable(Map<String, Object> map) {
        String field = "TABLE_NAME";
        Object value = map.containsKey(field) ? map.get(field) : map.get(field.toLowerCase());
        AssertUtil.notNull(value, "数据隔离初始化发现表名是空");
        return value.toString();
    }

    /**
     * Gets field.
     *
     * @param map the map
     * @return the field
     */
    default String getField(Map<String, Object> map) {
        String field = "COLUMN_NAME";
        Object value = map.containsKey(field) ? map.get(field) : map.get(field.toLowerCase());
        AssertUtil.notNull(value, "数据隔离初始化发现表名是空");
        return value.toString();
    }

    /**
     * Gets type.
     *
     * @param map the map
     * @return the type
     */
    default String getType(Map<String, Object> map) {
        String field = "DATA_TYPE";
        Object value = map.containsKey(field) ? map.get(field) : map.get(field.toLowerCase());
        AssertUtil.notNull(value, "数据隔离初始化发现表名是空");
        return value.toString();
    }
}
