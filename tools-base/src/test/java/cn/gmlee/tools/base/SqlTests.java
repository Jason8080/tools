package cn.gmlee.tools.base;

import cn.gmlee.tools.base.assist.ExpressionAssist;
import cn.gmlee.tools.base.util.SqlUtil;
import net.sf.jsqlparser.expression.Expression;
import org.junit.Test;

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
                "\t\tNVL( COUNT( 1 ), 0 ) AS sunActive \n" +
                "\tFROM\n" +
                "\t\t(\n" +
                "\t\tSELECT\n" +
                "\t\t\tBUY_MERCHANT_ID \n" +
                "\t\tFROM\n" +
                "\t\t\t(\n" +
                "\t\t\tSELECT\n" +
                "\t\t\t\tmai.order_no,\n" +
                "\t\t\t\tmai.buy_merchant_id,\n" +
                "\t\t\t\tmai.order_status,\n" +
                "\t\t\t\tmai.SELL_MERCHANT_ID,\n" +
                "\t\t\tCASE\n" +
                "\t\t\t\t\t\n" +
                "\t\t\t\t\tWHEN mai.SELL_WAY = '2' THEN\n" +
                "\t\t\t\t\tmob.paytime ELSE mai.PAYTIME \n" +
                "\t\t\t\tEND paytime \n" +
                "FROM\n" +
                "\tMALL_ORDER_INFO mai\n" +
                "\tLEFT JOIN ( SELECT ORDER_NO, min( UPDATE_TIME ) paytime FROM MALL_ORDER_BOND WHERE HAS_DELETE = '0' AND STATUS = '2' GROUP BY ORDER_NO ) mob ON mai.ORDER_NO = mob.ORDER_NO \n" +
                "\t) a \n" +
                "WHERE\n" +
                "\tPAYTIME >= TRUNC( SYSDATE ) \n" +
                "\tAND SELL_MERCHANT_ID = 'M00201' \n" +
                "\tAND ORDER_STATUS != '99' \n" +
                "GROUP BY\n" +
                "\tBUY_MERCHANT_ID \n" +
                "\t) \n" +
                "\t),\n" +
                "\t(\n" +
                "\tSELECT\n" +
                "\t\tNVL( COUNT( 1 ), 0 ) AS weekActive \n" +
                "\tFROM\n" +
                "\t\t(\n" +
                "\t\tSELECT\n" +
                "\t\t\tCOUNT( * ) \n" +
                "\t\tFROM\n" +
                "\t\t\t(\n" +
                "\t\t\tSELECT\n" +
                "\t\t\t\tmai.order_no,\n" +
                "\t\t\t\tmai.buy_merchant_id,\n" +
                "\t\t\t\tmai.order_status,\n" +
                "\t\t\t\tmai.SELL_MERCHANT_ID,\n" +
                "\t\t\tCASE\n" +
                "\t\t\t\t\t\n" +
                "\t\t\t\t\tWHEN mai.SELL_WAY = '2' THEN\n" +
                "\t\t\t\t\tmob.paytime ELSE mai.PAYTIME \n" +
                "\t\t\t\tEND paytime \n" +
                "FROM\n" +
                "\tMALL_ORDER_INFO mai\n" +
                "\tLEFT JOIN ( SELECT ORDER_NO, min( UPDATE_TIME ) paytime FROM MALL_ORDER_BOND WHERE HAS_DELETE = '0' AND STATUS = '2' GROUP BY ORDER_NO ) mob ON mai.ORDER_NO = mob.ORDER_NO \n" +
                "\t) a \n" +
                "WHERE\n" +
                "\tPAYTIME >= TRUNC( SYSDATE, 'IW' ) \n" +
                "\tAND SELL_MERCHANT_ID = 'M00201' \n" +
                "\tAND ORDER_STATUS != '99' \n" +
                "GROUP BY\n" +
                "\tBUY_MERCHANT_ID \n" +
                "\t) \n" +
                "\t),\n" +
                "\t(\n" +
                "\tSELECT\n" +
                "\t\tNVL( COUNT( 1 ), 0 ) AS oldWeekActive \n" +
                "\tFROM\n" +
                "\t\t(\n" +
                "\t\tSELECT\n" +
                "\t\t\tBUY_MERCHANT_ID \n" +
                "\t\tFROM\n" +
                "\t\t\t(\n" +
                "\t\t\tSELECT\n" +
                "\t\t\t\tmai.order_no,\n" +
                "\t\t\t\tmai.buy_merchant_id,\n" +
                "\t\t\t\tmai.order_status,\n" +
                "\t\t\t\tmai.SELL_MERCHANT_ID,\n" +
                "\t\t\tCASE\n" +
                "\t\t\t\t\t\n" +
                "\t\t\t\t\tWHEN mai.SELL_WAY = '2' THEN\n" +
                "\t\t\t\t\tmob.paytime ELSE mai.PAYTIME \n" +
                "\t\t\t\tEND paytime \n" +
                "FROM\n" +
                "\tMALL_ORDER_INFO mai\n" +
                "\tLEFT JOIN ( SELECT ORDER_NO, min( UPDATE_TIME ) paytime FROM MALL_ORDER_BOND WHERE HAS_DELETE = '0' AND STATUS = '2' GROUP BY ORDER_NO ) mob ON mai.ORDER_NO = mob.ORDER_NO \n" +
                "\t) a \n" +
                "WHERE\n" +
                "\tPAYTIME >= TRUNC( SYSDATE, 'IW' ) - 7 \n" +
                "\tAND PAYTIME < TRUNC( SYSDATE, 'IW' ) \n" +
                "\tAND SELL_MERCHANT_ID = 'M00201' \n" +
                "\tAND ORDER_STATUS != '99' \n" +
                "GROUP BY\n" +
                "\tBUY_MERCHANT_ID \n" +
                "\t) \n" +
                "\t),\n" +
                "\t(\n" +
                "\tSELECT\n" +
                "\t\tNVL( COUNT( 1 ), 0 ) AS oldSunActive \n" +
                "\tFROM\n" +
                "\t\t(\n" +
                "\t\tSELECT\n" +
                "\t\t\tBUY_MERCHANT_ID \n" +
                "\t\tFROM\n" +
                "\t\t\t(\n" +
                "\t\t\tSELECT\n" +
                "\t\t\t\tmai.order_no,\n" +
                "\t\t\t\tmai.buy_merchant_id,\n" +
                "\t\t\t\tmai.order_status,\n" +
                "\t\t\t\tmai.SELL_MERCHANT_ID,\n" +
                "\t\t\tCASE\n" +
                "\t\t\t\t\t\n" +
                "\t\t\t\t\tWHEN mai.SELL_WAY = '2' THEN\n" +
                "\t\t\t\t\tmob.paytime ELSE mai.PAYTIME \n" +
                "\t\t\t\tEND paytime \n" +
                "FROM\n" +
                "\tMALL_ORDER_INFO mai\n" +
                "\tLEFT JOIN ( SELECT ORDER_NO, min( UPDATE_TIME ) paytime FROM MALL_ORDER_BOND WHERE HAS_DELETE = '0' AND STATUS = '2' GROUP BY ORDER_NO ) mob ON mai.ORDER_NO = mob.ORDER_NO \n" +
                "\t) a \n" +
                "WHERE\n" +
                "\tTRUNC( PAYTIME ) = TRUNC( SYSDATE ) - 1 \n" +
                "\tAND SELL_MERCHANT_ID = 'M00201' \n" +
                "\tAND ORDER_STATUS != '99' \n" +
                "GROUP BY\n" +
                "\tBUY_MERCHANT_ID \n" +
                "\t) \n" +
                "\t)";
        Map<String, List<Expression>> wheres = new HashMap<>();
        wheres.put("env", ExpressionAssist.as("0", "1"));
//        wheres.put("auth_id", ExpressionAssist.as());
//        wheres.put("auth_id", ExpressionAssist.as("1", "2", "3"));
//        wheres.put("auth_type", ExpressionAssist.as(0, 1));
        SqlUtil.reset(SqlUtil.DataType.ORACLE);
        String newSql = SqlUtil.newSelect(sql, wheres);
        System.out.println(newSql);
    }
}
