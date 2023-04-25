package cn.gmlee.tools.dt.server;

import cn.gmlee.tools.base.util.BoolUtil;
import cn.gmlee.tools.base.util.ExceptionUtil;
import cn.gmlee.tools.base.util.JdbcUtil;
import cn.gmlee.tools.base.util.NullUtil;
import cn.gmlee.tools.dt.core.TxSupport;
import cn.gmlee.tools.dt.dao.entity.Tx;
import cn.gmlee.tools.dt.dao.entity.TxInfo;
import cn.gmlee.tools.dt.enums.Status;
import cn.gmlee.tools.dt.ex.GlobalTransactionTimeoutException;
import cn.gmlee.tools.dt.conf.DtProperties;
import cn.gmlee.tools.dt.kit.TxKit;
import cn.gmlee.tools.dt.repository.TxRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.sql.Connection;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * The type Tx server.
 */
public class TxServer {

    private static final Logger log = LoggerFactory.getLogger(TxServer.class);

    @Resource
    private TxRepository txRepository;

    @Resource
    private DtProperties dtProperties;

    /**
     * 进行中.
     * <p>
     * 观察全局事务状态; 然后根据观察结果提交/回滚本地事务.
     * </p>
     *
     * @param superior the superior
     * @return boolean boolean
     */
    public boolean observeGlobal(Tx superior) {
        try {
            return observe(superior);
        } catch (GlobalTransactionTimeoutException e) {
            // 超时处理
            return timeout(superior);
        }
    }

    /**
     * 超时处理.
     *
     * <p>
     * 能否处理成回滚, 如果全局事务状态已经提交则仍然提交.
     * </p>
     *
     * @param superior
     * @return 是否提交
     */
    private boolean timeout(Tx superior) {
        // 数据库加锁
        Connection con = JdbcUtil.newGet(txRepository.getDataSource(), false);
        try {
            List<Tx> txs = txRepository.listBySuperiorCode(superior.getCode(), con);
            if (BoolUtil.isNull(txs)) {
                return false;
            }
            long count = txs.stream().filter(x -> !TxSupport.isGlobal(x) && x.getStatus() > Status.UNDERWAY.code).count();
            // 如果全部提交: 则继续提交
            if (superior.getCount() <= count) {
                return true;
            }
            // 如果可以回滚: 则全局回滚
            this.updateStateByCode(superior.getGlobalCode(), Status.ROLLBACK, con);
            JdbcUtil.commit(con, true);
            return false;
        } finally {
            ExceptionUtil.suppress(() -> con.close());
        }
    }

    /**
     * 实时观察.
     *
     * @param global
     * @return
     */
    private boolean observe(Tx global) {
        // 在线程可用期限内不断检查变量情况
        long time = global.getTimeout() > 0 ? global.getTimeout() : dtProperties.getTimeout();
        long millis = TimeUnit.SECONDS.toMillis(time);
        long mark = System.currentTimeMillis();
        long target = mark + millis;
        while (true) {
            // 超时检查
            long current = System.currentTimeMillis();
            if (current < target) {
                throw new GlobalTransactionTimeoutException();
            }
            Boolean op = TxSupport.getOp(global);
            // 是否已经有结果
            if (op instanceof Boolean) {
                return op;
            }
        }
    }

    /**
     * 创建事务.
     *
     * @param txInfo the tx info
     * @return tx tx
     */
    public Tx create(TxInfo txInfo) {
        // 创建事务
        Tx current = createGlobalTx(txInfo);
        // 标记全局事务
        if (TxSupport.isGlobal(current)) {
            TxSupport.saveGlobalCode(current.getCode());
        } else {
            TxSupport.saveLocalTxInfo(current, txInfo);
        }
        TxSupport.saveSuperiorCode(current.getCode());
        TxSupport.saveTx(current);
        return current;
    }

    /**
     * 更新状态.
     *
     * @param id     the id
     * @param status the status
     */
    public void updateState(Long id, Status status) {
        Tx entity = new Tx();
        entity.setId(id);
        entity.setStatus(status.code);
        entity.setState(status.desc);
        txRepository.updateById(entity);
    }

    /**
     * 更新全局事务状态.
     *
     * @param globalCode the global code
     * @param status     the status
     */
    public void updateStateByGlobalCode(String globalCode, Status status) {
        Tx entity = new Tx();
        entity.setGlobalCode(globalCode);
        entity.setStatus(status.code);
        entity.setState(status.desc);
        txRepository.updateByGlobalCode(entity);

    }

    /**
     * Update state by superior code.
     *
     * @param superiorCode the superior code
     * @param status       the status
     */
    public void updateStateBySuperiorCode(String superiorCode, Status status) {
        Tx entity = new Tx();
        entity.setSuperiorCode(superiorCode);
        entity.setStatus(status.code);
        entity.setState(status.desc);
        txRepository.updateBySuperiorCode(entity);
    }

    /**
     * Update state by code.
     *
     * @param code   the code
     * @param status the status
     */
    public void updateStateByCode(String code, Status status) {
        Tx entity = new Tx();
        entity.setCode(code);
        entity.setStatus(status.code);
        entity.setState(status.desc);
        txRepository.updateByCode(entity);
    }

    /**
     * Update state by global code.
     *
     * @param globalCode the global code
     * @param status     the status
     * @param con        the con
     */
    public void updateStateByGlobalCode(String globalCode, Status status, Connection con) {
        Tx entity = new Tx();
        entity.setGlobalCode(globalCode);
        entity.setStatus(status.code);
        entity.setState(status.desc);
        txRepository.updateByGlobalCode(entity, con);
    }

    /**
     * Update state by code.
     *
     * @param code   the code
     * @param status the status
     * @param con    the con
     */
    public void updateStateByCode(String code, Status status, Connection con) {
        Tx entity = new Tx();
        entity.setCode(code);
        entity.setStatus(status.code);
        entity.setState(status.desc);
        txRepository.updateByCode(entity, con);
    }

    /**
     * Auto increment count.
     *
     * @param code the code
     */
    public void autoIncrementCount(String code) {
        // 本地数+1
        Tx tx = TxSupport.getTx();
        if (BoolUtil.eq(code, tx.getCode())) {
            tx.setCount(tx.getCount() + 1);
        }
        // 数据库+1
        Tx entity = new Tx();
        entity.setCode(code);
        txRepository.autoIncrementCount(entity);
    }

    private Tx createGlobalTx(TxInfo txInfo) {
        String code = TxKit.generateCode(txInfo);
        Tx entity = new Tx();
        entity.setCode(code);
        entity.setIp(dtProperties.getIp());
        entity.setPort(dtProperties.getPort());
        entity.setAppId(dtProperties.getAppId());
        // 全局事务编码
        entity.setGlobalCode(NullUtil.get(TxSupport.getGlobalCode(), code));
        // 上级事务编码
        entity.setSuperiorCode(TxSupport.getSuperiorCode());
        // 内部事务数量 (优化默认值, 不影响业务)
        entity.setCount(0);
        entity.setIsolation(txInfo.getTa().getIsolationLevel());
        entity.setPropagation(txInfo.getTa().getPropagationBehavior());
        entity.setTimeout(txInfo.getTa().getTimeout());
        entity.setSite(txInfo.getPoint());
        entity.setArgs(null);
        entity.setRet(null);
        // 默认值
        entity.setStatus(Status.UNDERWAY.code);
        entity.setState(Status.UNDERWAY.desc);
        txRepository.create(entity);
        return entity;
    }
}
