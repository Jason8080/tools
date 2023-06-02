package cn.gmlee.tools.mate.interceptor;


import cn.gmlee.tools.base.anno.DataFilter;
import cn.gmlee.tools.base.util.*;
import cn.gmlee.tools.mate.assist.FutureAssist;
import cn.gmlee.tools.mate.assist.SqlResetAssist;
import com.baomidou.mybatisplus.core.toolkit.PluginUtils;
import lombok.SneakyThrows;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.executor.resultset.DefaultResultSetHandler;
import org.apache.ibatis.executor.resultset.ResultSetHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;

/**
 * The type Data auth interceptor.
 */
@Intercepts({
        @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class}),
        @Signature(type = ParameterHandler.class, method = "setParameters", args = {PreparedStatement.class}),
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class}),
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}),
        @Signature(type = ResultSetHandler.class, method = "handleResultSets", args = {Statement.class}),
        @Signature(type = ResultSetHandler.class, method = "handleCursorResultSets", args = {Statement.class}),
        @Signature(type = ResultSetHandler.class, method = "handleOutputParameters", args = {CallableStatement.class}),
})
@SuppressWarnings("all")
public class DataAuthInterceptor implements Interceptor {

    private Logger logger = LoggerFactory.getLogger(DataAuthInterceptor.class);

    @Autowired
    private DataAuthServer dataAuthServer;

    @Autowired
    private CodecServer codecServer;

    /**
     * 临时表名称.
     */
    @Value("${tools.mate.temporaryTableName:toolsTt}")
    private String ttt;

