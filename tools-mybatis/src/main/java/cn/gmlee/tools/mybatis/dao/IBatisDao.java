package cn.gmlee.tools.mybatis.dao;

import cn.gmlee.tools.base.enums.Function;
import cn.gmlee.tools.base.mod.PageRequest;
import cn.gmlee.tools.base.mod.PageResponse;
import cn.gmlee.tools.base.util.ExceptionUtil;
import org.apache.ibatis.executor.ErrorContext;
import org.apache.ibatis.executor.ExecutorException;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.ParameterMode;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.property.PropertyTokenizer;
import org.apache.ibatis.scripting.xmltags.ForEachSqlNode;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.type.TypeHandler;
import org.apache.ibatis.type.TypeHandlerRegistry;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 通用传统Mybatis持久化操作工具
 * <p>
 * 该工具需要继承并追加到IOC中
 * </p>
 *
 * @param <T> the type parameter
 * @author Jas °
 * @date 2021 /3/5 (周五)
 */
public class IBatisDao<T> {

    private final SqlSessionFactory sqlSessionFactory;

    public IBatisDao(SqlSessionFactory sqlSessionFactory) {
        this.sqlSessionFactory = sqlSessionFactory;
    }

    public SqlSession openSession(){
        return sqlSessionFactory.openSession(true);
    }

    public SqlSession openSession(boolean autoCommit){
        return sqlSessionFactory.openSession(autoCommit);
    }

    public int execute(Object obj, String statementId) {
        return execute(obj, (entity, sqlSession) -> sqlSession.update(statementId, entity));
    }

    public int executeBatch(Collection<Object> os, String statementId) {
        return execute(os, (entity, sqlSession) -> sqlSession.update(statementId, entity));
    }

    public T selectOne(Object obj, String statementId) {
        return execute(obj, (entity, sqlSession) -> sqlSession.selectOne(statementId, entity));
    }

    public T selectById(Serializable id, String statementId) {
        return execute(id, (entity, sqlSession) -> sqlSession.selectOne(statementId, entity));
    }

    public List<T> selectList(Object obj, String statementId) {
        return execute(obj, (entity, sqlSession) -> sqlSession.selectList(statementId, entity));
    }

    public PageResponse<T> selectPage(PageRequest request, String statementId) {
        return execute(request, (entity, sqlSession) -> {
            String statement = statementId;
            Long count = selectCount(entity, sqlSession, statement);
            List<T> list = new ArrayList();
            if (count > 0) {
                list = sqlSession.selectList(statement, entity, getRowBounds(entity, count));
                return new PageResponse(entity, count, list);
            }
            return new PageResponse(entity, 0L, list);
        });
    }

    private RowBounds getRowBounds(PageRequest entity, Long count) {
        int pages = new BigDecimal(count / entity.size).setScale(0, RoundingMode.CEILING).intValue();
        if (entity.current > pages) {
            entity.setCurrent(pages);
        }
        int start = entity.current > 0 ? (entity.current - 1) : 0;
        int offset = start * entity.size;
        RowBounds rowBounds = new RowBounds(offset, offset + entity.size);
        return rowBounds;
    }

    protected Long selectCount(PageRequest request, SqlSession sqlSession, String statement) throws SQLException {
        MappedStatement mst = sqlSession.getConfiguration().getMappedStatement(statement);
        BoundSql boundSql = mst.getBoundSql(request);
        String sql = "select count(*) total from (" + boundSql.getSql() + ") tt";
        PreparedStatement ps = sqlSession.getConnection().prepareStatement(sql);
        setParameters(ps, mst, boundSql, request);
        ResultSet rs = ps.executeQuery();
        Long count = 0L;
        if (rs.next()) {
            count = rs.getLong("total");
        }
        rs.close();
        ps.close();
        return count;
    }

    private void setParameters(PreparedStatement ps, MappedStatement mappedStatement, BoundSql boundSql, Object parameterObject) throws SQLException {
        ErrorContext.instance().activity("setting parameters").object(mappedStatement.getParameterMap().getId());
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
        if (parameterMappings != null) {
            Configuration configuration = mappedStatement.getConfiguration();
            TypeHandlerRegistry typeHandlerRegistry = configuration.getTypeHandlerRegistry();
            MetaObject metaObject = parameterObject == null ? null : configuration.newMetaObject(parameterObject);
            for (int i = 0; i < parameterMappings.size(); i++) {
                ParameterMapping parameterMapping = parameterMappings.get(i);
                if (parameterMapping.getMode() != ParameterMode.OUT) {
                    Object value;
                    String propertyName = parameterMapping.getProperty();
                    PropertyTokenizer prop = new PropertyTokenizer(propertyName);
                    if (parameterObject == null) {
                        value = null;
                    } else if (typeHandlerRegistry.hasTypeHandler(parameterObject.getClass())) {
                        value = parameterObject;
                    } else if (boundSql.hasAdditionalParameter(propertyName)) {
                        value = boundSql.getAdditionalParameter(propertyName);
                    } else if (propertyName.startsWith(ForEachSqlNode.ITEM_PREFIX) && boundSql.hasAdditionalParameter(prop.getName())) {
                        value = boundSql.getAdditionalParameter(prop.getName());
                        if (value != null) {
                            value = configuration.newMetaObject(value).getValue(propertyName.substring(prop.getName().length()));
                        }
                    } else {
                        value = metaObject == null ? null : metaObject.getValue(propertyName);
                    }
                    TypeHandler typeHandler = parameterMapping.getTypeHandler();
                    if (typeHandler == null) {
                        throw new ExecutorException("There was no TypeHandler found for parameter " + propertyName + " of statement " + mappedStatement.getId());
                    }
                    typeHandler.setParameter(ps, i + 1, value, parameterMapping.getJdbcType());
                }
            }
        }
    }


    private <One, R> R execute(One one, Function.Two2r<One, SqlSession, R> two2r) {
        try {
            SqlSession sqlSession = sqlSessionFactory.openSession();
            R r = two2r.run(one, sqlSession);
            sqlSession.close();
            return r;
        } catch (Throwable throwable) {
            return ExceptionUtil.cast(throwable);
        }
    }
}
