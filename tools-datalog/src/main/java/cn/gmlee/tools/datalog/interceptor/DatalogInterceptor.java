package cn.gmlee.tools.datalog.interceptor;

import cn.gmlee.tools.base.assist.SqlAssist;
import cn.gmlee.tools.base.util.BoolUtil;
import cn.gmlee.tools.base.util.JsonUtil;
import cn.gmlee.tools.datalog.anno.Ignore;
import cn.gmlee.tools.datalog.anno.Open;
import cn.gmlee.tools.datalog.aop.DatalogAspect;
import cn.gmlee.tools.datalog.assist.DatalogAssist;
import cn.gmlee.tools.datalog.model.Datalog;
import cn.gmlee.tools.datalog.model.LogApi;
import cn.gmlee.tools.datalog.model.LogData;
import cn.gmlee.tools.mybatis.dao.IBatisDao;
import lombok.SneakyThrows;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMap;
import org.apache.ibatis.plugin.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 数据更新拦截器
 * </p>
 *
 * @author Jas °
 * @since 2020 /5/11
 */
@Intercepts({
        @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class}),
        @Signature(type = Executor.class, method = "commit", args = {boolean.class}),
        @Signature(type = Executor.class, method = "rollback", args = {boolean.class}),
        @Signature(type = Executor.class, method = "close", args = {boolean.class}),
})
public class DatalogInterceptor implements Interceptor {

    private Logger logger = LoggerFactory.getLogger(DatalogInterceptor.class);

    @Value("${tools.datalog.primaryKey:id}")
    private String primaryKey;

    @Value("${tools.datalog.noteComment:true}")
    private Boolean noteComment;

    @Value("${tools.datalog.useIgnore:true}")
    private Boolean useIgnore;

    @Value("${tools.datalog.statement.selectList:selectList}")
    private String selectListStatement;

    @Value("${tools.datalog.statement.forUpdate:false}")
    private Boolean forUpdate;

    private IBatisDao iBatisDao;

    private AbstractDatalogInterceptorHandler abstractDatalogInterceptorHandler;

    private ThreadLocal<List<LogData>> lds = new ThreadLocal();

    /**
     * Sets i batis dao.
     *
     * @param iBatisDao the batis dao
     */
    public void setIBatisDao(IBatisDao iBatisDao) {
        this.iBatisDao = iBatisDao;
    }

    /**
     * Gets i batis dao.
     *
     * @return the i batis dao
     */
    public IBatisDao getIBatisDao() {
        return iBatisDao;
    }

    /**
     * Sets abstract datalog interceptor handler.
     *
     * @param abstractDatalogInterceptorHandler the abstract datalog interceptor handler
     */
    public void setAbstractDatalogInterceptorHandler(AbstractDatalogInterceptorHandler abstractDatalogInterceptorHandler) {
        this.abstractDatalogInterceptorHandler = abstractDatalogInterceptorHandler;
    }

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

    @Override
    @SneakyThrows
    public final Object intercept(Invocation invocation) {
        try {
            datalog(invocation);
        } catch (Throwable throwable) {
            logger.warn(String.format("数据变更日志分析出错"), throwable);
        }
        return invocation.proceed();
    }

    private boolean datalog(Invocation invocation) {
        Method method = invocation.getMethod();
        switch (method.getName()) {
            case "update": {
                return recordUpdateDatalog((MappedStatement) invocation.getArgs()[0], invocation.getArgs()[1]);
            }
            case "close": {
                return closeUpdateDatalog((Boolean) invocation.getArgs()[0]);
            }
            case "commit": {
                return commitUpdateDatalog((Boolean) invocation.getArgs()[0]);
            }
            case "rollback": {
                return rollbackUpdateDatalog((Boolean) invocation.getArgs()[0]);
            }
        }
        return true;
    }

    /**
     * Record update datalog boolean.
     *
     * @param ms  the ms
     * @param arg the arg
     * @return the boolean
     */
    private boolean recordUpdateDatalog(MappedStatement ms, Object arg) {
        ParameterMap parameterMap = ms.getParameterMap();
        Class<?> clazz = parameterMap.getType();
        if (useIgnore) {
            if (ignore(arg, clazz)) {
                handlerUpdateDatalog(ms, arg);
            }
        } else {
            if (open(arg, clazz)) {
                handlerUpdateDatalog(ms, arg);
            }
        }
        return true;
    }

    private boolean ignore(Object arg, Class<?> clazz) {
        return (clazz != null && clazz.getAnnotation(Ignore.class) == null)
                || (arg != null && arg.getClass().getAnnotation(Ignore.class) == null);
    }

    private boolean open(Object arg, Class<?> clazz) {
        return (clazz != null && clazz.getAnnotation(Open.class) != null)
                || (arg != null && arg.getClass().getAnnotation(Open.class) != null);
    }

