package cn.gmlee.tools.base;

import cn.gmlee.tools.base.util.SqlUtil;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SqlTests {

    @Test
    public void testSql1() throws Exception{
        String sql = "SELECT\n" +
                "\ta.capital_change_id,\n" +
                "\ta.payment_type,\n" +
                "\ta.available_balance,\n" +
                "\ta.freezed_available_balance,\n" +
                "\ta.amount,\n" +
                "\ta.create_time,\n" +
                "\tb.trade_record_id,\n" +
                "\tc.file_path,\n" +
                "CASE\n" +
                "\t\t\n" +
                "\t\tWHEN b.flow_type = '2' THEN\n" +
                "\t\tnvl( b.REMARK, '--' ) ELSE NULL \n" +
                "\tEND AS remark,\n" +
                "\tb.BANK_FLOW_ID,\n" +
                "CASE\n" +
                "\t\t\n" +
                "\t\tWHEN b.PAYMENT_ACCOUNT_ID IS NOT NULL THEN\n" +
                "\t\t( SELECT t.bank_sub_account FROM cpp.t_merchant_account t WHERE t.account_id = b.PAYMENT_ACCOUNT_ID ) \n" +
                "\t\tWHEN b.PAYMENT_ACCOUNT_ID IS NULL THEN\n" +
                "\t\t( SELECT t.encrypt_account FROM cpp.t_cust_settle t WHERE t.settle_account_id = b.PAYMENT_SETTLE_ACCOUNT_ID ) ELSE b.PAYMENT_ACCOUNT_ID \n" +
                "\tEND AS pay_ACCOUNT_ID,\n" +
                "CASE\n" +
                "\t\t\n" +
                "\t\tWHEN b.COLLECT_ACCOUNT_ID IS NOT NULL THEN\n" +
                "\t\t( SELECT t.bank_sub_account FROM cpp.t_merchant_account t WHERE t.account_id = b.COLLECT_ACCOUNT_ID ) \n" +
                "\t\tWHEN b.COLLECT_ACCOUNT_ID IS NULL THEN\n" +
                "\t\t( SELECT t.encrypt_account FROM cpp.t_cust_settle t WHERE t.settle_account_id = b.COLLECT_SETTLE_ACCOUNT_ID ) ELSE b.COLLECT_ACCOUNT_ID \n" +
                "\tEND AS collect_ACCOUNT_ID,\n" +
                "\tb.payer_cust_no,\n" +
                "\tb.collect_cust_no \n" +
                "FROM\n" +
                "\tcpp.t_capital_change_record b\n" +
                "\tINNER JOIN cpp.t_capital_change_flow a ON a.capital_change_id = b.capital_change_id \n" +
                "\tINNER JOIN cpp.t_merchant_account t ON a.account_id = t.account_id \n" +
                "\tLEFT JOIN cpp.t_bank_receipt c ON c.bank_id = b.bank_flow_id \n" +
                "WHERE\n" +
                "\tt.cust_no = 'M00610' \n" +
                "\tAND a.create_time >= to_date( '2023-5-21' || ' 00:00:00', 'yyyy-mm-dd hh24:mi:ss' ) \n" +
                "\tAND a.create_time <= to_date( '2024-5-20' || ' 23:59:59', 'yyyy-mm-dd hh24:mi:ss' ) \n" +
                "\tAND a.payment_type IN ( '1', '2', '3', '4' ) ";
        Map<String, List> wheres = new HashMap<>();
        wheres.put("env", Arrays.asList("0", "1"));
        SqlUtil.reset(SqlUtil.DataType.ORACLE);
        String newSql = SqlUtil.newSelect(sql, wheres);
        System.out.println(newSql);
    }
}
