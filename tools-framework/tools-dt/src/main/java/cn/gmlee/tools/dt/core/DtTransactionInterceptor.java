package cn.gmlee.tools.dt.core;

import cn.gmlee.tools.dt.enums.Status;
import cn.gmlee.tools.dt.dao.entity.Tx;
import cn.gmlee.tools.dt.dao.entity.TxInfo;
import cn.gmlee.tools.dt.ex.GlobalTransactionTimeoutException;
import cn.gmlee.tools.dt.server.CcServer;
import cn.gmlee.tools.dt.server.TxServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.interceptor.TransactionAttribute;
import org.springframework.transaction.interceptor.TransactionInterceptor;

import javax.annotation.Resource;

/**
 * 事务拦截器.
 */
public class DtTransactionInterceptor extends TransactionInterceptor {

    private static final Logger log = LoggerFactory.getLogger(DtTransactionInterceptor.class);

    @Resource
    private TxServer txServer;

    @Resource
    private CcServer ccServer;

    @Override
    protected TransactionInfo createTransactionIfNecessary(PlatformTransactionManager tm, TransactionAttribute txAttr, String s) {
        return begin(super.createTransactionIfNecessary(tm, txAttr, s));
    }

    @Override
    protected void commitTransactionAfterReturning(TransactionInfo txInfo) {
        try {
            int commit = commit(txInfo);
            // 全局失败则回滚
            if (commit == 0) {
                txInfo.getTransactionManager().rollback(txInfo.getTransactionStatus());
            }
            if (commit == 1) {
                txInfo.getTransactionManager().commit(txInfo.getTransactionStatus());
            }
        } finally {
            // 线程数据清理
            TxSupport.clear();
        }
    }

    @Override
    protected void completeTransactionAfterThrowing(TransactionInfo txInfo, Throwable ex) {
        try {
            int commit = complete(txInfo, ex);
            // 全局失败则回滚
            if (commit == 0) {
                txInfo.getTransactionManager().rollback(txInfo.getTransactionStatus());
            }
            if (commit == 1) {
                txInfo.getTransactionManager().commit(txInfo.getTransactionStatus());
            }
        } finally {
            // 线程数据清理
            TxSupport.clear();
        }
    }

    /**
     * 开启.
     *
     * @param info the info
     * @return the transaction info
     */
    public TransactionInfo begin(TransactionInfo info) {
        TxInfo txInfo = TxInfo.build(info.getTransactionManager(), info.getTransactionStatus(), info.getTransactionAttribute(), info.getJoinpointIdentification());
        txServer.create(txInfo);
        return info;
    }

    /**
     * 回滚或提交.
     *
     * @param txInfo the txInfo
     * @param ex     the ex
     * @return
     */
    public int complete(TransactionInfo txInfo, Throwable ex) {
        if (txInfo != null && txInfo.getTransactionStatus() != null) {
            if (txInfo.getTransactionAttribute() != null && txInfo.getTransactionAttribute().rollbackOn(ex)) {
                rollback(txInfo, ex);
                return 0;
            }
            return commit(txInfo);
        }
        return -1;
    }

    /**
     * 提交.
     *
     * @param info the info
     * @return
     */
    public int commit(TransactionInfo info) throws GlobalTransactionTimeoutException {
        Tx tx = TxSupport.getTx();
        if (tx == null) {
            return -1;
        }
        // 不需要观察的事务
        if (!TxSupport.isObserve(tx)) {
            ccServer.commitNotify(tx);
            log.info("事务已返回: {}", tx.getCode());
            return -1;
        }
        // 需要观察事务提交
        boolean ret = txServer.observeGlobal(tx);
        // 变更当前和全局的状态
        if (ret) {
            // 当前只需要处理提交(回滚后续自行处理)
            txServer.updateStateByCode(tx.getCode(), Status.COMMIT);
            // 收尾工作
            log.info("事务已提交: {}", tx.getCode());
        } else {
            log.info("事务已回滚: {}", tx.getCode());
        }
        return ret ? 1 : 0;
    }

    /**
     * 回滚.
     *
     * @param info the info
     * @param ex   the ex
     */
    public void rollback(TransactionInfo info, Throwable ex) {
        Tx tx = TxSupport.getTx();
        if (tx == null) {
            return;
        }
        // 变更当前和全局的状态
        txServer.updateStateByCode(tx.getGlobalCode(), Status.ROLLBACK);
        // 收尾工作
        log.info("事务已回滚: {}", tx.getCode());
    }
}
