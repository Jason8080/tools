package cn.gmlee.tools.dt.kit;

import cn.gmlee.tools.base.util.ClassUtil;
import cn.gmlee.tools.base.util.HumpUtil;
import cn.gmlee.tools.dt.dao.entity.Tx;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Iterator;
import java.util.Map;

/**
 * The type Tx kit.
 */
@Valid
public class SqlKit {

    private static final String FOR_UPDATE = " FOR UPDATE";

    /**
     * 插入事物.
     *
     * @param entity the entity
     * @return the string
     */
    public synchronized static String insertIgnoreInto(@NotNull Tx entity) {
        String sql = "insert ignore into ";
        StringBuilder columns = new StringBuilder("`tx` (`id`");
        StringBuilder values = new StringBuilder("values (null");
        Map<String, Object> map = ClassUtil.generateCurrentMap(entity);
        map.remove("id");
        Iterator<Map.Entry<String, Object>> it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Object> next = it.next();
            String key = next.getKey();
            Object value = next.getValue();
            if (value != null) {
                columns.append(", ");
                values.append(", ");
                columns.append(String.format("`%s`", HumpUtil.hump2underline(key)));
                if (value instanceof Number) {
                    values.append(value);
                    continue;
                }
                values.append(String.format("'%s'", value.toString()));
            }
        }
        columns.append(") ");
        values.append(") ");
        return sql + columns.toString() + values.toString();
    }

    /**
     * Auto increment count string.
     *
     * @param entity the entity
     * @return the string
     */
    public static String autoIncrementCount(Tx entity) {
        return String.format("update `tx` set `count` = (`count`+1) where `code` = '%s'", entity.getCode());
    }

    /**
     * 更新事务.
     *
     * @param entity the tx
     * @return the string
     */
    public synchronized static String updateById(@NotNull Tx entity) {
        return updateBy(entity, "id");
    }

    /**
     * Update by global id string.
     *
     * @param entity the entity
     * @return the string
     */
    public static String updateByGlobalCode(Tx entity) {
        return updateBy(entity, "globalCode");
    }

    /**
     * Update by code string.
     *
     * @param entity the entity
     * @return the string
     */
    public static String updateByCode(Tx entity) {
        return updateBy(entity, "code");
    }

    /**
     * Update by superior code string.
     *
     * @param entity the entity
     * @return the string
     */
    public static String updateBySuperiorCode(Tx entity) {
        return updateBy(entity, "superiorCode");
    }

    private static String updateBy(Tx entity, String by) {
        StringBuilder sql = new StringBuilder("update `tx` set ");
        Map<String, Object> map = ClassUtil.generateCurrentMap(entity);
        Iterator<Map.Entry<String, Object>> it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Object> next = it.next();
            String key = next.getKey();
            Object value = next.getValue();
            if (by.equalsIgnoreCase(key)) {
                continue;
            }
            if (value != null) {
                sql.append(String.format("`%s` = ", HumpUtil.hump2underline(key)));
                if (value instanceof Number) {
                    sql.append(value);
                } else {
                    sql.append(String.format("'%s'", value.toString()));
                }
                sql.append(",");
            }
        }
        sql.delete(sql.length() - 1, sql.length());
        Object val = map.get(by);
        sql.append(String.format(" where `%s` = ", HumpUtil.hump2underline(by)));
        if (val instanceof Number) {
            sql.append(val);
        } else {
            sql.append(String.format("'%s'", val.toString()));
        }
        return sql.toString();
    }

    /**
     * Gets by code.
     *
     * @param code the code
     * @param lock the lock
     * @return the by code
     */
    public static String getByCode(String code, boolean lock) {
        String sql = "select * from `tx` where `code` = " + String.format("'%s'", code);
        if (lock) {
            return sql + FOR_UPDATE;
        }
        return sql;
    }

    /**
     * 查找全局数据的业务脚本.
     *
     * @param globalCode the global code
     * @param lock       the lock
     * @return the string
     */
    public static String listByGlobalCode(String globalCode, boolean lock) {
        String sql = "select * from `tx` where `global_code` = " + String.format("'%s'", globalCode);
        if (lock) {
            return sql + FOR_UPDATE;
        }
        return sql;
    }

    /**
     * List by superior code string.
     *
     * @param superiorCode the superior code
     * @param lock         the lock
     * @return the string
     */
    public static String listBySuperiorCode(String superiorCode, boolean lock) {
        String sql = "select * from `tx` where `superior_code` = " + String.format("'%s'", superiorCode);
        if (lock) {
            return sql + FOR_UPDATE;
        }
        return sql;
    }
}
