package cn.gmlee.tools.mate.assist;

import cn.gmlee.tools.base.util.BoolUtil;
import cn.gmlee.tools.base.util.QuickUtil;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.reflection.DefaultReflectorFactory;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.factory.DefaultObjectFactory;
import org.apache.ibatis.reflection.wrapper.DefaultObjectWrapperFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SqlResetAssist {

    private static Logger logger = LoggerFactory.getLogger(SqlResetAssist.class);

    /**
     * 句柄重置.
     * <p>
     * 考虑到MybatisPlus分页插件类似的sql优化情况发生, 采用该方案实现sql重置(直接反射BoundSql没用, 必须重置args0).
     * </p>
     *
     * @param invocation
     * @param newSql
     * @param printSql
     */
    public static void resetSql(Invocation invocation, String newSql, boolean printSql) {
        BoundSql boundSql = getBoundSql(invocation);
        if (!boundSql.getSql().equalsIgnoreCase(newSql) && BoolUtil.notEmpty(newSql)) {
            QuickUtil.isTrue(printSql, () -> logger.debug("数据权限拦截前: {}", boundSql.getSql()));
            MappedStatement mappedStatement = (MappedStatement) invocation.getArgs()[0];
            BoundSqlSqlSource sqlSource = new BoundSqlSqlSource(boundSql);
            MappedStatement newMappedStatement = newMappedStatement(mappedStatement, sqlSource);
            DefaultObjectFactory objectFactory = new DefaultObjectFactory();
            DefaultObjectWrapperFactory objectWrapperFactory = new DefaultObjectWrapperFactory();
            DefaultReflectorFactory reflectorFactory = new DefaultReflectorFactory();
            MetaObject msObject = MetaObject.forObject(newMappedStatement, objectFactory, objectWrapperFactory, reflectorFactory);
            msObject.setValue("sqlSource.boundSql.sql", newSql);
            invocation.getArgs()[0] = newMappedStatement;
            QuickUtil.isTrue(printSql, () -> logger.debug("数据权限拦截后: {}", newSql));
        }
    }


    public static BoundSql getBoundSql(Invocation invocation) {
        MappedStatement mappedStatement = (MappedStatement) invocation.getArgs()[0];
        Object parameter = invocation.getArgs()[1];
        return mappedStatement.getBoundSql(parameter);
    }


    private static MappedStatement newMappedStatement(MappedStatement ms, SqlSource sqlSource) {
        MappedStatement.Builder builder = new MappedStatement.Builder(
                ms.getConfiguration(), ms.getId(), sqlSource, ms.getSqlCommandType()
        );
        builder.resource(ms.getResource());
        builder.fetchSize(ms.getFetchSize());
        builder.statementType(ms.getStatementType());
        builder.keyGenerator(ms.getKeyGenerator());
        if (ms.getKeyProperties() != null && ms.getKeyProperties().length != 0) {
            StringBuilder keyProperties = new StringBuilder();
            for (String keyProperty : ms.getKeyProperties()) {
                keyProperties.append(keyProperty).append(",");
            }
            keyProperties.delete(keyProperties.length() - 1, keyProperties.length());
            builder.keyProperty(keyProperties.toString());
        }
        builder.timeout(ms.getTimeout());
        builder.parameterMap(ms.getParameterMap());
        builder.resultMaps(ms.getResultMaps());
        builder.resultSetType(ms.getResultSetType());
        builder.cache(ms.getCache());
        builder.flushCacheRequired(ms.isFlushCacheRequired());
        builder.useCache(ms.isUseCache());
        return builder.build();
    }

    private static class BoundSqlSqlSource implements SqlSource {
        private BoundSql boundSql;

        public BoundSqlSqlSource(BoundSql boundSql) {
            this.boundSql = boundSql;
        }

        @Override
        public BoundSql getBoundSql(Object parameterObject) {
            return boundSql;
        }
    }
}
