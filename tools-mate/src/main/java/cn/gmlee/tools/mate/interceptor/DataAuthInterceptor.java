package cn.gmlee.tools.mate.interceptor;


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
import java.time.LocalDateTime;
import java.util.*;

/**
 * 数据鉴权拦截器.
 * <p>
 * 功能包含: 数据(行、列)权限, 数据编解码
 *
 * @Signature method sequence: query > setParameters > handleResultSets
 * </p>
 */
@Intercepts({
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class}),
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class,}),
        @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class}),
        @Signature(type = ParameterHandler.class, method = "setParameters", args = {PreparedStatement.class}),
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
                return null;
//                return encodeSetParameters(invocation);
            }
            case "query": {
                encodeQuery(invocation);
                return rowDataAuth(invocation);
            }
            case "handleResultSets": {
                return columnDataAuth(invocation);
            }
        }
        return null;
    }

    // -----------------------------------------------------------------------------------------------------------------

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
        encode((Map<String, Object>) po, new HashSet());
        return null;
    }

    private Object encodeSetParameters(Invocation invocation) {
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
            encode((Map<String, Object>) po, new HashSet<>());

        }
        return null;
    }

    private Object encodeQuery(Invocation invocation) {
        // 编码开关
        if (!codecServer.enable(CodecServer.ENCODE)) {
            return null;
        }
        // 加密
        if (invocation.getTarget() instanceof Executor) {
            Object po = invocation.getArgs()[1];
            if (po != null && !(po instanceof Map)) {
                String key = po.getClass().getSimpleName().toLowerCase();
                Map<String, Object> map = new HashMap();
                map.put(key, po);
                po = map;
            }
            encode((Map<String, Object>) po, new HashSet<>());
        }
        return null;
    }

    private void encode(Map<String, Object> parameterMap, Set<Object> set) {
        if (BoolUtil.isEmpty(parameterMap)) {
            return;
        }
        // 已加密对象
        Iterator<Map.Entry<String, Object>> it = parameterMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Object> next = it.next();
            Object value = next.getValue();
            if (set.contains(value) && !(value instanceof String)) {
                continue;
            }
            String encode = encode(next.getKey(), value, set);
            if (encode != null) {
                parameterMap.put(next.getKey(), encode);
            }
        }
    }

    private String encode(String key, Object val, Set set) {
        // 支持String入参编码
        if (val instanceof String && codecServer.enable(CodecServer.ENCODE_STRING)) {
            return codecServer.encode(null, key, val);
        }
        // 重复编码检查
        if (set.contains(val) && !(val instanceof String)) {
            return null;
        }
        if (val instanceof Map) {
            Iterator<? extends Map.Entry<?, ?>> it = ((Map<?, ?>) val).entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<?, ?> next = it.next();
                Object newKey = next.getKey();
                if (!(newKey instanceof String)) {
                    continue;
                }
                Object newVal = next.getValue();
                if (set.contains(newVal)) {
                    continue;
                }
                String encode = encode((String) newKey, newVal, set);
                if (encode != null) {
                    ((Map) val).put(newKey, newVal);
//                    set.add(newVal);
                }
            }
            set.add(val);
            return null;
        }
        if (val instanceof List) {
            Iterator it = new ArrayList((List) val).iterator();
            while (it.hasNext()) {
                Object value = it.next();
                if (set.contains(value)) {
                    continue;
                }
                String encode = encode(key, value, set);
                if (encode != null) {
                    ((List<?>) val).remove(value);
                    ((List) val).add(encode);
//                    set.add(value);
                }
            }
            set.add(val);
            return null;
        }
        if (val instanceof Object[]) {
            for (int i = 0; i < ((Object[]) val).length; i++) {
                Object value = ((Object[]) val)[i];
                if (set.contains(value)) {
                    continue;
                }
                String encode = encode(key, value, set);
                if (encode != null) {
                    ((Object[]) val)[i] = encode;
//                    set.add(value);
                }
            }
            set.add(val);
            return null;
        }
        encodeObj(val, set);
        return null;
    }

    private void encodeObj(Object o, Set set) {
        // 忽略基础字段
        if (o == null ||
                BoolUtil.isBaseClass(o,
                        String.class,
                        Date.class,
                        BigDecimal.class,
                        LocalDateTime.class
                )
        ) {
            return;
        }
        // 如果是Map
        if (o instanceof Map) {
            Map<String, Object> valuesMap = (Map<String, Object>) o;
            Iterator<Map.Entry<String, Object>> it = valuesMap.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, Object> next = it.next();
                String key = next.getKey();
                Object value = next.getValue();
                if (set.contains(value) && !(value instanceof String)) {
                    continue;
                }
                // 字段编码处理
                String encode = codecServer.encode(o, key, value);
                if (encode != null) {
                    valuesMap.put(key, encode);
//                    set.add(value);
                }
            }
        }
        // 获取所有字段
        Map<String, Field> fieldsMap = ClassUtil.getFieldsMap(o);
        Iterator<Map.Entry<String, Field>> it = fieldsMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Field> next = it.next();
            String key = next.getKey();
            Field field = next.getValue();
            // 字段编码处理
            Object value = ClassUtil.getValue(o, field);
            if (set.contains(value) && !(value instanceof String)) {
                continue;
            }
            String encode = codecServer.encode(o, key, value);
            if (encode != null) {
                ClassUtil.setValue(o, field, encode);
//                set.add(value);
            }
        }
        set.add(o);
    }

    // -----------------------------------------------------------------------------------------------------------------

    private Object rowDataAuth(Invocation invocation) throws Exception {
        String[] row = DataScopeUtil.getRow();
        if (BoolUtil.notEmpty(row)) {
            BoundSql boundSql = SqlResetAssist.getBoundSql(invocation);
            if (BoolUtil.isNull(boundSql)) {
                return null;
            }
            // 设行权限
            String sql = rowDataAuthAll(boundSql.getSql(), row);
            // 重置句柄
            SqlResetAssist.resetSql(invocation, sql, dataAuthServer.printSql());
        }
        return null;
    }

    private String rowDataAuthAll(String originalSql, String... flags) throws Exception {
        // 遍历标志位
        for (String flag : flags) {
            // 获取行权限数据
            Map<String, List> wheres = FutureAssist.supplyAsync(() -> dataAuthServer.rowWheres(flag));
            // 添加行权限句柄
            originalSql = SqlUtil.newSelect(originalSql, wheres);
        }
        // 返回新句柄
        return originalSql;
    }

    // -----------------------------------------------------------------------------------------------------------------

    private Object columnDataAuth(Invocation invocation) throws Exception {
        String[] column = DataScopeUtil.getCol();
        // 数据权限和解码关
        if (BoolUtil.isEmpty(column) && !codecServer.enable(CodecServer.DECODE)) {
            return null;
        }
        DefaultResultSetHandler defaultResultSetHandler = PluginUtils.realTarget(invocation.getTarget());
        MetaObject metaStatementHandler = SystemMetaObject.forObject(defaultResultSetHandler);
        MappedStatement mappedStatement = (MappedStatement) metaStatementHandler.getValue("mappedStatement");
        // 结果集处理
        Statement statement = (Statement) invocation.getArgs()[0];
        List<Object> objects = defaultResultSetHandler.handleResultSets(statement);
        return columnDataAuthAll(objects, NullUtil.get(column, new String[0]));
    }

    private Object columnDataAuthAll(List<Object> objects, String... flags) throws Exception {
        if (BoolUtil.notEmpty(objects)) {
            Iterator<Object> it = objects.iterator();
            while (it.hasNext()) {
                Object o = it.next();
                if (o == null ||
                        BoolUtil.isBaseClass(
                                o,
                                String.class,
                                Date.class,
                                BigDecimal.class,
                                LocalDateTime.class
                        )
                ) {
                    continue;
                }
                // 如果是Map
                if (o instanceof Map) {
                    Map<String, Object> valuesMap = (Map<String, Object>) o;
                    Iterator<Map.Entry<String, Object>> iterator = valuesMap.entrySet().iterator();
                    while (iterator.hasNext()) {
                        Map.Entry<String, Object> next = iterator.next();
                        String key = next.getKey();
                        Object value = next.getValue();
                        // 字段编码处理
                        String decode = codecServer.decode(o, key, value);
                        if (decode != null) {
                            valuesMap.put(key, decode);
                        }
                    }
                    continue;
                }
                // 获取所有字段
                Map<String, Field> fieldsMap = ClassUtil.getFieldsMap(o);
                Iterator<Map.Entry<String, Field>> iterator = fieldsMap.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<String, Field> entry = iterator.next();
                    String key = entry.getKey();
                    Field field = entry.getValue();
                    // 数据权限处理
                    filterColumns(o, key, field, flags);
                    // 字段解码处理
                    Object value = ClassUtil.getValue(o, field);
                    String decode = codecServer.decode(o, key, value);
                    if (decode != null) {
                        ClassUtil.setValue(o, field, decode);
                    }
                }
            }
        }
        return objects;
    }

    private void filterColumns(Object o, String key, Field field, String... flags) {
        boolean filter = true;
        for (String flag : flags) {
            List<String> columns = FutureAssist.supplyAsync(() -> dataAuthServer.colFields(flag));
            boolean contain = BoolUtil.containOne(columns, key, HumpUtil.hump2underline(key));
            if (contain && !auth(key, flag)) {
                filter = false;
                break;
            }
        }
        // 再置空
        QuickUtil.isFalse(filter, () -> ClassUtil.setValue(o, field, null));
    }

    /**
     * @param column
     * @param flag
     * @return 返回true则该字段将置空
     */
    private synchronized boolean auth(String column, String flag) {
        return FutureAssist.supplyAsync(() -> dataAuthServer.colFilter(flag, column));
    }

    // -----------------------------------------------------------------------------------------------------------------

}
