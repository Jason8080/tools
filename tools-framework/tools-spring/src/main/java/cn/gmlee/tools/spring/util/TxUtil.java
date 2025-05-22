package cn.gmlee.tools.spring.util;

import cn.gmlee.tools.base.enums.Function;
import cn.gmlee.tools.base.util.AssertUtil;
import cn.gmlee.tools.base.util.ExceptionUtil;
import cn.gmlee.tools.base.util.NullUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * 事务工具.
 */
@Slf4j
public class TxUtil {
    /**
     * 打印当前事务信息.
     *
     * @param tips the tips
     */
    public static void print(String... tips) {
        String tip = String.join(" • ", NullUtil.get(tips, new String[]{"事务信息"}));
        boolean actualTransactionActive = TransactionSynchronizationManager.isActualTransactionActive();
        boolean currentTransactionReadOnly = TransactionSynchronizationManager.isCurrentTransactionReadOnly();
        String currentTransactionName = TransactionSynchronizationManager.getCurrentTransactionName();
        Integer currentTransactionIsolationLevel = TransactionSynchronizationManager.getCurrentTransactionIsolationLevel();
        log.info("{} -> tx: {}, active: {}, read-only: {}, level: {}", tip, currentTransactionName, actualTransactionActive, currentTransactionReadOnly, currentTransactionIsolationLevel);
    }

    /**
     * 条件提交 (编程式事务)
     *
     * @param condition 条件
     * @param run       任务
     */
    public static void submit(boolean condition, Function.Zero run) {
        TransactionTemplate transactionTemplate = IocUtil.getBean(TransactionTemplate.class);
        AssertUtil.notNull(transactionTemplate, "当前项目没有事务环境");
        transactionTemplate.executeWithoutResult(ts -> {
            ExceptionUtil.sandbox(run);
            if (!condition) {
                ts.setRollbackOnly();
            }
        });
    }

    /**
     * 条件提交 (编程式事务)
     *
     * @param <T>       泛型
     * @param condition 条件
     * @param run       任务
     * @return t 执行结果
     */
    public static <T> T submit(boolean condition, Function.Zero2r<T> run) {
        TransactionTemplate transactionTemplate = IocUtil.getBean(TransactionTemplate.class);
        AssertUtil.notNull(transactionTemplate, "当前项目没有事务环境");
        return transactionTemplate.execute(ts -> {
            T result = ExceptionUtil.sandbox(run);
            if (!condition) {
                ts.setRollbackOnly();
            }
            return result;
        });
    }
}
