package cn.gmlee.tools.kv.mysql.redis;

import cn.gmlee.tools.base.util.BoolUtil;
import cn.gmlee.tools.base.util.ExceptionUtil;
import cn.gmlee.tools.base.util.JdbcUtil;
import cn.gmlee.tools.base.util.TimeUtil;
import cn.gmlee.tools.kv.FakeKv;
import cn.gmlee.tools.kv.cmd.MysqlFakeRedisConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 伪Redis工具.
 *
 * @author Jas °
 * @date 2021 /7/20 (周二)
 */
public class FakeRedis implements FakeKv<String, String> {

    private static final Logger logger = LoggerFactory.getLogger(FakeRedis.class);

    private DataSource dataSource;

    /**
     * Instantiates a new Fake redis.
     *
     * @param dataSource the data source
     */
    public FakeRedis(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * 防并发存储.
     * <p>
     * 仅当key不存在或已超时才可以
     * </p>
     *
     * @param k      the k
     * @param v      the v
     * @param expire the expire
     * @return the nx
     */
    @Override
    public synchronized Boolean setNx(String k, String v, Date expire) {
        Connection con = JdbcUtil.get(dataSource, false);
        try {
            List getResult = JdbcUtil.execute(con, MysqlFakeRedisConstant.getForUpdate, false, k);
            if (BoolUtil.isEmpty(getResult)) {
                List setNxResult = JdbcUtil.execute(con, MysqlFakeRedisConstant.setNx, false, k, v, expire);
                return JdbcUtil.getBool(setNxResult);
            } else {
                List updateExResult = JdbcUtil.execute(con, MysqlFakeRedisConstant.updateEx, false, v, expire, k);
                return JdbcUtil.getBool(updateExResult);
            }
        } catch (Exception e) {
            // 回滚
            JdbcUtil.rollback(con);
            logger.error(String.format("伪Redis发生了不可描述的错误"), e);
        }
        // 提交
        JdbcUtil.commit(con);
        return false;
    }

    @Override
    public synchronized Boolean set(String k, String v) {
        List result = JdbcUtil.execute(dataSource, MysqlFakeRedisConstant.set, true, k, v, v);
        return JdbcUtil.getBool(result);
    }

    @Override
    public synchronized Boolean setEx(String k, String v, Date expire) {
        List result = JdbcUtil.execute(dataSource, MysqlFakeRedisConstant.setEx, true, k, v, expire, v, expire);
        return JdbcUtil.getBool(result);
    }

    @Override
    public synchronized String get(String k) {
        List result = JdbcUtil.execute(dataSource, MysqlFakeRedisConstant.get, String.class, true, k);
        // 懒过期处理
        String timeStr = getVal(result, MysqlFakeRedisConstant.COLUMN_EXPIRE);
        Date expire = ExceptionUtil.sandbox(() -> TimeUtil.parseTime(timeStr));
        // 空内容视为不过期
        if(BoolUtil.lt(expire, TimeUtil.getCurrentDate())){
            delete(k);
            return null;
        }
        return getVal(result, MysqlFakeRedisConstant.COLUMN_VAL);
    }

    @Override
    public synchronized void delete(String k) {
        JdbcUtil.execute(dataSource, MysqlFakeRedisConstant.delete, true, k);
    }

    private synchronized <T> T getVal(List<Map<String, T>> result, String column, Class<T> clazz) {
        if (BoolUtil.notEmpty(result)) {
            Map<String, T> map = result.get(0);
            if (BoolUtil.notEmpty(map)) {
                return map.get(column);
            }
        }
        return null;
    }

    private synchronized String getVal(List<Map<String, String>> result, String column) {
        if (BoolUtil.notEmpty(result)) {
            Map<String, String> map = result.get(0);
            if (BoolUtil.notEmpty(map)) {
                return map.get(column);
            }
        }
        return null;
    }
}
