package cn.gmlee.tools.dt.core;

import cn.gmlee.tools.base.util.BoolUtil;
import cn.gmlee.tools.dt.dao.entity.Tx;
import cn.gmlee.tools.dt.dao.entity.TxInfo;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 全局事务支持.
 */
public class TxSupport {
    private static final ThreadLocal<Tx> tx = new InheritableThreadLocal();
    private static final ThreadLocal<String> GLOBAL_CODE = new InheritableThreadLocal();
    private static final ThreadLocal<String> SUPERIOR_CODE = new InheritableThreadLocal();
    private static final Map<String, TxInfo> LOCAL_TX_INFO = new ConcurrentHashMap();
    /**
     * 全局事务是否提交
     * <p>
     * true: 提交
     * false: 回滚
     * null: 等待
     * </p>
     */
    private static final Map<String, List<String>> globalOp = new ConcurrentHashMap();

    /**
     * 查询操作指令.
     *
     * @param global the global
     * @return the global cmd
     */
    public static Boolean getOp(Tx global) {
        List<String> list = globalOp.get(global.getCode());
        if (BoolUtil.isEmpty(list)) {
            return null;
        }
        if (global.getCount() <= list.size()) {
            return null;
        }
        return true;
    }


    /**
     * Save tx info.
     *
     * @param tx     the tx
     * @param txInfo the tx info
     */
    public static void saveLocalTxInfo(Tx tx, TxInfo txInfo) {
        txInfo.setTx(tx);
        LOCAL_TX_INFO.put(tx.getCode(), txInfo);
    }

    /**
     * Save.
     *
     * @param globalCode the global code
     */
    public static void saveGlobalCode(String globalCode) {
        GLOBAL_CODE.set(globalCode);
    }

    /**
     * Save superior code.
     *
     * @param superiorCode the superior code
     */
    public static void saveSuperiorCode(String superiorCode) {
        SUPERIOR_CODE.set(superiorCode);
    }

    /**
     * Save tx info.
     *
     * @param tx the tx
     */
    public static void saveTx(Tx tx) {
        TxSupport.tx.set(tx);
    }

    /**
     * Gets tx.
     *
     * @return the tx
     */
    public static Tx getTx() {
        return tx.get();
    }

    /**
     * Get.
     *
     * @param globalCode the global code
     * @return the tx info
     */
    public static TxInfo getLocalTxInfo(String globalCode) {
        return LOCAL_TX_INFO.get(globalCode);
    }

    /**
     * Get.
     *
     * @return the string
     */
    public static String getGlobalCode() {
        return GLOBAL_CODE.get();
    }

    /**
     * Get superior code string.
     *
     * @return the string
     */
    public static String getSuperiorCode() {
        return SUPERIOR_CODE.get();
    }

    /**
     * Is global boolean.
     *
     * @param tx the tx
     * @return the boolean
     */
    public static boolean isGlobal(Tx tx) {
        String globalCode = tx.getGlobalCode();
        String code = tx.getCode();
        return BoolUtil.eq(code, globalCode);
    }

    /**
     * Is observe boolean.
     *
     * @param tx the tx
     * @return the boolean
     */
    public static boolean isObserve(Tx tx) {
        return tx.getCount() > 0;
    }

    /**
     * 清理线程数据.
     * <p>
     * 全局数据如TX_INFO不清理.
     * </p>
     */
    public static void clear() {
        GLOBAL_CODE.remove();
        SUPERIOR_CODE.remove();
        tx.remove();
    }
}
