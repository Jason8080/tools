package cn.gmlee.tools.dt.dao.entity;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.interceptor.TransactionAttribute;

import java.io.Serializable;

/**
 * The type Tx info.
 */
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

    public PlatformTransactionManager getTm() {
        return tm;
    }

    public void setTm(PlatformTransactionManager tm) {
        this.tm = tm;
    }

    public TransactionStatus getTs() {
        return ts;
    }

    public void setTs(TransactionStatus ts) {
        this.ts = ts;
    }

    public TransactionAttribute getTa() {
        return ta;
    }

    public void setTa(TransactionAttribute ta) {
        this.ta = ta;
    }

    public Tx getTx() {
        return tx;
    }

    public void setTx(Tx tx) {
        this.tx = tx;
    }

    public String getPoint() {
        return point;
    }

    public void setPoint(String point) {
        this.point = point;
    }
}