    /**
     * 为了兼容3.4.5以下版本
     *
     * @param target
     * @return
     */
    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }


    /**
     * 数据拦截.
     * 由于注解加载任意方法(非Mapper上), 所以当前线程中所有的查询都会使用该数据权限.
     *
     * @param invocation
     * @return
     */
    @Override
    @SneakyThrows
    public Object intercept(Invocation invocation) {
        try {
            Object o = dataAuth(invocation);
            if (o != null) {
                return o;
            }
        } catch (Throwable throwable) {
            logger.error(String.format("数据鉴权出错"), throwable);
            if (dataAuthServer.allowEx()) {
                throw throwable;
            }
        }
        return invocation.proceed();
    }

    private Object dataAuth(Invocation invocation) throws Exception {
        Method method = invocation.getMethod();
        switch (method.getName()) {
            case "update": {
                return encode(invocation);
            }
            case "setParameters": {
//                return encode2(invocation);
            }
            case "query": {
                return rowDataAuth(invocation);
            }
            case "handleResultSets": {
                return columnDataAuth(invocation);
            }
        }
        return null;
    }

    private Object encode(Invocation invocation) {
        // 编码开关
        if (!codecServer.enable(CodecServer.ENCODE)) {
            return null;
        }
        Object[] args = invocation.getArgs();
        MappedStatement mappedStatement = (MappedStatement) args[0];
        Object po = args[1];
        // 加密
        if (po != null && !(po instanceof Map)) {
            String key = po.getClass().getSimpleName().toLowerCase();
            Map<String, Object> map = new HashMap();
            map.put(key, po);
            po = map;
        }
        encode((Map<String, Object>) po);
        return null;
    }


    private Object encode2(Invocation invocation) {
        // 编码开关
        if (!codecServer.enable(CodecServer.ENCODE)) {
            return null;
        }
        // 加密
        if (invocation.getTarget() instanceof ParameterHandler) {
            ParameterHandler ph = (ParameterHandler) invocation.getTarget();
            Object po = ph.getParameterObject();
            if (po != null && !(po instanceof Map)) {
                String key = po.getClass().getSimpleName().toLowerCase();
                Map<String, Object> map = new HashMap();
                map.put(key, po);
                po = map;
            }
            encode((Map<String, Object>) po);
        }
        return null;
    }

    private void encode(Map<String, Object> parameterMap) {
        if (BoolUtil.isEmpty(parameterMap)) {
            return;
        }
        // 已加密对象
        List<Object> list = new ArrayList();
        Iterator<Map.Entry<String, Object>> it = parameterMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Object> next = it.next();
            Object value = next.getValue();
            encode(value, list);
        }
    }

    private void encode(Collection c, List list) {
        for (Object o : c) {
            encode(o, list);
        }
    }

    private void encode(Object[] os, List list) {
        for (Object o : os) {
            encode(o, list);
        }
    }

    private void encode(Object o, List list) {
        if (o instanceof Collection) {
            encode((Collection) o, list);
        } else if (o instanceof Object[]) {
            encode((Object[]) o, list);
        } else if (!list.contains(o)) {
            // 已编码过将不再重复编码
            list.add(o);
            encodeObj(o);
        }
    }

    private void encodeObj(Object o) {
        // 忽略基础字段
        if (o == null || BoolUtil.isBaseClass(o, String.class, Date.class, BigDecimal.class)){
            return;
        }
        // 如果是Map
        if(o instanceof Map){
            Map<String, Object> valuesMap = (Map<String, Object>) o;
            Iterator<Map.Entry<String, Object>> it = valuesMap.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, Object> next = it.next();
                String key = next.getKey();
                Object value = next.getValue();
                // 字段编码处理
                codecServer.encode(valuesMap, o, key, value);
            }
            return;
        }
        // 获取所有字段
        Map<String, Field> fieldsMap = ClassUtil.getFieldsMap(o);
        Iterator<Map.Entry<String, Field>> it = fieldsMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Field> next = it.next();
            String key = next.getKey();
            Field field = next.getValue();
            // 字段编码处理
            codecServer.encode(fieldsMap, o, key, field);
        }
    }

    private Object rowDataAuth(Invocation invocation) throws Exception {
        DataFilter[] row = DataScopeUtil.getRow();
        if (BoolUtil.notEmpty(row)) {
            BoundSql boundSql = SqlResetAssist.getBoundSql(invocation);
            if (BoolUtil.isNull(boundSql)) {
                return null;
            }
            // 兜底句柄
            StringBuilder newSql = new StringBuilder();
            // 直拼句柄处理
            StringBuilder directSql = new StringBuilder();
            sqlHandler(directSql, boundSql.getSql(), row);
            // 行权限处理(叠加直拼句柄)
            fieldsHandler(directSql.toString(), newSql, row);
            // 重置句柄
            SqlResetAssist.resetSql(invocation, newSql.toString(), dataAuthServer.printSql());
        }
        return null;
    }

    private Object columnDataAuth(Invocation invocation) throws Exception {
        DataFilter[] column = DataScopeUtil.getCol();
        // 数据权限和解码关
        if(BoolUtil.isEmpty(column) && !codecServer.enable(CodecServer.DECODE)){
            return null;
        }
        DefaultResultSetHandler defaultResultSetHandler = PluginUtils.realTarget(invocation.getTarget());
        MetaObject metaStatementHandler = SystemMetaObject.forObject(defaultResultSetHandler);
        MappedStatement mappedStatement = (MappedStatement) metaStatementHandler.getValue("mappedStatement");
        // 结果集处理
        Statement statement = (Statement) invocation.getArgs()[0];
        List<Object> objects = defaultResultSetHandler.handleResultSets(statement);
        return columnDataAuthAll(objects, NullUtil.get(column, new DataFilter[0]));
    }

    private synchronized void fieldsHandler(String originalSql, StringBuilder sb, DataFilter[] dataFilters) {
        sb.append("select ");
        sb.append(ttt);
        sb.append(".* ");
        sb.append("from (");
        sb.append(originalSql);
        sb.append(") ");
        sb.append(ttt);
        sb.append(" where ");
        for (int i = 0; i < dataFilters.length; i++) {
            if (i > 0) {
                sb.append(" and ");
            }
            DataFilter dataFilter = dataFilters[i];
            String rowIn = FutureAssist.supplyAsync(() -> dataAuthServer.rowIn(dataFilter.flag()));
            List<String> fields = FutureAssist.supplyAsync(() -> dataAuthServer.rowFields(dataFilter.flag()));
            if (BoolUtil.notEmpty(fields)) {
                if (BoolUtil.notEmpty(rowIn)) {
                    for (int j = 0; j < fields.size(); j++) {
                        if (j > 0) {
                            sb.append("and");
                        }
                        sb.append(" ");
                        sb.append(ttt);
                        sb.append(".");
                        sb.append(fields.get(j));
                        sb.append(" in (");
                        sb.append(rowIn);
                        sb.append(") ");
                    }
                } else {
                    sb.append("false");
                }
            } else {
                sb.append("true");
            }
        }
    }

    private void sqlHandler(StringBuilder sb, String originalSql, DataFilter[] dataFilters) {
        for (DataFilter dataFilter : dataFilters) {
            if (BoolUtil.notEmpty(dataFilter.sql())) {
                sb.append("select ");
                sb.append(ttt);
                sb.append(".* from (");
                sb.append(originalSql);
                sb.append(") ");
                sb.append(ttt);
                sb.append(" ");
                sb.append(dataFilter.sql());
                sb.append(" ");
            } else {
                sb.append(originalSql);
            }
        }
    }


    // -----------------------------------------------------------------------------------------------------------------


    private Object columnDataAuthAll(List<Object> objects, DataFilter... dataFilters) throws Exception {
        if (BoolUtil.notEmpty(objects)) {
            Iterator<Object> it = objects.iterator();
            while (it.hasNext()){
                Object o = it.next();
                if(o == null || BoolUtil.isBaseClass(o, String.class, Date.class, BigDecimal.class)){
                    continue;
                }
                // 如果是Map
                if(o instanceof Map){
                    Map<String, Object> valuesMap = (Map<String, Object>) o;
                    Iterator<Map.Entry<String, Object>> iterator = valuesMap.entrySet().iterator();
                    while (iterator.hasNext()) {
                        Map.Entry<String, Object> next = iterator.next();
                        String key = next.getKey();
                        Object value = next.getValue();
                        // 字段编码处理
                        codecServer.decode(valuesMap, o, key, value);
                    }
                    continue;
                }
                // 获取所有字段
                Map<String, Field> fieldsMap = ClassUtil.getFieldsMap(o);
                Iterator<Map.Entry<String, Field>> iterator = fieldsMap.entrySet().iterator();
                while (iterator.hasNext()){
                    Map.Entry<String, Field> entry = iterator.next();
                    String key = entry.getKey();
                    Field field = entry.getValue();
                    // 数据权限处理
                    filterColumns(o, key, field, dataFilters);
                    // 字段解码处理
                    codecServer.decode(fieldsMap, o, key, field);
                }
            }
        }
        return objects;
    }

    private void filterColumns(Object o, String key, Field field, DataFilter... dataFilters) {
        boolean filter = true;
        for (DataFilter dataFilter : dataFilters) {
            List<String> columns = FutureAssist.supplyAsync(() -> dataAuthServer.colFields(dataFilter.flag()));
            boolean contain = BoolUtil.containOne(columns, key, HumpUtil.hump2underline(key));
            if (contain && !auth(key, dataFilter)) {
                filter = false;
                break;
            }
        }
        // 再置空
        QuickUtil.isFalse(filter, () -> ClassUtil.setValue(o, field, null));
    }

    /**
     * @param column
     * @param dataFilter
     * @return 返回true则该字段将置空
     */
    private synchronized boolean auth(String column, DataFilter dataFilter) {
        return FutureAssist.supplyAsync(() -> dataAuthServer.colFilter(dataFilter.flag(), column));
    }
}
