package cn.gmlee.tools.dt.dao.entity;

import lombok.Data;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.interceptor.TransactionAttribute;

import java.io.Serializable;

/**
 * The type Tx info.
 */
@Data
public class TxInfo implements Serializable {
    private PlatformTransactionManager tm;
    private TransactionStatus ts;
    private TransactionAttribute ta;
    private Tx tx;
    private String point;

    /**
     * Build tx info.
     *
     * @param tm    the tm
     * @param ts    the ts
     * @param ta    the ta
     * @param point the point
     * @return the tx info
     */
    public static TxInfo build(PlatformTransactionManager tm, TransactionStatus ts, TransactionAttribute ta, String point){
        TxInfo info = new TxInfo();
        info.tm = tm;
        info.ts = ts;
        info.ta = ta;
        info.point = point;
        return info;
    }
}
