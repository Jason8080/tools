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
                "\t* \n" +
                "FROM\n" +
                "\t(\n" +
                "\tSELECT\n" +
                "\t\tTMP.*,\n" +
                "\t\tROWNUM ROW_ID \n" +
                "\tFROM\n" +
                "\t\t(\n" +
                "\t\tSELECT\n" +
                "\t\t\tt1.capital_change_id,\n" +
                "\t\t\tt1.recordId recordId,\n" +
                "\t\t\tt2.contract_id contractId,\n" +
                "\t\t\tnvl( t2.payment_cust_name, decode( t1.paymentType, '1', t3.other_account_name, t3.bank_sub_account_name ) ) paymentCustName,\n" +
                "\t\t\tnvl( nvl2( t2.payment_cust_name, t2.collect_cust_name, t3.bank_sub_account_name ), t4.bank_sub_account_name ) collect_cust_name,\n" +
                "\t\t\tt1.paymentType,\n" +
                "\t\t\tnvl( t2.trans_type, '20' ) transType,\n" +
                "\t\t\tnvl( t2.trans_type_cn, t3.remark ) trans_type_cn,\n" +
                "\t\t\tt2.business_mode,\n" +
                "\t\t\tt1.amount,\n" +
                "\t\t\tt1.preAvaliableBalance,\n" +
                "\t\t\tt1.preFreezeBalance,\n" +
                "\t\t\tnvl( t2.trans_status, t3.trans_state ) trans_status,\n" +
                "\t\t\tnvl( t2.trans_status_cn, t3.trans_status_cn ) trans_status_cn,\n" +
                "\t\t\tto_char( t1.create_time, 'yyyy-mm-dd hh24:mi:ss' ) createTime,\n" +
                "\t\t\tnvl( t1.pay_ACCOUNT_ID, t3.other_account_no ) pay_ACCOUNT_ID,\n" +
                "\t\t\tnvl( t4.other_account_no, t1.collect_ACCOUNT_ID ) collect_ACCOUNT_ID,\n" +
                "\t\t\tnvl( nvl( t3.cmt, t1.remark ), t4.cmt ) remark,\n" +
                "\t\t\tt1.bankreceipt,\n" +
                "\t\t\tnvl( t3.bank_flow_no, t4.bank_flow_no ) bank_flow_id,\n" +
                "\t\t\t( SELECT ma.merchant_kind FROM cpp.t_merchant_account ma WHERE ma.cust_no = t1.payer_cust_no ) paymentMerchantKind,\n" +
                "\t\t\t( SELECT ma.merchant_kind FROM cpp.t_merchant_account ma WHERE ma.cust_no = t1.collect_cust_no ) collectMerchantKind \n" +
                "\t\tFROM\n" +
                "\t\t\t(\n" +
                "\t\t\tSELECT\n" +
                "\t\t\t\tk.capital_change_id,\n" +
                "\t\t\t\tk.trade_record_id recordId,\n" +
                "\t\t\t\tk.payment_type paymentType,\n" +
                "\t\t\t\tk.available_balance preAvaliableBalance,\n" +
                "\t\t\t\tk.freezed_available_balance preFreezeBalance,\n" +
                "\t\t\t\tk.amount amount,\n" +
                "\t\t\t\tk.create_time,\n" +
                "\t\t\t\tk.file_path bankReceipt,\n" +
                "\t\t\t\tk.remark,\n" +
                "\t\t\t\tk.bank_flow_id,\n" +
                "\t\t\t\tk.pay_ACCOUNT_ID,\n" +
                "\t\t\t\tk.collect_ACCOUNT_ID,\n" +
                "\t\t\t\tk.payer_cust_no,\n" +
                "\t\t\t\tk.collect_cust_no \n" +
                "\t\t\tFROM\n" +
                "\t\t\t\t(\n" +
                "\t\t\t\tSELECT\n" +
                "\t\t\t\t\ta.capital_change_id,\n" +
                "\t\t\t\t\ta.payment_type,\n" +
                "\t\t\t\t\ta.available_balance,\n" +
                "\t\t\t\t\ta.freezed_available_balance,\n" +
                "\t\t\t\t\ta.amount,\n" +
                "\t\t\t\t\ta.create_time,\n" +
                "\t\t\t\t\tb.trade_record_id,\n" +
                "\t\t\t\t\tc.file_path,\n" +
                "\t\t\t\tCASE\n" +
                "\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\tWHEN b.flow_type = '2' THEN\n" +
                "\t\t\t\t\t\tnvl( b.REMARK, '--' ) ELSE '' \n" +
                "\t\t\t\t\tEND AS remark,\n" +
                "\t\t\t\t\tb.BANK_FLOW_ID,\n" +
                "\t\t\t\t\t(\n" +
                "\t\t\t\t\tCASE\n" +
                "\t\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\tWHEN b.PAYMENT_ACCOUNT_ID IS NOT NULL THEN\n" +
                "\t\t\t\t\t\t\t( SELECT t.bank_sub_account FROM cpp.t_merchant_account t WHERE t.account_id = b.PAYMENT_ACCOUNT_ID ) \n" +
                "\t\t\t\t\t\t\tWHEN b.PAYMENT_ACCOUNT_ID IS NULL THEN\n" +
                "\t\t\t\t\t\t\t( SELECT t.encrypt_account FROM cpp.t_cust_settle t WHERE t.settle_account_id = b.PAYMENT_SETTLE_ACCOUNT_ID ) ELSE b.PAYMENT_ACCOUNT_ID \n" +
                "\t\t\t\t\t\tEND \n" +
                "\t\t\t\t\t\t) AS pay_ACCOUNT_ID,\n" +
                "\t\t\t\t\t\t(\n" +
                "\t\t\t\t\t\tCASE\n" +
                "\t\t\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t\tWHEN b.COLLECT_ACCOUNT_ID IS NOT NULL THEN\n" +
                "\t\t\t\t\t\t\t\t( SELECT t.bank_sub_account FROM cpp.t_merchant_account t WHERE t.account_id = b.COLLECT_ACCOUNT_ID ) \n" +
                "\t\t\t\t\t\t\t\tWHEN b.COLLECT_ACCOUNT_ID IS NULL THEN\n" +
                "\t\t\t\t\t\t\t\t( SELECT t.encrypt_account FROM cpp.t_cust_settle t WHERE t.settle_account_id = b.COLLECT_SETTLE_ACCOUNT_ID ) ELSE b.COLLECT_ACCOUNT_ID \n" +
                "\t\t\t\t\t\t\tEND \n" +
                "\t\t\t\t\t\t\t) AS collect_ACCOUNT_ID,\n" +
                "\t\t\t\t\t\t\tb.payer_cust_no,\n" +
                "\t\t\t\t\t\t\tb.collect_cust_no \n" +
                "\t\t\t\t\t\tFROM\n" +
                "\t\t\t\t\t\t\tcpp.t_capital_change_record b\n" +
                "\t\t\t\t\t\t\tINNER JOIN cpp.t_capital_change_flow a ON a.capital_change_id = b.capital_change_id\n" +
                "\t\t\t\t\t\t\tINNER JOIN cpp.t_merchant_account t ON a.account_id = t.account_id\n" +
                "\t\t\t\t\t\t\tLEFT JOIN cpp.t_bank_receipt c ON c.bank_id = b.bank_flow_id \n" +
                "\t\t\t\t\t\tWHERE\n" +
                "\t\t\t\t\t\t\tt.cust_no = 'M00610' \n" +
                "\t\t\t\t\t\t\tAND a.create_time >= to_date( '2023-5-21' || ' 00:00:00', 'yyyy-mm-dd hh24:mi:ss' ) \n" +
                "\t\t\t\t\t\t\tAND a.create_time <= to_date( '2024-5-20' || ' 23:59:59', 'yyyy-mm-dd hh24:mi:ss' ) \n" +
                "\t\t\t\t\t\t\tAND a.payment_type IN ( '1', '2', '3', '4' ) \n" +
                "\t\t\t\t\t\t) k \n" +
                "\t\t\t\t\t) t1\n" +
                "\t\t\t\t\tLEFT JOIN (\n" +
                "\t\t\t\t\tSELECT\n" +
                "\t\t\t\t\t\ta.record_id,\n" +
                "\t\t\t\t\t\ta.inner_contract_no,\n" +
                "\t\t\t\t\t\tb.contract_id is_contract_id,\n" +
                "\t\t\t\t\t\t(\n" +
                "\t\t\t\t\t\tCASE\n" +
                "\t\t\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t\tWHEN b.contract_id IS NOT NULL THEN\n" +
                "\t\t\t\t\t\t\t\tb.contract_id \n" +
                "\t\t\t\t\t\t\t\tWHEN bt.contract_id IS NOT NULL \n" +
                "\t\t\t\t\t\t\t\tAND bt.target_id IS NOT NULL \n" +
                "\t\t\t\t\t\t\t\tAND a.trans_type = '114' THEN\n" +
                "\t\t\t\t\t\t\t\t\tbt.contract_id \n" +
                "\t\t\t\t\t\t\t\t\tWHEN bt.contract_id IS NOT NULL \n" +
                "\t\t\t\t\t\t\t\t\tAND bt.target_id IS NOT NULL \n" +
                "\t\t\t\t\t\t\t\t\tAND a.trans_type != '114' THEN\n" +
                "\t\t\t\t\t\t\t\t\t\tbt.contract_id || '/' || bt.target_id \n" +
                "\t\t\t\t\t\t\t\t\t\tWHEN a.target_id IS NOT NULL THEN\n" +
                "\t\t\t\t\t\t\t\t\t\ta.target_id \n" +
                "\t\t\t\t\t\t\t\t\t\tWHEN a.tender_meeting_id IS NOT NULL THEN\n" +
                "\t\t\t\t\t\t\t\t\t\ta.tender_meeting_id \n" +
                "\t\t\t\t\t\t\t\t\t\tWHEN a.trade_id IS NOT NULL THEN\n" +
                "\t\t\t\t\t\t\t\t\t\ta.trade_id \n" +
                "\t\t\t\t\t\t\t\t\t\tWHEN a.list_no IS NOT NULL THEN\n" +
                "\t\t\t\t\t\t\t\t\t\ta.list_no \n" +
                "\t\t\t\t\t\t\t\t\t\tWHEN a.protocol_id IS NOT NULL THEN\n" +
                "\t\t\t\t\t\t\t\t\t\ta.protocol_id ELSE NULL \n" +
                "\t\t\t\t\t\t\t\t\tEND \n" +
                "\t\t\t\t\t\t\t\t\t) AS contract_id,\n" +
                "\t\t\t\t\t\t\t\t\ta.amount,\n" +
                "\t\t\t\t\t\t\t\t\ta.trans_type,\n" +
                "\t\t\t\t\t\t\t\t\ta.trans_type_cn,\n" +
                "\t\t\t\t\t\t\t\t\ta.trans_status,\n" +
                "\t\t\t\t\t\t\t\t\ta.trans_status_cn,\n" +
                "\t\t\t\t\t\t\t\t\ta.reg_time,\n" +
                "\t\t\t\t\t\t\t\t\ta.payment_cust_name,\n" +
                "\t\t\t\t\t\t\t\t\ta.collect_cust_name,\n" +
                "\t\t\t\t\t\t\t\t\ta.actual_quantity,\n" +
                "\t\t\t\t\t\t\t\t\t( CASE WHEN a.sub_list_type IN ( '02', '03', '04', '13' ) THEN a.FRMWK_CONTRACT_NO ELSE NULL END ) AS FRMWK_CONTRACT_NO,\n" +
                "\t\t\t\t\t\t\t\t\ta.list_type,\n" +
                "\t\t\t\t\t\t\t\t\ta.price,\n" +
                "\t\t\t\t\t\t\t\t\t(\n" +
                "\t\t\t\t\t\t\t\t\tCASE\n" +
                "\t\t\t\t\t\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t\t\t\t\tWHEN ( c.report_type = '4' AND b.contract_id IS NULL AND a.trade_id IS NULL AND a.list_no IS NULL ) THEN\n" +
                "\t\t\t\t\t\t\t\t\t\t\tc.report_no ELSE NULL \n" +
                "\t\t\t\t\t\t\t\t\t\tEND \n" +
                "\t\t\t\t\t\t\t\t\t\t) AS dpmReportNo,\n" +
                "\t\t\t\t\t\t\t\t\t\ta.business_mode \n" +
                "\t\t\t\t\t\t\t\t\tFROM\n" +
                "\t\t\t\t\t\t\t\t\t\t(\n" +
                "\t\t\t\t\t\t\t\t\t\tSELECT\n" +
                "\t\t\t\t\t\t\t\t\t\t\tfl.inner_contract_no,\n" +
                "\t\t\t\t\t\t\t\t\t\t\tfl.record_id,\n" +
                "\t\t\t\t\t\t\t\t\t\t\tfl.trade_id,\n" +
                "\t\t\t\t\t\t\t\t\t\t\tfl.list_no,\n" +
                "\t\t\t\t\t\t\t\t\t\t\tfl.amount,\n" +
                "\t\t\t\t\t\t\t\t\t\t\tfl.trans_type,\n" +
                "\t\t\t\t\t\t\t\t\t\t\tfl.trans_type_cn,\n" +
                "\t\t\t\t\t\t\t\t\t\t\tfl.trans_status,\n" +
                "\t\t\t\t\t\t\t\t\t\t\tfl.trans_status_cn,\n" +
                "\t\t\t\t\t\t\t\t\t\t\tfl.reg_time,\n" +
                "\t\t\t\t\t\t\t\t\t\t\tfl.payment_cust_name,\n" +
                "\t\t\t\t\t\t\t\t\t\t\tfl.collect_cust_name,\n" +
                "\t\t\t\t\t\t\t\t\t\t\tfl.actual_quantity,\n" +
                "\t\t\t\t\t\t\t\t\t\t\tfl.FRMWK_CONTRACT_NO,\n" +
                "\t\t\t\t\t\t\t\t\t\t\tfl.sub_list_type,\n" +
                "\t\t\t\t\t\t\t\t\t\t\tfl.list_type,\n" +
                "\t\t\t\t\t\t\t\t\t\t\tfl.price,\n" +
                "\t\t\t\t\t\t\t\t\t\t\tfl.report_no,\n" +
                "\t\t\t\t\t\t\t\t\t\t\tfl.PROTOCOL_ID,\n" +
                "\t\t\t\t\t\t\t\t\t\t\tfl.target_id,\n" +
                "\t\t\t\t\t\t\t\t\t\t\tfl.tender_meeting_id,\n" +
                "\t\t\t\t\t\t\t\t\t\t\tfl.business_mode \n" +
                "\t\t\t\t\t\t\t\t\t\tFROM\n" +
                "\t\t\t\t\t\t\t\t\t\t\t(\n" +
                "\t\t\t\t\t\t\t\t\t\t\tSELECT\n" +
                "\t\t\t\t\t\t\t\t\t\t\t\tnvl( tl.inner_contract_no, qyb_tl.inner_contract_no ) inner_contract_no,\n" +
                "\t\t\t\t\t\t\t\t\t\t\t\ttfr.record_id,\n" +
                "\t\t\t\t\t\t\t\t\t\t\t\ttfr.trade_id,\n" +
                "\t\t\t\t\t\t\t\t\t\t\t\ttfr.list_no,\n" +
                "\t\t\t\t\t\t\t\t\t\t\t\ttfr.amount,\n" +
                "\t\t\t\t\t\t\t\t\t\t\t\ttfr.trans_type,\n" +
                "\t\t\t\t\t\t\t\t\t\t\t\ttfr.trans_type_cn,\n" +
                "\t\t\t\t\t\t\t\t\t\t\t\ttfr.trans_status,\n" +
                "\t\t\t\t\t\t\t\t\t\t\t\ttfr.trans_status_cn,\n" +
                "\t\t\t\t\t\t\t\t\t\t\t\tto_char( tfr.reg_time, 'YYYY-MM-DD HH24:MI:SS' ) reg_time,\n" +
                "\t\t\t\t\t\t\t\t\t\t\t\ttfr.payment_cust_name,\n" +
                "\t\t\t\t\t\t\t\t\t\t\t\ttfr.collect_cust_name,\n" +
                "\t\t\t\t\t\t\t\t\t\t\t\tttd.actual_quantity,\n" +
                "\t\t\t\t\t\t\t\t\t\t\t\ttl.FRMWK_CONTRACT_NO,\n" +
                "\t\t\t\t\t\t\t\t\t\t\t\ttl.sub_list_type,\n" +
                "\t\t\t\t\t\t\t\t\t\t\t\ttl.list_type,\n" +
                "\t\t\t\t\t\t\t\t\t\t\t\ttd.price,\n" +
                "\t\t\t\t\t\t\t\t\t\t\t\ttfr.report_no,\n" +
                "\t\t\t\t\t\t\t\t\t\t\t\ttfr.PROTOCOL_ID,\n" +
                "\t\t\t\t\t\t\t\t\t\t\t\ttfr.target_id,\n" +
                "\t\t\t\t\t\t\t\t\t\t\t\ttfr.tender_meeting_id,\n" +
                "\t\t\t\t\t\t\t\t\t\t\t\ttfr.business_mode \n" +
                "\t\t\t\t\t\t\t\t\t\t\tFROM\n" +
                "\t\t\t\t\t\t\t\t\t\t\t\tebp.t_trade_fund_record tfr\n" +
                "\t\t\t\t\t\t\t\t\t\t\t\tLEFT JOIN ebp.t_list_info tl ON tfr.list_no = tl.list_no\n" +
                "\t\t\t\t\t\t\t\t\t\t\t\tLEFT JOIN ebp.t_qyb_list_info qyb_tl ON tfr.list_no = qyb_tl.list_no\n" +
                "\t\t\t\t\t\t\t\t\t\t\t\tLEFT JOIN ebp.t_trade_delivery ttd ON ttd.delivery_id = tfr.delivery_id\n" +
                "\t\t\t\t\t\t\t\t\t\t\t\tLEFT JOIN ebp.t_trade_info td ON tfr.trade_id = td.trade_id \n" +
                "\t\t\t\t\t\t\t\t\t\t\tWHERE\n" +
                "\t\t\t\t\t\t\t\t\t\t\t\ttfr.list_no IS NOT NULL UNION ALL\n" +
                "\t\t\t\t\t\t\t\t\t\t\tSELECT\n" +
                "\t\t\t\t\t\t\t\t\t\t\t\tnvl( tif.inner_contract_no, qyb_tl.inner_contract_no ) inner_contract_no,\n" +
                "\t\t\t\t\t\t\t\t\t\t\t\ttfr.record_id,\n" +
                "\t\t\t\t\t\t\t\t\t\t\t\tnvl( tfr.trade_id, qybl.trade_id ) trade_id,\n" +
                "\t\t\t\t\t\t\t\t\t\t\t\tnvl( tfr.list_no, qybl.list_no ) list_no,\n" +
                "\t\t\t\t\t\t\t\t\t\t\t\ttfr.amount,\n" +
                "\t\t\t\t\t\t\t\t\t\t\t\ttfr.trans_type,\n" +
                "\t\t\t\t\t\t\t\t\t\t\t\ttfr.trans_type_cn,\n" +
                "\t\t\t\t\t\t\t\t\t\t\t\ttfr.trans_status,\n" +
                "\t\t\t\t\t\t\t\t\t\t\t\ttfr.trans_status_cn,\n" +
                "\t\t\t\t\t\t\t\t\t\t\t\tto_char( tfr.reg_time, 'YYYY-MM-DD HH24:MI:SS' ) reg_time,\n" +
                "\t\t\t\t\t\t\t\t\t\t\t\ttfr.payment_cust_name,\n" +
                "\t\t\t\t\t\t\t\t\t\t\tCASE\n" +
                "\t\t\t\t\t\t\t\t\t\t\t\t\t\n" +
                "\t\t\t\t\t\t\t\t\t\t\t\t\tWHEN tfr.trans_type = '23' \n" +
                "\t\t\t\t\t\t\t\t\t\t\t\t\tAND tfr.payment_cust_no = 'P20141025' THEN\n" +
                "\t\t\t\t\t\t\t\t\t\t\t\t\t\t'中粮招商局（深圳）粮食电子交易中心有限公司' ELSE tfr.collect_cust_name \n" +
                "\t\t\t\t\t\t\t\t\t\t\t\t\tEND collect_cust_name,\n" +
                "\tttd.actual_quantity,\n" +
                "\ttif.FRMWK_CONTRACT_NO,\n" +
                "\ttif.sub_list_type,\n" +
                "\ttif.list_type,\n" +
                "\ttd.price,\n" +
                "\ttfr.report_no,\n" +
                "\ttfr.PROTOCOL_ID,\n" +
                "\ttfr.target_id,\n" +
                "\ttfr.tender_meeting_id,\n" +
                "\ttfr.business_mode \n" +
                "FROM\n" +
                "\tebp.t_trade_fund_record tfr\n" +
                "\tLEFT JOIN ebp.t_trade_info td ON tfr.trade_id = td.trade_id\n" +
                "\tLEFT JOIN ebp.t_list_info tif ON td.list_no = tif.list_no\n" +
                "\tLEFT JOIN ebp.t_qyb_trade_info qybl ON tfr.trade_id = qybl.trade_id\n" +
                "\tLEFT JOIN ebp.t_qyb_list_info qyb_tl ON qybl.list_no = qyb_tl.list_no\n" +
                "\tLEFT JOIN ebp.t_trade_delivery ttd ON ttd.delivery_id = tfr.delivery_id \n" +
                "WHERE\n" +
                "\ttfr.list_no IS NULL \n" +
                "\t) fl \n" +
                "\t) a\n" +
                "\tLEFT JOIN ebp.t_trade_info b ON a.trade_id = b.trade_id\n" +
                "\tLEFT JOIN ebp.t_report_info c ON a.report_no = c.report_no\n" +
                "\tLEFT JOIN ebp.t_bid_trade_info bt ON bt.trade_id = a.trade_id UNION ALL\n" +
                "SELECT\n" +
                "\ts.cpr_id record_id,\n" +
                "\tNULL AS inner_contract_no,\n" +
                "\t'wl' IS_CONTRACT_ID,\n" +
                "\ts.cpr_cat_no contract_id,\n" +
                "\ts.cpr_amount_payment amount,\n" +
                "\tNULL AS trans_type,\n" +
                "\t'物流' || s.cpr_fund_use trans_type_cn,\n" +
                "\tNULL AS trans_status,\n" +
                "\tdecode( s.cpr_status, 0, '成功', 1, '失败', 2, '处理中' ) trans_status_cn,\n" +
                "\tto_char( s.cpr_create_time, 'YYYY-MM-DD HH24:MI:SS' ) reg_time,\n" +
                "\ts.cpr_out_name payment_cust_name,\n" +
                "\ts.cpr_in_name collect_cust_name,\n" +
                "\tNULL AS actual_quantity,\n" +
                "\tNULL AS FRMWK_CONTRACT_NO,\n" +
                "\tNULL AS list_type,\n" +
                "\tNULL AS price,\n" +
                "\tNULL AS dpmReportNo,\n" +
                "\t'纯物流' AS bussiness_mode \n" +
                "FROM\n" +
                "\tcarry.charge_payment_record s \n" +
                "WHERE\n" +
                "\ts.cpr_fund_type IN ( 1, 2 ) \n" +
                "\t) t2 ON t1.recordId = t2.record_id\n" +
                "\tLEFT JOIN (\n" +
                "\tSELECT\n" +
                "\t\tbtf.bank_flow_no,\n" +
                "\t\tbtf.bank_sub_account_no,\n" +
                "\t\tbtf.bank_sub_account_name,\n" +
                "\t\tbtf.other_account_no,\n" +
                "\t\tbtf.other_account_name,\n" +
                "\t\tbtf.trans_state,\n" +
                "\tCASE\n" +
                "\t\t\tbtf.trans_state \n" +
                "\t\t\tWHEN '0' THEN\n" +
                "\t\t\t'成功' \n" +
                "\t\t\tWHEN '1' THEN\n" +
                "\t\t\t'失败' ELSE '处理中' \n" +
                "\t\tEND trans_status_cn,\n" +
                "\tabt.cmt cmt,\n" +
                "\tabt.remark remark \n" +
                "FROM\n" +
                "\tcpp.t_bank_trans_flow btf\n" +
                "\tLEFT JOIN cpp.t_abc_bank_trans_detail abt ON btf.bank_flow_no = abt.tr_date || abt.tr_jrn \n" +
                "\tAND btf.bank_sub_account_no = abt.log_acc_no \n" +
                "WHERE\n" +
                "\tbtf.trans_type = '10' \n" +
                "\t) t3 ON t1.bank_flow_id = t3.bank_flow_no\n" +
                "\tLEFT JOIN (\n" +
                "\tSELECT\n" +
                "\t\tbtf.bank_flow_no,\n" +
                "\t\tbtf.trace_no,\n" +
                "\t\tbtf.bank_sub_account_no,\n" +
                "\t\tbtf.bank_sub_account_name,\n" +
                "\t\tbtf.other_account_no,\n" +
                "\t\tbtf.other_account_name,\n" +
                "\t\tbtf.trans_state,\n" +
                "\tCASE\n" +
                "\t\t\tbtf.trans_state \n" +
                "\t\t\tWHEN '0' THEN\n" +
                "\t\t\t'成功' \n" +
                "\t\t\tWHEN '1' THEN\n" +
                "\t\t\t'失败' ELSE '处理中' \n" +
                "\t\tEND trans_status_cn,\n" +
                "\tabt.cmt cmt,\n" +
                "\tabt.remark remark \n" +
                "FROM\n" +
                "\tcpp.t_bank_trans_flow btf\n" +
                "\tLEFT JOIN cpp.t_abc_bank_trans_detail abt ON btf.bank_flow_no = abt.tr_date || abt.tr_jrn \n" +
                "\tAND btf.bank_sub_account_no = abt.log_acc_no \n" +
                "WHERE\n" +
                "\tbtf.trans_type IN ( '00', '21', '22' ) \n" +
                "\t) t4 ON t1.recordId = t4.trace_no \n" +
                "\tAND t1.pay_account_id = t4.bank_sub_account_no \n" +
                "ORDER BY\n" +
                "\tt1.create_time DESC,\n" +
                "\tt1.capital_change_id DESC \n" +
                "\t) TMP \n" +
                "WHERE\n" +
                "\tROWNUM <= 10 \n" +
                "\t) \n" +
                "WHERE\n" +
                "\tROW_ID > 0";
        Map<String, List> wheres = new HashMap<>();
        wheres.put("env", Arrays.asList("0", "1"));
        SqlUtil.reset(SqlUtil.DataType.ORACLE);
        String newSql = SqlUtil.newSelect(sql, wheres);
        System.out.println(newSql);
    }
}
