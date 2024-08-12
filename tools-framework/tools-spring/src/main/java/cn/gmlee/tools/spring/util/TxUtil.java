package cn.gmlee.tools.spring.util;

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
    public static void print() {
        boolean actualTransactionActive = TransactionSynchronizationManager.isActualTransactionActive();
        String currentTransactionName = TransactionSynchronizationManager.getCurrentTransactionName();
        Integer currentTransactionIsolationLevel = TransactionSynchronizationManager.getCurrentTransactionIsolationLevel();
        log.info("Transaction active: {}, name: {}, level: {}", actualTransactionActive, currentTransactionName, currentTransactionIsolationLevel);
    }
}
