package cn.gmlee.tools.spring.util;

import cn.gmlee.tools.base.util.NullUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * 事务工具.
 */
@Slf4j
public class TxUtil {
    /**
     * 打印当前事务信息.
     */
    public static void print(String... tips) {
        String tip = String.join(" • ", NullUtil.get(tips, new String[]{"事务信息"}));
        boolean actualTransactionActive = TransactionSynchronizationManager.isActualTransactionActive();
        boolean currentTransactionReadOnly = TransactionSynchronizationManager.isCurrentTransactionReadOnly();
        String currentTransactionName = TransactionSynchronizationManager.getCurrentTransactionName();
        Integer currentTransactionIsolationLevel = TransactionSynchronizationManager.getCurrentTransactionIsolationLevel();
        log.info("{} -> tx: {}, active: {}, read-only: {}, level: {}", tip, currentTransactionName, actualTransactionActive, currentTransactionReadOnly, currentTransactionIsolationLevel);
    }
}
