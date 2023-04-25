package cn.gmlee.tools.datalog.assist;

import cn.gmlee.tools.base.util.*;
import cn.gmlee.tools.datalog.model.Datalog;
import cn.gmlee.tools.datalog.model.LogData;
import org.apache.ibatis.binding.MapperMethod;
import org.apache.ibatis.builder.SqlSourceBuilder;
import org.apache.ibatis.mapping.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.springframework.util.StringUtils;

import java.text.DateFormat;
import java.util.*;

/**
 * MybatisPlus sql辅助工具
 *
 * @author Jas °
 * @date 2021 /4/1 (周四)
 */
public class DatalogAssist {
    private static final String where = "WHERE";
    private static final String set = "SET";

    /**
     * 获取条件语句
     *
     * @param originalSql the originalSql
     * @return where originalSql
     */
    public static String getWhereSql(String originalSql) {
        String sql = originalSql.replace(where.toLowerCase(), where);
        return sql.substring(sql.lastIndexOf(where) + where.length());
    }

    /**
     * 获取更新语句
     *
     * @param originalSql the original sql
     * @return set sql
     */
    public static String getSetSql(String originalSql) {
        String sql = originalSql.replace(where.toLowerCase(), where);
        int whereIndex = sql.lastIndexOf(where);
        String setUpperCaseSql = sql.replace(set.toLowerCase(), set);
        return setUpperCaseSql.substring(setUpperCaseSql.indexOf(set) + set.length(), whereIndex);
    }

    /**
     * 获取查询语句.
     *
     * @param tables   the tables
     * @param whereSql the where sql
     * @return the select sql
     */
    public static String getSelectSql(String tables, String whereSql) {
        StringBuilder sb = new StringBuilder();
        AssertUtil.notEmpty(tables, String.format("表名解析异常: %s", tables));
        String[] split = tables.split(",");
        if (BoolUtil.notEmpty(split)) {
            sb.append(new StringBuilder("SELECT * FROM " + split[0] + " WHERE " + whereSql));
        }
        for (int i = 1; i < split.length; i++) {
            if (BoolUtil.notEmpty(split[i])) {
                sb.append(" union ");
                sb.append("SELECT * FROM " + split[i] + where + whereSql);
            }
        }
        return sb.toString();
    }

    /**
     * 获取更新集合
     *
     * @param sql the sql
     * @return set map
     */
    public static Map<String, String> getSetMap(String sql) {
        Map<String, String> setMap = new HashMap(0);
        if (!StringUtils.isEmpty(sql)) {
            String[] groups = sql.split(",");
            for (String group : groups) {
                int eqIndex = group.indexOf("=");
                String fieldName = group.substring(0, eqIndex);
                String fieldValue = group.substring(eqIndex + 1);
                String name = fieldName.replaceAll("\"|'| ", "");
                String value = fieldValue.replaceAll("\"|'", "");
                setMap.put(name, value.trim());
            }
        }
        return setMap;
    }

    /**
     * 获取原始SQL
     *
     * @param ms  the ms
     * @param arg the arg
     * @return original sql
     */
    public static String getOriginalSql(MappedStatement ms, Object arg) {
        Configuration configuration = ms.getConfiguration();
        BoundSql boundSql = ms.getBoundSql(arg);
        Object parameterObject = boundSql.getParameterObject();
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
        String sql = boundSql.getSql().replaceAll("[\\s]+", " ");
        if (parameterMappings.size() > 0 && parameterObject != null) {
            TypeHandlerRegistry typeHandlerRegistry = configuration.getTypeHandlerRegistry();
            if (typeHandlerRegistry.hasTypeHandler(parameterObject.getClass())) {
                sql = sql.replaceFirst("\\?", getParameterValue(parameterObject));

            } else {
                MetaObject metaObject = configuration.newMetaObject(parameterObject);
                for (ParameterMapping parameterMapping : parameterMappings) {
                    String propertyName = parameterMapping.getProperty();
                    if (metaObject.hasGetter(propertyName)) {
                        Object obj = metaObject.getValue(propertyName);
                        sql = sql.replaceFirst("\\?", getParameterValue(obj));
                    } else if (boundSql.hasAdditionalParameter(propertyName)) {
                        Object obj = boundSql.getAdditionalParameter(propertyName);
                        sql = sql.replaceFirst("\\?", getParameterValue(obj));
                    }
                }
            }
        }
        return sql;
    }

