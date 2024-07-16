package cn.gmlee.tools.mybatis.assist;

import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.Configuration;

import java.sql.Date;
import java.util.List;

/**
 * 真实Sql处理工具.
 */
public class SqlRealAssist {
    /**
     * 替换占位符并压缩.
     *
     * @param configuration   the configuration
     * @param boundSql        the bound sql
     * @param parameterObject the parameter object
     * @return the string
     */
    public static String replaceStrip(Configuration configuration, BoundSql boundSql, Object parameterObject) {
        String sql = boundSql.getSql();
        MetaObject metaObject = configuration.newMetaObject(parameterObject);
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
        if (parameterObject != null && parameterMappings != null) {
            for (ParameterMapping parameterMapping : parameterMappings) {
                String propertyName = parameterMapping.getProperty();
                Object value = boundSql.hasAdditionalParameter(propertyName) ?
                        boundSql.getAdditionalParameter(propertyName) :
                        getValue(metaObject.getValue(propertyName));
                sql = sql.replaceFirst("\\?", value.toString());
            }
        }
        return sql.replaceAll("\\s+", " ").trim();
    }

    private static Object getValue(Object value) {
        if (value == null) {
            return "null";
        } else if (value instanceof String) {
            return "'" + value + "'";
        } else if (value instanceof Date) {
            return "'" + value + "'";
        } else {
            return value;
        }
    }
}
