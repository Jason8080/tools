package cn.gmlee.tools.datalog.interceptor;

import cn.gmlee.tools.base.mod.Login;
import cn.gmlee.tools.base.util.*;
import cn.gmlee.tools.datalog.model.Datalog;
import cn.gmlee.tools.mybatis.dao.IBatisDao;
import com.fasterxml.jackson.core.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 默认输出到控制台的实现
 *
 * @param <LOG> the type parameter
 * @author Jas °
 * @date 2021 /4/6 (周二)
 */
public abstract class AbstractDatalogInterceptorHandler<LOG extends Datalog> implements DatalogInterceptorHandler<LOG> {

    /**
     * The Logger.
     */
    Logger logger = LoggerFactory.getLogger(AbstractDatalogInterceptorHandler.class);

    /**
     * The Datalog clazz.
     */
    private Class<LOG> datalogClazz;

    /**
     * The Batis dao.
     */
    protected IBatisDao iBatisDao;

    private static final String insertDatalogStatementId = "cn.gmlee.tools.datalog.dao.mapper.DatalogMapper.insert";

    /**
     * Class for datalog datalog.
     *
     * @return the datalog
     */
    public final Class<LOG> getDatalogClazz() {
        try {
            if (this.datalogClazz == null) {
                Class<LOG> clazz = ClassUtil.getGenericClass(this);
                if (clazz != null) {
                    datalogClazz = clazz;
                    return this.datalogClazz;
                }
                datalogClazz = (Class<LOG>) Datalog.class;
            }
            return this.datalogClazz;
        } catch (Exception e) {
            return ExceptionUtil.cast(e);
        }
    }

    @Override
    public boolean commit(List<LOG> logs) {
        Login login = getLogin();
        for (LOG log : logs) {
            // 登陆信息
            ExceptionUtil.sandbox(() -> log.setUserId(login.getUid()));
            ExceptionUtil.sandbox(() -> log.setUsername(login.getUsername()));
            // 获取旧数据
            List<Map<String, Object>> oldsData = getOldsData(log);
            log.setOldDataJson(JsonUtil.toJson(oldsData));
            // 获取字段名
            Map<String, String> commentMap = getCommentMap(log, oldsData);
            log.loadDatalog("id", oldsData, JsonUtil.toBean(log.getNewDataJson(), new TypeReference<Map<String, Object>>() {
            }), commentMap);
            // 统计耗时
            long totalMs = System.currentTimeMillis() - log.getStartMs();
            log.setTotalMs(totalMs);
            // 记录时间
            log.setCreateAt(new Date());
            // 默认句柄
            defaultSave(log);
        }
        return true;
    }

    /**
     * Gets comment map.
     *
     * @param log      the log
     * @param oldsData the olds data
     * @return the comment map
     */
    private Map<String, String> getCommentMap(LOG log, List<Map<String, Object>> oldsData) {
        DataSource dataSource = getDataSource();
        List<Map<String, Object>> commentMap = JdbcUtil.exec(
                dataSource, String.format("show full columns from %s", log.getDataTable())
        );
        Map<String, String> fieldCommentMap = commentMap.stream().collect(
                Collectors.toMap(
                        map -> NullUtil.get(map.get("COLUMN_NAME"), map.get("Field")).toString(),
                        map -> NullUtil.get(map.get("COLUMN_COMMENT"), map.get("Comment")).toString()
                )
        );
        return fieldCommentMap;
    }

    /**
     * Gets olds data.
     *
     * @param log the log
     * @return the olds data
     */
    private List<Map<String, Object>> getOldsData(LOG log) {
        DataSource dataSource = getDataSource();
        List<Map<String, Object>> oldsData = JdbcUtil.exec(
                dataSource,
                log.getDatalogSelectSql()
        );
        return oldsData;
    }

    /**
     * Sets i batis dao.
     *
     * @param iBatisDao the batis dao
     */
    public void setIBatisDao(IBatisDao iBatisDao) {
        this.iBatisDao = iBatisDao;
    }


    /**
     * Save.
     *
     * @param datalog the datalog
     */
    public void defaultSave(Datalog datalog) {
        iBatisDao.execute(datalog, insertDatalogStatementId);
    }

    /**
     * Create data source data source.
     *
     * @return the data source
     */
    protected abstract DataSource getDataSource();

    /**
     * 获取登陆对象.
     *
     * @return the login
     */
    protected abstract Login getLogin();
}