    private static String getParameterValue(Object obj) {
        String value = null;
        if (obj instanceof String) {
            value = "'" + obj.toString() + "'";
            value = value.replaceAll("\\\\", "\\\\\\\\");
            value = value.replaceAll("\\$", "\\\\\\$");
        } else if (obj instanceof Date) {
            DateFormat formatter = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.CHINA);
            value = "'" + formatter.format(obj) + "'";
        } else {
            if (obj != null) {
                value = obj.toString();
            } else {
                value = "";
            }

        }
        return value;
    }

    /**
     * Generator ms mapped statement.
     *
     * @param ms          the ms
     * @param statementId the select list statement
     * @return the mapped statement
     */
    public static MappedStatement getStatementById(MappedStatement ms, String statementId) {
        Configuration configuration = ms.getConfiguration();
        String currentStatementId = ms.getId();
        int lastIndexOf = currentStatementId.lastIndexOf(".");
        String statementPrefix = currentStatementId.substring(0, lastIndexOf);
        try {
            return configuration.getMappedStatement(statementPrefix + "." + statementId);
        } catch (IllegalArgumentException exception) {
            return ExceptionUtil.cast(String.format("Statement not found: %s", statementPrefix + "." + statementId), exception);
        }
    }

    /**
     * 根据原始sql生成句柄工具.
     *
     * @param configuration the configuration
     * @param originalSql   the original sql
     * @param statementId   the statement id
     * @return the mapped statement
     */
    public static MappedStatement generatorMs(Configuration configuration, String originalSql, String statementId) {
        SqlSourceBuilder sqlSourceBuilder = new SqlSourceBuilder(configuration);
        SqlSource sqlSource = sqlSourceBuilder.parse(originalSql, null, Collections.EMPTY_MAP);
        MappedStatement statement = new MappedStatement
                .Builder(configuration, statementId, sqlSource, SqlCommandType.SELECT)
                .useCache(false)
                .build();
        configuration.addMappedStatement(statement);
        return statement;
    }

    /**
     * Gets select list original sql.
     *
     * @param ms                  the ms
     * @param selectListStatement the select list statement
     * @param whereSql            the where sql
     * @param forUpdate           the for update
     * @return the select list original sql
     */
    public static String getDatalogSelectSql(MappedStatement ms, String selectListStatement, String whereSql, boolean forUpdate) {
        MappedStatement selectMs = getStatementById(ms, selectListStatement);
        BoundSql boundSql = selectMs.getBoundSql(null);
        return getSelectSql(boundSql.getSql(), whereSql, forUpdate);
    }

    private static String getSelectSql(String sql, String whereSql, boolean forUpdate) {
        StringBuilder sb = new StringBuilder();
        int whereIndex1 = sql.lastIndexOf("where");
        int whereIndex2 = sql.lastIndexOf("WHERE");
        sb.append(sql);
        if (whereIndex1 < 0 && whereIndex2 < 0) {
            sb.append(" WHERE ");
            sb.append(whereSql);
        } else {
            sb.append(" AND (");
            sb.append(whereSql);
            sb.append(")");
        }
        if (forUpdate) {
            sb.append(" FOR UPDATE ");
        }
        return sb.toString();
    }

    /**
     * Gets query wrapper.
     *
     * @param ms  the ms
     * @param arg the arg
     * @return the query wrapper
     */
    public static MapperMethod.ParamMap<Object> parameterObjectHandler(MappedStatement ms, MapperMethod.ParamMap<Object> arg) {
        MapperMethod.ParamMap<Object> args = new MapperMethod.ParamMap();
        args.putAll(arg);
        Iterator<Map.Entry<String, Object>> it = arg.entrySet().iterator();
        if (it.hasNext()) {
            Map.Entry<String, Object> next = it.next();
            Map<String, Object> parameterMap = ClassUtil.generateMap(next.getValue());
            BoundSql boundSql = ms.getBoundSql(arg);
            String whereSql = DatalogAssist.getWhereSql(boundSql.getSql());
            Set<String> keys = parameterMap.keySet();
            for (String key : keys) {
                Object val = parameterMap.get(key);
                if (val != null && !whereSql.contains(key)) {
                    parameterMap.put(key, null);
                }
            }
            args.putAll(parameterMap);
//            args.put(Constants.WRAPPER, Wrappers.query(parameterMap))
        }
        return args;
    }

    /**
     * Class for datalog datalog.
     *
     * @param lds the thread local log data
     * @param datalogClazz       the datalog clazz
     * @return datalog datalog
     */
    public static List<Datalog> classForDatalog(ThreadLocal<List<LogData>> lds, Class<? extends Datalog> datalogClazz) {
        List<LogData> list = lds.get();
        try {
            if (BoolUtil.notEmpty(list)) {
                List<Datalog> ds = new ArrayList(list.size());
                for (LogData ld : list){
                    Datalog datalog = BeanUtil.convert(ld, datalogClazz);
                    ds.add(datalog);
                }
                return ds;
            }
        } finally {
            lds.remove();
        }
        return null;
    }
}
