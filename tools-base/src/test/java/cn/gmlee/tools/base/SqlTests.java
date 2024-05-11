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
                "\t\tROUND( sum( BUY_NUMBER ), 0 ) AS tradeVolume,\n" +
                "\t\tmerchant_name merchantName,\n" +
                "\t\tNVL( ROUND( sum( ACTUAL_RECEIPT ) / 10000 ), 0 ) tradeAmount,\n" +
                "\t\tcount( * ) orderCount \n" +
                "\tFROM\n" +
                "\t\t(\n" +
                "\t\tSELECT\n" +
                "\t\tCASE\n" +
                "\t\t\t\t\n" +
                "\t\t\tWHEN\n" +
                "\t\t\t\tmoi.SELL_WAY = '2' THEN\n" +
                "\t\t\t\t\tmob.paytime ELSE moi.PAYTIME \n" +
                "\t\t\t\tEND paytime,\n" +
                "\tmoi.buy_number,\n" +
                "\tmoi.actual_receipt,\n" +
                "\tsm.merchant_name,\n" +
                "\tbelong_to_region \n" +
                "FROM\n" +
                "\tMALL_ORDER_INFO moi\n" +
                "\tLEFT JOIN ldw_sys.sys_merchant sm ON moi.buy_merchant_id = sm.id\n" +
                "\tLEFT JOIN (\n" +
                "\tSELECT\n" +
                "\t\tORDER_NO,\n" +
                "\t\tSUM( AMOUNT ) AS AMOUNT,\n" +
                "\t\tmin( UPDATE_TIME ) paytime \n" +
                "\tFROM\n" +
                "\t\tMALL_ORDER_BOND \n" +
                "\tWHERE\n" +
                "\t\tHAS_DELETE = '0' \n" +
                "\t\tAND STATUS = '2' \n" +
                "\tGROUP BY\n" +
                "\t\tORDER_NO \n" +
                "\t) mob ON moi.ORDER_NO = mob.ORDER_NO\n" +
                "\tLEFT JOIN mall_goods_snapshot mgs ON moi.order_no = mgs.order_no \n" +
                "WHERE\n" +
                "\tmoi.payment_receipt IS NOT NULL \n" +
                "\tAND moi.ORDER_STATUS != '99' \n" +
                "\tAND moi.business_type = '2' \n" +
                "\tAND moi.sell_merchant_id = 'M00610' \n" +
                "\tAND moi.buy_merchant_id NOT IN ( SELECT merchant_id FROM ldw_sys.data_board_test_merchant ) \n" +
                "\t) a \n" +
                "WHERE\n" +
                "\tpaytime IS NOT NULL \n" +
                "GROUP BY\n" +
                "\tbelong_to_region,\n" +
                "\tmerchant_name \n" +
                "\t) a \n" +
                "WHERE\n" +
                "\tROWNUM <= 30 \n" +
                "ORDER BY\n" +
                "\ta.tradeVolume DESC";
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