    /**
     * Handler update datalog.
     *
     * @param ms  the ms
     * @param arg the arg
     */
    protected boolean handlerUpdateDatalog(MappedStatement ms, Object arg) {
        switch (ms.getSqlCommandType()) {
            case INSERT: {
                return true;
            }
            case UPDATE: {
                return generatorUpdateDatalog(ms, arg);
            }
            case DELETE: {
                return generatorDeleteDatalog(ms, arg);
            }
            default:
                return false;
        }
    }

    /**
     * Close update datalog boolean.
     *
     * @param required the required
     * @return the boolean
     */
    protected boolean closeUpdateDatalog(Boolean required) {
        return true;
    }

    /**
     * Commit update datalog boolean.
     *
     * @param required the required
     * @return the boolean
     */
    protected boolean commitUpdateDatalog(Boolean required) {
        if (abstractDatalogInterceptorHandler == null) {
            return false;
        }
        List<Datalog> list = DatalogAssist.classForDatalog(lds, abstractDatalogInterceptorHandler.getDatalogClazz());
        if (BoolUtil.notEmpty(list)) {
            long currentMs = System.currentTimeMillis();
            list.forEach(datalog -> datalog.setTotalMs(currentMs - datalog.getStartMs()));
            return abstractDatalogInterceptorHandler.commit(list);
        }
        return true;
    }

    /**
     * Rollback update datalog boolean.
     *
     * @param required the required
     * @return the boolean
     */
    protected boolean rollbackUpdateDatalog(Boolean required) {
        if (abstractDatalogInterceptorHandler == null) {
            return false;
        }
        List<Datalog> list = DatalogAssist.classForDatalog(lds, abstractDatalogInterceptorHandler.getDatalogClazz());
        if (BoolUtil.notEmpty(list)) {
            long currentMs = System.currentTimeMillis();
            list.forEach(datalog -> datalog.setTotalMs(currentMs - datalog.getStartMs()));
            return abstractDatalogInterceptorHandler.rollback(list);
        }
        return true;
    }


    /**
     * 分析数据更新日志
     *
     * @param ms  句柄
     * @param arg 执行参数对象
     */
    protected boolean generatorUpdateDatalog(MappedStatement ms, Object arg) {
        long startMs = System.currentTimeMillis();
        // 解析执行sql
        String originalSql = DatalogAssist.getOriginalSql(ms, arg);
        // 获取条件 (用于查询)
        String whereSql = DatalogAssist.getWhereSql(originalSql);
        // 解析数据 (新数据)
        String setSql = DatalogAssist.getSetSql(originalSql);
        Map<String, String> newData = DatalogAssist.getSetMap(setSql);
        String tables = String.join(",", new SqlAssist(originalSql).tables());
        // 查询语句 (旧数据)
        String selectSql = DatalogAssist.getSelectSql(tables, whereSql);
        // 封装日志
        LogApi logApi = DatalogAspect.get();
        LogData logData = new LogData(logApi, newData);
        logData.setStartMs(startMs);
        logData.setOriginalSql(originalSql);
        logData.setWhereSql(whereSql);
        logData.setNewDataJson(JsonUtil.toJson(newData));
        logData.setDatalogSelectSql(selectSql);
        logData.setDataTable(tables);
        add(logData);
        return true;
    }

    /**
     * Generator delete datalog.
     *
     * @param ms  the ms
     * @param arg the arg
     */
    protected boolean generatorDeleteDatalog(MappedStatement ms, Object arg) {
        long startMs = System.currentTimeMillis();
        // 解析执行sql
        String originalSql = DatalogAssist.getOriginalSql(ms, arg);
        // 获取条件 (用于查询)
        String whereSql = DatalogAssist.getWhereSql(originalSql);
        // 查询语句 (旧数据)
        String datalogSelectSql = DatalogAssist.getDatalogSelectSql(ms, selectListStatement, whereSql, forUpdate);
        // 封装日志
        LogApi logApi = DatalogAspect.get();
        LogData logData = new LogData(logApi, Collections.EMPTY_MAP);
        logData.setStartMs(startMs);
        logData.setOriginalSql(originalSql);
        logData.setWhereSql(whereSql);
        logData.setDatalogSelectSql(datalogSelectSql);
        String tables = new SqlAssist(originalSql).tables().toString();
        logData.setDataTable(tables.substring(1, tables.length() - 1));
        add(logData);
        return true;
    }

    private void add(LogData logData) {
        List<LogData> list = init();
        list.add(logData);
    }

    private List<LogData> init() {
        List<LogData> list = lds.get();
        if (list == null) {
            list = new ArrayList();
            lds.set(list);
        }
        return list;
    }
}
