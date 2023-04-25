package cn.gmlee.tools.dt.repository;

import cn.gmlee.tools.base.util.BeanUtil;
import cn.gmlee.tools.base.util.BoolUtil;
import cn.gmlee.tools.base.util.JdbcUtil;
import cn.gmlee.tools.dt.dao.entity.Tx;
import cn.gmlee.tools.dt.kit.SqlKit;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * The type Tx repository.
 */
public class TxRepository {

    private static final Logger log = LoggerFactory.getLogger(TxRepository.class);

    @Getter
    @Resource
    private DataSource dataSource;

    /**
     * Create global tx.
     *
     * @param entity the entity
     * @return the tx
     */
    public Tx create(Tx entity) {
        Connection con = JdbcUtil.newGet(dataSource, false);
        List<Map<String, Object>> maps = JdbcUtil.execute(con, SqlKit.insertIgnoreInto(entity), true);
        if (!maps.isEmpty()) {
            Map<String, Object> ret = maps.get(0);
            if (BoolUtil.notEmpty(ret) && ret.containsKey("id")) {
                Object id = ret.get("id");
                if (id instanceof Number) {
                    entity.setId(((Number) id).longValue());
                }
            }
        }
        return entity;
    }

    /**
     * Auto increment count.
     *
     * @param entity the entity
     */
    public void autoIncrementCount(Tx entity) {
        Connection con = JdbcUtil.newGet(dataSource, false);
        List<Map<String, Object>> maps = JdbcUtil.execute(con, SqlKit.autoIncrementCount(entity), true);
        log(entity, maps);
    }

    /**
     * Update by id.
     *
     * @param entity the entity
     */
    public void updateById(Tx entity) {
        Connection con = JdbcUtil.newGet(dataSource, false);
        List<Map<String, Object>> maps = JdbcUtil.execute(con, SqlKit.updateById(entity), true);
        log(entity, maps);
    }

    /**
     * Update by global id.
     *
     * @param entity the entity
     */
    public void updateByGlobalCode(Tx entity) {
        Connection con = JdbcUtil.newGet(dataSource, false);
        List<Map<String, Object>> maps = JdbcUtil.execute(con, SqlKit.updateByGlobalCode(entity), true);
        // 打印日志
        log(entity, maps);
    }

    /**
     * Update by superior code.
     *
     * @param entity the entity
     */
    public void updateBySuperiorCode(Tx entity) {
        Connection con = JdbcUtil.newGet(dataSource, false);
        List<Map<String, Object>> maps = JdbcUtil.execute(con, SqlKit.updateBySuperiorCode(entity), true);
        // 打印日志
        log(entity, maps);
    }

    /**
     * Update by global code.
     *
     * @param entity the entity
     * @param con    the con
     */
    public void updateByGlobalCode(Tx entity, Connection con) {
        List<Map<String, Object>> maps = JdbcUtil.execute(con, SqlKit.updateByGlobalCode(entity), false);
        // 打印日志
        log(entity, maps);
    }

    /**
     * Update by code.
     *
     * @param entity the entity
     */
    public void updateByCode(Tx entity) {
        Connection con = JdbcUtil.newGet(dataSource, false);
        List<Map<String, Object>> maps = JdbcUtil.execute(con, SqlKit.updateByCode(entity), true);
        // 打印日志
        log(entity, maps);
    }

    /**
     * Update by code.
     *
     * @param entity the entity
     * @param con    the con
     */
    public void updateByCode(Tx entity, Connection con) {
        List<Map<String, Object>> maps = JdbcUtil.execute(con, SqlKit.updateByCode(entity), false);
        // 打印日志
        log(entity, maps);
    }

    private void log(Tx entity, List<Map<String, Object>> maps) {
        // 打印日志
        if (maps.isEmpty() || BoolUtil.isEmpty(maps.get(0))) {
            log.warn("事务更新失败: {}", entity);
            return;
        }
        // 打印日志
        Integer count = (Integer) maps.get(0).get("count");
        if (count < 1) {
            log.warn("事务更新失败: {}", entity);
            return;
        }
    }

    /**
     * Find global by code global.
     *
     * @param globalCode the global code
     * @param con        the con
     * @return the global
     */
    public List<Tx> listByGlobalCode(String globalCode, Connection con) {
        List<Map<String, Object>> maps = JdbcUtil.execute(con, SqlKit.listByGlobalCode(globalCode, true), false);
        // 封装全局事务
        return convertTxs(maps);
    }

    /**
     * List by superior code list.
     *
     * @param superiorCode the superior code
     * @param con          the con
     * @return the list
     */
    public List<Tx> listBySuperiorCode(String superiorCode, Connection con) {
        List<Map<String, Object>> maps = JdbcUtil.execute(con, SqlKit.listBySuperiorCode(superiorCode, true), false);
        return convertTxs(maps);
    }

    private List<Tx> convertTxs(List<Map<String, Object>> maps) {
        // 封装全局事务
        if (maps.isEmpty()) {
            return Collections.EMPTY_LIST;
        }
        // 封装本地事务
        List<Tx> list = new ArrayList(maps.size());
        for (Map<String, Object> map : maps) {
            list.add(BeanUtil.convert(map, Tx.class, false));
        }
        return list;
    }

    /**
     * 检查状态值.
     *
     * @param code the code
     * @param con  the con
     * @return the boolean
     */
    public Tx getByCode(String code, Connection con) {
        List<Map<String, Object>> maps = JdbcUtil.execute(con, SqlKit.getByCode(code, true), false);
        if (maps.isEmpty()) {
            return null;
        }
        Map<String, Object> map = maps.get(0);
        if (BoolUtil.isEmpty(map)) {
            return null;
        }
        return BeanUtil.convert(map, Tx.class, false);
    }
}
